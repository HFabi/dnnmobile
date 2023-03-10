//
//  ContentViewModel.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation

class ContentViewModel {
    
    func runCoreMlMnist() {
        print("runCoreMlMnist ->")
        
        let benchmark = CoreMlBenchmark(dnnModel: ModelConfig.CoreMlMnist(),dataset: ModelConfig.Mnist())
        benchmark.training()
        print("<- runCoreMlMnist")
    }
}
