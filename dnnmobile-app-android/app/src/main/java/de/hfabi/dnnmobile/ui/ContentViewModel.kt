package de.hfabi.dnnmobile.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hfabi.dnnmobile.benchmark.ModelConfig
import de.hfabi.dnnmobile.benchmark.TfBenchmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentViewModel : ViewModel() {

    fun trainingTfBreastCancerWisconsin(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            val benchmark = TfBenchmark(context,
                modelPath = ModelConfig.Model.TensorFlow.BreastCancerWisconsin,
                checkpointPath = "checkpointBCW",
                numberOfEpochs = 10,
                batchSize = 1,
                dataset= ModelConfig.BreastCancerWisconsin)
            benchmark.training()
            benchmark.inferenceWithTrainedWeights(true)
        }
    }

    fun inferenceTfBreastCancerWisconsin(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            val benchmark = TfBenchmark(
                context,
                modelPath = ModelConfig.Model.TensorFlow.BreastCancerWisconsin,
                checkpointPath = "checkpointBCW",
                numberOfEpochs = 10,
                batchSize = 1,
                dataset = ModelConfig.BreastCancerWisconsin
            )
            benchmark.inferenceWithTrainedWeights(true)
        }
    }
}