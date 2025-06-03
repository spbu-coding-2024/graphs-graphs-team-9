package viewModel.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistFordBellman(
    showFordBellman: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
    showResult: MutableState<Boolean>
) {

    val startName = mutableStateOf<String?>(null)
    val endName = mutableStateOf<String?>(null)
    AnimatedVisibility(
        visible = showFordBellman.value,
    ) {
        AlertDialog(
            modifier = Modifier.padding(5.dp),
            onDismissRequest = { showFordBellman.value = false },
            title = { Text("Ford Bellman") },
            text = {
                Column {
                    OutlinedTextField(
                        value = startName.value ?: "",
                        onValueChange = {
                            viewModel.setStart(it)
                            startName.value = it
                        },
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
                        viewModel.runFordBellman()
                        showResult.value = if (viewModel.graphViewModel.isWeighted()) true else false
                        showFordBellman.value = false
                    }
                ) {
                    Text("Find")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showFordBellman.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}