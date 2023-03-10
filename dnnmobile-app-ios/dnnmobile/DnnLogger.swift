//
//  DnnLogger.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation

struct DnnLogger {
    
    static func logD(message: String) {
        print(message)
    }
    
    static func logE(error: Error, message: String = "Error") {
        print("\(message) : \(message) ")
        print(error.localizedDescription)
    }
    
    static func logE(message: String = "Error") {
        print("\(message) : \(message) ")
    }
    
    static func loge(_ message: String) {
        print("Error : \(message) ")
    }
}
