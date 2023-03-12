//
//  ModelConfig.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation

protocol DnnDataset {
    var name: String { get }
    var trainInput: String { get }
    var trainTarget: String { get }
    var testInput: String { get }
    var testTarget: String { get }
    var featuresInput: Int { get }
    var featuresTarget: Int { get }
    var trainSampleCount: Int { get }
    var testSampleCount: Int { get }
}

protocol DnnModel {
    var bundleName: String { get }
    var fileName: String { get }
}

protocol CoreMlDnnModel: DnnModel {
    var featureLabelInput: String { get }
    var featureLabelOutput: String { get }
    var featureLabelOutputClass: String { get }
    var allowedEpochs: Array<Int> { get }
}

extension CoreMlDnnModel {
    var fileName: String {
        get {
            return "\(bundleName).mlmodelc"
        }
    }
}

struct ModelConfig {
    
    private static let dataDir = "datasets"
    
    struct FashionMnistSmall : DnnDataset {
        let name = "Fashion Mnist Small"
        let trainInput = "\(dataDir)/fashion_mnist_600_train_input"
        let trainTarget = "\(dataDir)/fashion_mnist_600_train_target_categorical"
        let testInput = "\(dataDir)/fashion_mnist_100_test_input"
        let testTarget = "\(dataDir)/fashion_mnist_100_test_target_categorical"
        let featuresInput = 784
        let featuresTarget = 10
        let trainSampleCount = 600
        let testSampleCount = 100
    }
    
    struct BreastCancerWisconsin : DnnDataset {
        let name = "Breast Cancer Wisconsin"
        let trainInput = "\(dataDir)/fashion_mnist_600_train_input"
        let trainTarget = "\(dataDir)/fashion_mnist_600_train_target_categorical"
        let testInput = "\(dataDir)/fashion_mnist_100_test_input"
        let testTarget = "\(dataDir)/fashion_mnist_100_test_target_categorical"
        let featuresInput = 30
        let featuresTarget = 1
        let trainSampleCount = 398
        let testSampleCount = 171
    }
    
    struct Mnist : DnnDataset {
        let name = "Mnist"
        let trainInput = "\(dataDir)/fashion_mnist_600_train_input"
        let trainTarget = "\(dataDir)/fashion_mnist_600_train_target_categorical"
        let testInput = "\(dataDir)/fashion_mnist_100_test_input"
        let testTarget = "\(dataDir)/fashion_mnist_100_test_target_categorical"
        let featuresInput = 784
        let featuresTarget = 10
        let trainSampleCount = 60000
        let testSampleCount = 10000
    }
    
    struct MnistSmall : DnnDataset {
        let name = "Mnist"
        let trainInput = "\(dataDir)/fashion_mnist_600_train_input"
        let trainTarget = "\(dataDir)/fashion_mnist_600_train_target_categorical"
        let testInput = "\(dataDir)/fashion_mnist_100_test_input"
        let testTarget = "\(dataDir)/fashion_mnist_100_test_target_categorical"
        let featuresInput = 784
        let featuresTarget = 10
        let trainSampleCount = 600
        let testSampleCount = 100
    }
    
    struct CoreMlMnist: CoreMlDnnModel {
        var featureLabelInput = "ip_1_input"
        var featureLabelOutput = "ip_6_output"
        var featureLabelOutputClass = "ip_6_output_true"
        var allowedEpochs = [50]
        let bundleName = "MNIST100-500Updatable"
    }
}
