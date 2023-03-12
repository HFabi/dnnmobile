//
//  CoreMlBenchmark.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation
import CoreML
import Vision

class CoreMlBenchmark: BaseBenchmark {
    var updatableModel: MLModel? = nil
    var initialModel: MLModel? = nil
    var dnnModel: CoreMlDnnModel
    
    var featuresInput: Int
    var featuresTarget: Int
    var trainSampleCount: Int
    var testSampleCount: Int
    
    var trainInputPath: String
    var trainTargetPath: String
    var testInputPath: String
    var testTargetPath: String
    
    init(
        dnnModel: CoreMlDnnModel,
        dataset: DnnDataset
    ) {
        self.dnnModel = dnnModel
        self.featuresInput = dataset.featuresInput
        self.featuresTarget = dataset.featuresTarget
        self.trainSampleCount = dataset.trainSampleCount
        self.testSampleCount = dataset.testSampleCount
        self.trainInputPath = Bundle.main.url(forResource: dataset.trainInput, withExtension: "csv")!.relativePath
        self.trainTargetPath = Bundle.main.url(forResource: dataset.trainTarget, withExtension: "csv")!.relativePath
        self.testInputPath = Bundle.main.url(forResource: dataset.testInput, withExtension: "csv")!.relativePath
        self.testTargetPath = Bundle.main.url(forResource: dataset.testTarget, withExtension: "csv")!.relativePath
        
        super.init()
        
        guard let modelURL = Bundle.main.url(forResource: dnnModel.fileName, withExtension: "mlmodelc") else {
            DnnLogger.logE(message: "Could not find model in bundle")
            return
        }
        
        guard let model = loadModel(url: modelURL) else {
            DnnLogger.logE(message: "Could not load model from bundle")
            return
        }
        self.initialModel = model
        self.updatableModel = model
    }
    
    private func loadModel(url: URL) -> MLModel? {
        do {
            let config = MLModelConfiguration()
            config.computeUnits = .all // e.g. cpu only?
            return try MLModel(contentsOf: url, configuration: config)
        } catch {
            DnnLogger.logE(error: error, message: "Could not load model")
            return nil
        }
    }
    
    /// Inference of CoreML model
    ///
    /// - Parameters
    ///     - featureProviderDict: input
    /// - Returns
    func inference(featureProviderDict: MLDictionaryFeatureProvider)->Int? {
        guard let model = self.updatableModel else {
            DnnLogger.logE(message: "Tried to run inference, but no model found for inference")
            return nil
        }
        
        var predictionValue: Int? = nil
        do {
            let prediction: MLFeatureProvider = try model.prediction(from: featureProviderDict)
            let outputMultiArray: MLMultiArray? = prediction.featureValue(for: self.dnnModel.featureLabelOutput)!.multiArrayValue
            let output = MlUtils.multiArrayToArray(multiArray: outputMultiArray!)
            predictionValue = MlUtils.argmax(data: output)
        } catch(let error){
            DnnLogger.logE(error: error, message: "Inference failed: \(error.localizedDescription)")
        }
        return predictionValue
    }
    
    
    func groupInference() {
        do {
            let batchprovider = prepareBatchProviderCSV(filePathInput: testInputPath, filePathTarget: testTargetPath)
            for i in 0..<20 {
                let feature = batchprovider.features(at: i)
                let data = feature.featureValue(for: self.dnnModel.featureLabelInput)
                let label = feature.featureValue(for: self.dnnModel.featureLabelOutputClass)
                let featureProviderDict = try MLDictionaryFeatureProvider(dictionary: [self.dnnModel.featureLabelInput : data])
                let prediction = inference(featureProviderDict: featureProviderDict)
                if (prediction != nil) {
                    DnnLogger.logD(message: "\(String(describing: label)) \(prediction!)")
                } else {
                    DnnLogger.logD(message: "\(String(describing: label)) prediction nil")
                }
            }
        } catch(let error){
            DnnLogger.logE(error: error, message: "In group inference")
        }
        accurcy(sampleCount: 100)
    }
    
    
    /// Training of CoreML model
    func training(epochs: Int) {
        // configure
        let modelConfig = MLModelConfiguration()
        modelConfig.computeUnits = .cpuOnly
        modelConfig.parameters = [MLParameterKey.epochs : epochs]
        
        // load from document directory?
        let fileManager = FileManager.default
        let documentDirectory = try! fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor:nil, create:true)
        var modelURL = Bundle.main.url(forResource: self.dnnModel.bundleName, withExtension: "mlmodelc")!
        let pathOfFile = documentDirectory.appendingPathComponent(self.dnnModel.fileName)
        if fileManager.fileExists(atPath: pathOfFile.path){
            modelURL = pathOfFile
        }
        
