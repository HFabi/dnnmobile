package de.hfabi.dnnmobile.benchmark

import android.os.SystemClock

/**
 * DnnTimer
 *
 * Measure the time of setup, training and inference.
 */
class DnnTimer {

    var durationSetup: Long = 0
    var durationInference: Long = 0
    var durationTraining: Long = 0

    var startSetupUptimeMillis: Long = 0
    var startInferenceUptimeMillis: Long = 0
    var startTrainingUptimeMillis: Long = 0

    fun notifySetupStarted() {
        startSetupUptimeMillis = SystemClock.uptimeMillis()
    }

    fun notifySetupStopped() {
        durationSetup = SystemClock.uptimeMillis() - startSetupUptimeMillis
    }

    fun notifyInferenceStarted() {
        startInferenceUptimeMillis = SystemClock.uptimeMillis()
    }

    fun notifyInferenceStopped() {
        durationInference = SystemClock.uptimeMillis() - startInferenceUptimeMillis
    }

    fun notifyTrainingStarted() {
        startTrainingUptimeMillis = SystemClock.uptimeMillis()
    }

    fun notifyTrainingStopped() {
        durationTraining = SystemClock.uptimeMillis() - startTrainingUptimeMillis
    }

    override fun toString(): String {
        return """
            Timer results
            -> setup took $durationSetup millis
            -> inference took $durationInference millis
            -> training took $durationTraining millis
        """
    }
}