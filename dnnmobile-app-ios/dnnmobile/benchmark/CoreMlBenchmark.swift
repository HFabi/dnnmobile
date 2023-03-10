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
    var updatableModel: MLModel?
    var initialModel: MLModel?
    var dnnModel: DnnModel
    
    var trainInputPath: String
    var trainTargetPath: String
    var testInputPath: String
    var testTargetPath: String
    
    init(
        dnnModel: DnnModel,
        dataset: DnnDataset
    ) {
        self.dnnModel = dnnModel
        self.trainInputPath = Bundle.main.url(forResource: dataset.trainInput, withExtension: "csv")!.relativePath
        self.trainTargetPath = Bundle.main.url(forResource: dataset.trainTarget, withExtension: "csv")!.relativePath
        self.testInputPath = Bundle.main.url(forResource: dataset.testInput, withExtension: "csv")!.relativePath
        self.testTargetPath = Bundle.main.url(forResource: dataset.testTarget, withExtension: "csv")!.relativePath
        
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
    
    
    func inference(featureProviderDict: MLDictionaryFeatureProvider)->Int? {
        guard let model = self.updatableModel else {
            print("inference, updatable model nil")
            return nil
        }
        
        var predictionValue:Int? = nil
        do {
            let prediction: MLFeatureProvider = try model.prediction(from: featureProviderDict)
            let m:MLMultiArray? = prediction.featureValue(for: self.feature_label_output)!.multiArrayValue
            
            let output = multiArrayToArray(multiArray: m!)
            predictionValue = argmax(data: output)
        } catch(let error){
            print("error is \(error.localizedDescription)")
        }
        return predictionValue
    }
    
    func training() {
        print("training mnist")
        //
        let modelConfig = MLModelConfiguration()
        modelConfig.computeUnits = .cpuOnly
        modelConfig.parameters = [MLParameterKey.epochs : 50]
        
        // load from document directory?
        let fileManager = FileManager.default
        let documentDirectory = try! fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor:nil, create:true)
        var modelURL = Bundle.main.url(forResource: modelresourcename, withExtension: "mlmodelc")!
        let pathOfFile = documentDirectory.appendingPathComponent(modelresourcenamewithending)
        if fileManager.fileExists(atPath: pathOfFile.path){
            modelURL = pathOfFile
        }
        
        let start = DispatchTime.now()
        do {
            let batchProvider = try prepareBatchProviderCSV(filePathInput: trainInputPath, filePathTarget: trainTargetPath)
            
            let updateTask = try MLUpdateTask(forModelAt: modelURL, trainingData: batchProvider, configuration: modelConfig, progressHandlers: MLUpdateProgressHandlers(forEvents: [.trainingBegin,.miniBatchEnd, .epochEnd], progressHandler: { (contextProgress) in
                
                switch contextProgress.event {
                case .trainingBegin:
                    print("Training begin")
                    
                case .miniBatchEnd:
                    let batchIndex = contextProgress.metrics[.miniBatchIndex] as! Int
                    let batchLoss = contextProgress.metrics[.lossValue] as! Double
                    
                case .epochEnd:
                    let epochIndex = contextProgress.metrics[.epochIndex] as! Int
                    let trainLoss = contextProgress.metrics[.lossValue] as! Double
                    print("Epoch end \(epochIndex), loss: \(trainLoss)")
                    
                default:
                    print("switch default")
                }
                
            }) { (finalContext) in
                if finalContext.task.error?.localizedDescription == nil
                {
                    let end = DispatchTime.now()
                    let nanoTime = end.uptimeNanoseconds - start.uptimeNanoseconds
                    print("TIME TRAINING:: \(nanoTime) \(Double(nanoTime)/1000.0/1000.0) in nanoseconds")
                    print("training end")
                    
                    let fileManager = FileManager.default
                    do {
                        print("training before update")
                        // write trained model to file
                        let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor:nil, create:true)
                        let fileURL = documentDirectory.appendingPathComponent(self.modelresourcenamewithending)
                        try finalContext.model.write(to: fileURL)
                        
                        // replace our model with the trained instance for next inference
                        self.updatableModel = self.loadModel(url: fileURL)
                        print("training after update")
                        
                        self.groupInference() // TODO
                        
                    } catch(let error) {
                        print("error is \(error.localizedDescription)")
                    }
                } else {
                    print("localizedDescription == nil")
                    print("error is \(finalContext.task.error?.localizedDescription)")
                }
            })
            updateTask.resume()
        } catch(let error) {
            print("error is \(error.localizedDescription)")
        }
    }
    
    
}