        timer.notifyTrainingStarted()
        do {
            let batchProvider = prepareBatchProviderCSV(filePathInput: trainInputPath, filePathTarget: trainTargetPath)
            
            let updateTask = try MLUpdateTask(forModelAt: modelURL, trainingData: batchProvider, configuration: modelConfig, progressHandlers: MLUpdateProgressHandlers(forEvents: [.trainingBegin,.miniBatchEnd, .epochEnd], progressHandler: { (contextProgress) in
                self.logProgress(updateContext: contextProgress)
            }) { (finalContext) in
                if finalContext.task.error?.localizedDescription == nil
                {
                    self.timer.notifyTrainingStopped()
                    DnnLogger.logD(message: "Training finished: \(self.timer.description)")
                    
                    let fileManager = FileManager.default
                    do {
                        // write trained model to file
                        let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create:true)
                        let fileURL = documentDirectory.appendingPathComponent(self.dnnModel.fileName)
                        try finalContext.model.write(to: fileURL)
                        
                        // replace our model with the trained instance for next inference
                        self.updatableModel = self.loadModel(url: fileURL)
                        
                        self.groupInference() // TODO
                        
                    } catch(let error) {
                        print("error is \(error.localizedDescription)")
                    }
                } else {
                    print("error is \(String(describing: finalContext.task.error?.localizedDescription))")
                }
            })
            updateTask.resume()
        } catch(let error) {
            DnnLogger.logE(error: error)
        }
    }
    
    /// Helper to prepare
    ///
    func prepareBatchProviderCSV(filePathInput: String, filePathTarget: String) -> MLBatchProvider {
        // https://github.com/JacopoMangiavacchi/MNIST-CoreML-Training/blob/master/MNIST-CoreML-Training/MNIST.swift
        var featureProviders = [MLFeatureProvider]()
        var allInputMultiArrays: [MLMultiArray] = []
        var sampleCounter = 0
        errno = 0
        
        // read input data from csv
        if freopen(filePathInput, "r", stdin) == nil {
            DnnLogger.logE(message: "Error open file \(filePathInput)")
        }
        while let line = readLine()?.split(separator: ",") {
            let inputMultiArray = try! MLMultiArray(shape: [NSNumber(value: self.featuresInput)], dataType: .float32)
            for i in 0..<self.featuresInput {
                inputMultiArray[i] = NSNumber(value: Float32(String(line[i]))!)
            }
            allInputMultiArrays.append(inputMultiArray)
        }
        if freopen(filePathTarget, "r", stdin) == nil {
            DnnLogger.logE(message: "Error open file \(filePathTarget)")
        }
        
        // read target data from csv
        while let line = readLine()?.split(separator: ",") {
            // read target
            let outputMultiArray = try! MLMultiArray(shape: [1], dataType: .int32)
            for i in 0..<self.featuresTarget {
                if (Float(String(line[i]))! == 1.0) {
                    outputMultiArray[0] = NSNumber(value: Float32(i))
                }
            }
            
            // combine input and target to feature provider
            let inputValue = MLFeatureValue(multiArray: allInputMultiArrays[sampleCounter])
            let targetValue = MLFeatureValue(multiArray: outputMultiArray)
            
            let dataPointFeatures: [String: MLFeatureValue] = [self.dnnModel.featureLabelInput: inputValue, self.dnnModel.featureLabelOutputClass: targetValue]
            
            if let provider = try? MLDictionaryFeatureProvider(dictionary: dataPointFeatures) {
                featureProviders.append(provider)
            }
            sampleCounter += 1
        }
        return MLArrayBatchProvider(array: featureProviders)
    }
    
    func logProgress(updateContext: MLUpdateContext) {
        switch updateContext.event {
        case .trainingBegin:
            DnnLogger.logD(message: "Training started")
        case .miniBatchEnd:
            let batchIndex = updateContext.metrics[.miniBatchIndex] as! Int
            let batchLoss = updateContext.metrics[.lossValue] as! Double
            // log?
        case .epochEnd:
            let epochIndex = updateContext.metrics[.epochIndex] as! Int
            let trainLoss = updateContext.metrics[.lossValue] as! Double
            DnnLogger.logD(message: "Epoch end \(epochIndex), loss: \(trainLoss)")
        default:
            break
        }
    }
    
    func accurcy(sampleCount: Int) {
        var counter: Float = 0.0
        do {
            let batchprovider = prepareBatchProviderCSV(filePathInput: testInputPath, filePathTarget: testTargetPath)
            for i in 0..<sampleCount {
                let feature = batchprovider.features(at: i)
                let data = feature.featureValue(for: self.dnnModel.featureLabelInput)
                let label = feature.featureValue(for: self.dnnModel.featureLabelOutputClass)
                let featureProviderDict = try MLDictionaryFeatureProvider(dictionary: [self.dnnModel.featureLabelInput : data])
                let prediction = inference(featureProviderDict: featureProviderDict)
                
                let labelInt = MlUtils.multiArrayToInt(multiArray: label!.multiArrayValue!)
                if (prediction != nil && prediction! == labelInt) {
                    counter += 1
                }
            }
            var accuracy = counter / Float(sampleCount)
            print("accuracy \(accuracy)")
        } catch(let error){
            print("error groupInference description is \(error.localizedDescription)")
        }
    }
}
