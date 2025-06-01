package viewModel.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistDijkstraScreen(
    showDijkstraScreen: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {

    val startName = mutableStateOf<String?>(null)
    val endName = mutableStateOf<String?>(null)
    AnimatedVisibility(
        visible = showDijkstraScreen.value,
    ) {
        AlertDialog(
            modifier = Modifier.padding(5.dp),
            onDismissRequest = { showDijkstraScreen.value = false },
            title = { Text("Dijkstra") },
            text = {
                Column {
                    OutlinedTextField(
                        value = startName.value ?: "",
                        onValueChange = {
                            viewModel.setStart(it)
                            startName.value = it },
                        label = { Text("Start vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = endName.value ?: "",
                        onValueChange = {
                            viewModel.setEnd(it)
                            endName.value = it
                                        },
                        label = { Text("End Vertex") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.runDijkstra()
                        showDijkstraScreen.value = false
                    }
                ) {
                    Text("Find")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDijkstraScreen.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
