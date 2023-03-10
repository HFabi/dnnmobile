package de.hfabi.dnnmobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ContentView(viewModel: ContentViewModel) {
    var context = LocalContext.current
    Column(Modifier.padding(16.dp)) {
        Text(text = "On-Device Training")
        Divider()
        Text(text = "Breast Cancer Wisconsin")
        Row {
            Button(onClick = { viewModel.trainingTfBreastCancerWisconsin(context) }) {
                Text(text = "TfLite - Train")
            }
            Button(onClick = { viewModel.inferenceTfBreastCancerWisconsin(context) }) {
                Text(text = "TfLite - Infer")
            }
        }
        Divider()
    }
}