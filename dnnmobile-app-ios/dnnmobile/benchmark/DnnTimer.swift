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
    
    var startSetupUptimeMillis: Double = 0
    var startInferenceUptimeMillis: Double = 0
    var startTrainingUptimeMillis: Double = 0
    
    func notifySetupStarted() {
        startSetupUptimeMillis = CFAbsoluteTimeGetCurrent()
    }
    
    func notifySetupStopped() {
        durationSetup = CFAbsoluteTimeGetCurrent() - startSetupUptimeMillis
    }
    
    func notifyInferenceStarted() {
        startInferenceUptimeMillis = CFAbsoluteTimeGetCurrent()
    }
    
    func notifyInferenceStopped() {
        durationInference = CFAbsoluteTimeGetCurrent() - startInferenceUptimeMillis
    }
    
    func notifyTrainingStarted() {
        startTrainingUptimeMillis = CFAbsoluteTimeGetCurrent()
    }
    
    func notifyTrainingStopped() {
        durationTraining = CFAbsoluteTimeGetCurrent() - startTrainingUptimeMillis
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
