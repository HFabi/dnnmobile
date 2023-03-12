//
//  DnnTimer.swift
//  dnnmobile
//
//  Created by Fabian on 10.03.23.
//

import Foundation

/**
 * DnnTimer
 *
 * Measure the time of setup, training and inference.
 */
class DnnTimer {
    
    var durationSetup: Double = 0
    var durationInference: Double = 0
    var durationTraining: Double = 0
    
    var startSetupUptimeMillis: UInt64 = 0
    var startInferenceUptimeMillis: UInt64 = 0
    var startTrainingUptimeMillis: UInt64 = 0
    
    func notifySetupStarted() {
        startSetupUptimeMillis = DispatchTime.now().uptimeNanoseconds
    }
    
    func notifySetupStopped() {
        durationSetup = Double(DispatchTime.now().uptimeNanoseconds - startSetupUptimeMillis)/1000.0/1000.0
    }
    
    func notifyInferenceStarted() {
        startInferenceUptimeMillis = DispatchTime.now().uptimeNanoseconds
    }
    
    func notifyInferenceStopped() {
        durationInference = Double(DispatchTime.now().uptimeNanoseconds - startInferenceUptimeMillis)/1000.0/1000.0
    }
    
    func notifyTrainingStarted() {
        startTrainingUptimeMillis = DispatchTime.now().uptimeNanoseconds
    }
    
    func notifyTrainingStopped() {
        durationTraining = Double(DispatchTime.now().uptimeNanoseconds - startTrainingUptimeMillis)/1000.0/1000.0
    }
    
    public var description: String {
        return """
            Timer results
            -> setup took \(durationSetup) millis
            -> inference took \(durationInference) millis
            -> training took \(durationTraining) millis
        """
    }
}
