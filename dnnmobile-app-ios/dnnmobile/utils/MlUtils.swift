//
//  MlUtils.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation
import CoreML

struct MlUtils {
    
    func argmax(data: [Float])-> Int {
        var maxvalue:Float = 0.0
        var maxindex = 0
        for i in 0..<data.count {
            if(data[i] > maxvalue) {
                maxindex = i
                maxvalue = data[i]
            }
        }
        return maxindex
    }
    
    func multiArrayToArray(multiArray: MLMultiArray)-> [Float] {
        let length = multiArray.count
        let floatPtr =  multiArray.dataPointer.bindMemory(to: Float.self, capacity: length)
        let floatBuffer = UnsafeBufferPointer(start: floatPtr, count: length)
        let output:[Float] = Array(floatBuffer)
        return output
    }
    
    func multiArrayToInt(multiArray: MLMultiArray)-> Int {
        let length = multiArray.count
        let intPtr =  multiArray.dataPointer.bindMemory(to: Int32.self, capacity: length)
        let intBuffer = UnsafeBufferPointer(start: intPtr, count: length)
        let output:[Int32] = Array(intBuffer)
        return Int(output[0])
    }
}
