package de.hfabi.dnnmobile.benchmark

import android.content.Context
import de.hfabi.dnnmobile.DnnLogger
import de.hfabi.dnnmobile.utils.MlUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.MappedByteBuffer

class TfBenchmark(
    var context: Context,
    var modelPath: String,
    var checkpointPath: String,
    var numberOfEpochs: Int,
    var batchSize: Int,
    var dataset: ModelConfig.Dataset
) : BaseBenchmark() {

    private val numberOfBatches = dataset.trainSampleCount / batchSize

    fun training() {
        DnnLogger.logD("--- TfBreastCancer training ---")
        DnnLogger.logMemory(context)

        timer.notifySetupStarted()
        val trainInput = MlUtils.readFromAssetCSV(
            context,
            dataset.trainInput,
            dataset.featuresInput * dataset.trainSampleCount
        )
        val trainTarget = MlUtils.readFromAssetCSV(
            context,
            dataset.trainTarget,
            dataset.featuresTarget * dataset.trainSampleCount
        )
        val testInput = MlUtils.readFromAssetCSV(
            context,
            dataset.testInput,
            dataset.featuresInput * dataset.testSampleCount
        )
        val testTarget = MlUtils.readFromAssetCSV(
            context,
            dataset.testTarget,
            dataset.featuresTarget * dataset.testSampleCount
        )
        DnnLogger.logD("data loaded")
        DnnLogger.logMemory(context)

        val compatList = CompatibilityList()
        val options = Interpreter.Options().apply {
            /*if(compatList.isDelegateSupportedOnThisDevice){
                // if the device has a supported GPU, add the GPU delegate
                val delegateOptions = compatList.bestOptionsForThisDevice
                this.addDelegate(GpuDelegate(delegateOptions))
            } else {
                // if the GPU is not supported, run on 4 threads
                this.setNumThreads(4)
            }*/
        }

        var interpreter: Interpreter? = null
        val tfliteModelBuffer: MappedByteBuffer = MlUtils.getModelByteBuffer(context, modelPath)
        interpreter = Interpreter(tfliteModelBuffer, options)


        // Prepare training batches for TF Lite
        val trainInputBatches: MutableList<FloatBuffer> = ArrayList(numberOfBatches)
        val trainLabelBatches: MutableList<FloatBuffer> = ArrayList(numberOfBatches)
        for (i in 0 until numberOfBatches) {
            val trainInputFloatBuffer: FloatBuffer =
                ByteBuffer.allocateDirect(batchSize * dataset.featuresInput * SIZE_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            val trainTargetFloatBuffer: FloatBuffer =
                ByteBuffer.allocateDirect(batchSize * dataset.featuresTarget * SIZE_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()

            for (j in 0 until batchSize * dataset.featuresInput) {
                trainInputFloatBuffer.put(trainInput[i * batchSize + j])
            }
            for (j in 0 until batchSize * dataset.featuresTarget) {
                trainTargetFloatBuffer.put(trainTarget[i * batchSize + j])
            }
            // Fill the data values...
            trainInputBatches.add(trainInputFloatBuffer.rewind() as FloatBuffer)
            trainLabelBatches.add(trainTargetFloatBuffer.rewind() as FloatBuffer)
        }

        // prepare test data for TF lite
        val testInputFloatBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(dataset.testSampleCount * dataset.featuresInput * SIZE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        val testTargetFloatBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(dataset.testSampleCount * dataset.featuresTarget * SIZE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        testInputFloatBuffer.put(testInput, 0, dataset.testSampleCount * dataset.featuresInput)
        testTargetFloatBuffer.put(testTarget, 0, dataset.testSampleCount * dataset.featuresTarget)

        testInputFloatBuffer.rewind()
        testTargetFloatBuffer.rewind()

        timer.notifySetupStopped()

        timer.notifyTrainingStarted()
        val losses = FloatArray(numberOfEpochs)
        val lossesOnTestData = FloatArray(numberOfEpochs)
        val accuraciesOnTestData = FloatArray(numberOfEpochs)
        for (epochIdx in (0 until numberOfEpochs)) {
            for (batchIdx in (0 until numberOfBatches)) {
                val inputs: MutableMap<String, Any> = HashMap()
                inputs["x_train"] = trainInputBatches[batchIdx]
                inputs["y_train"] = trainLabelBatches[batchIdx]

                val outputs: MutableMap<String, Any> = HashMap()
                val loss = FloatBuffer.allocate(1)
                outputs["loss"] = loss

                interpreter.runSignature(inputs, outputs, "train")

                if (batchIdx == numberOfBatches - 1)
                    losses[epochIdx] = loss.get(0)
            }
            val evaluateInputs: MutableMap<String, Any> = HashMap()
            evaluateInputs["x"] = testInputFloatBuffer
            evaluateInputs["y"] = testTargetFloatBuffer

            val evaluateOutputs: MutableMap<String, Any> = HashMap()
            val evaluateLoss = FloatBuffer.allocate(1)
            evaluateOutputs["loss"] = evaluateLoss
            val evaluateAccuracy = FloatBuffer.allocate(1)
            evaluateOutputs["accuracy"] = evaluateAccuracy

            interpreter.runSignature(evaluateInputs, evaluateOutputs, "evaluate")

            lossesOnTestData[epochIdx] = evaluateLoss.get(0)
            accuraciesOnTestData[epochIdx] = evaluateAccuracy.get(0)
            // Print the loss output for every 10 epochs.
            if ((epochIdx + 1) % 1 == 0) {
                DnnLogger.logD("Epoch ${epochIdx + 1}: loss: ${losses[epochIdx]}  ${lossesOnTestData[epochIdx]}, accuracy: ${accuraciesOnTestData[epochIdx]}")
            }
        }
        timer.notifyTrainingStopped()

        // Export the trained weights as a checkpoint file.
        // Export the trained weights as a checkpoint file.
        val outputFile = File(context.filesDir, checkpointPath)

        val inputs: MutableMap<String, Any> = HashMap()
        inputs["checkpoint_path"] = outputFile.absolutePath
        val outputs: Map<String, Any> = HashMap()
        interpreter.runSignature(inputs, outputs, "save")
        DnnLogger.logD("saved weigths")

        DnnLogger.apply {
            logSpacer()
            logTime(timer)
            logTrainingResult(losses.last(), lossesOnTestData.last())
            logSpacer()
        }

        interpreter.close()
    }

    /**
     *
     */
    fun inferenceWithTrainedWeights(withTrained: Boolean) {
        DnnLogger.logD("runInferenceWithTrainedWeights")
        // Any time you create an interpreter from a TFLite model,
        // the interpreter will initially load the original model weights.
        // So after you've done some training and saved a checkpoint file,
        // you'll need to run the restore signature method to load the checkpoint.

        var interpreter: Interpreter?
        val tfliteModelBuffer: MappedByteBuffer = MlUtils.getModelByteBuffer(context, modelPath)
        interpreter = Interpreter(tfliteModelBuffer)

        if (withTrained) {
            interpreter.allocateTensors()
            DnnLogger.logD("in code for restoring weights")
            // restore weights (e.g. for continue training or inference)
            // Load the trained weights from the checkpoint file.
            val outputFile: File = File(context.filesDir, checkpointPath)
            DnnLogger.logD("FILE::  ${outputFile.exists()}")
            DnnLogger.logD("FILE::  ${outputFile.absolutePath}")
            DnnLogger.logD("FILE::  ${outputFile.totalSpace}")
            val restoreInputs: MutableMap<String, Any> = HashMap()
            restoreInputs["checkpoint_path"] = outputFile.absolutePath
            val restoreOutputs: Map<String, Any> = HashMap()
            interpreter.runSignature(restoreInputs, restoreOutputs, "restore")
            DnnLogger.logD("RESTORE OUTPUTS::  ${restoreOutputs}")
            DnnLogger.logD("RESTORE OUTPUTS::  ${restoreOutputs.size}")
        }

        // run inference with restored weights
        val NUM_TESTS = 100
        val testInputFloatBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(NUM_TESTS * dataset.featuresInput * SIZE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        val testLabelsList: MutableList<Int> = mutableListOf()
        val output: FloatBuffer =
            ByteBuffer.allocateDirect(NUM_TESTS * dataset.featuresTarget * SIZE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        // Fill the test data.
        val testInput = MlUtils.readFromAssetCSV(
            context,
            dataset.testInput,
            dataset.testSampleCount * dataset.featuresInput
        )
        val testTarget = MlUtils.readFromAssetCSV(
            context,
            dataset.testTarget,
            dataset.testSampleCount * dataset.featuresTarget
        )

        for (j in 0 until dataset.featuresInput * NUM_TESTS) {
            testInputFloatBuffer.put(testInput[j])

        }
        for (j in 0 until dataset.featuresTarget * NUM_TESTS) {
            testLabelsList.add(testTarget[j].toInt())
        }

        // Run the inference.
        val inputs: MutableMap<String, Any> = HashMap()
        inputs["x_infer"] = testInputFloatBuffer.rewind()
        val outputs: MutableMap<String, Any> = HashMap()
        outputs["logits"] = output
        interpreter.allocateTensors()
        timer.notifyInferenceStarted()
        interpreter.runSignature(inputs, outputs, "infer")
        timer.notifyInferenceStopped()
        output.rewind()

        DnnLogger.apply {
            logSpacer()
            logTime(timer)
            logInference(output, testTarget)
        }
        output.rewind()

        interpreter.close()
    }
}