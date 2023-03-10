package de.hfabi.dnnmobile

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import de.hfabi.dnnmobile.benchmark.DnnTimer
import de.hfabi.dnnmobile.utils.MlUtils
import java.nio.FloatBuffer

object DnnLogger {
    private const val TAG = "DNN"

    fun logD(message: String) {
        Log.d(TAG, message)
    }

    fun logE(error: Error, message: String = "Error:") {
        Log.e(TAG, "${message} : ${error.message}")
        Log.e(TAG, error.stackTraceToString())
    }

    fun logTime(dnnTimer: DnnTimer) {
        Log.d(TAG, dnnTimer.toString())
    }

    fun logSpacer() {
        Log.d(TAG, "------------------------------------")
    }

    fun logInferenceResult(classifications: Array<FloatArray>) {
        var out: String = ""
        for (c in classifications) {
            out = "$out   ${c.contentToString()}"
        }
        Log.d(TAG, "Inference: $out")
    }

    fun logInferenceResult(classifications: Array<Float>) {
        Log.d(TAG, "Inference: ${classifications.contentToString()}")
    }

    fun logInferenceResult(classifications: Map<Int, Any>) {
        Log.d(TAG, "Inference: $classifications")
    }

    fun logTrainingResult(loss: Float, loss1: Float) {
        Log.d(TAG, "Loss: $loss $loss1")
    }

    fun logTrainingResult(loss: Float) {
        Log.d(TAG, "Loss: $loss")
    }

    fun logInferenceCategorical(
        classSize: Int,
        testInputData: FloatBuffer,
        testTargetData: FloatArray
    ) {
        var resultString = ""
        for (c in 0..35) {
            val group = FloatArray(classSize)
            testInputData.get(group, 0, classSize)
            val prediction = MlUtils.oneHotEncodingToNumeric(group)
            val actualCategorical = testTargetData.copyOfRange(c * classSize, (c + 1) * classSize)
            val actual = MlUtils.oneHotEncodingToNumeric(actualCategorical)
            resultString = "${resultString}[p:${prediction} a:${actual}]  "
        }
        testInputData.rewind()
        logD(resultString)
    }

    fun logInference(
        testInputData: FloatBuffer,
        testTargetData: FloatArray
    ) {
        var resultString = ""
        for (c in 0..35) {
            val prediction = testInputData.get()
            val actual = testTargetData.copyOfRange(c, (c + 1))
            resultString = "${resultString}[p:${prediction} a:${actual[0]}]  "
        }
        testInputData.rewind()
        logD(resultString)
    }

    fun logMemory(context: Context) {
        Log.d(TAG, "- - -")
        val mem = getAvailableMemory(context)
        Log.d(TAG, "lowMemory: ${mem.lowMemory}")
        Log.d(TAG, "totalMem: ${mem.totalMem}  ${mem.totalMem / 1000.0f / 1000.0f}")
        Log.d(TAG, "availMem: ${mem.availMem}  ${mem.availMem / 1000.0f / 1000.0f}")
        Log.d(TAG, "threshold: ${mem.threshold}    ${mem.threshold / 1000.0f / 1000.0f}")
        val rt = Runtime.getRuntime()
        Log.d(TAG, "maxMemory: ${rt.maxMemory()}")
        Log.d(TAG, "freeMemory: ${rt.freeMemory()}")
    }

    // Get a MemoryInfo object for the device's current memory status.
    private fun getAvailableMemory(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }
}