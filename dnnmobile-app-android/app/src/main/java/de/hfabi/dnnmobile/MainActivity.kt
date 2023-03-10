package de.hfabi.dnnmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.hfabi.dnnmobile.benchmark.ModelConfig
import de.hfabi.dnnmobile.benchmark.TfBenchmark
import de.hfabi.dnnmobile.ui.theme.DnnmobileTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DnnmobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var context = LocalContext.current
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "On-Device Training")
                        Divider()
                        Text(text = "Breast Cancer Wisconsin")
                        Row {
                            Button(onClick = { trainingTfBreastCancerWisconsin() }) {
                                Text(text = "TfLite - Train")
                            }
                            Button(onClick = { inferenceTfBreastCancerWisconsin() }) {
                                Text(text = "TfLite - Infer")
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }

    private fun trainingTfBreastCancerWisconsin() {
        lifecycleScope.launch(Dispatchers.Default) {
            val benchmark = TfBenchmark(this@MainActivity,
                modelPath = ModelConfig.Model.TensorFlow.BreastCancerWisconsin,
                checkpointPath = "checkpointBCW",
                numberOfEpochs = 10,
                batchSize = 1,
                dataset= ModelConfig.BreastCancerWisconsin)
            benchmark.training()
            benchmark.inferenceWithTrainedWeights(true)
        }
    }
    private fun inferenceTfBreastCancerWisconsin() {
        lifecycleScope.launch(Dispatchers.Default) {
            val benchmark = TfBenchmark(this@MainActivity,
                modelPath = ModelConfig.Model.TensorFlow.BreastCancerWisconsin,
                checkpointPath = "checkpointBCW",
                numberOfEpochs = 10,
                batchSize = 1,
                dataset= ModelConfig.BreastCancerWisconsin)
            benchmark.inferenceWithTrainedWeights(true)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DnnmobileTheme {

    }
}