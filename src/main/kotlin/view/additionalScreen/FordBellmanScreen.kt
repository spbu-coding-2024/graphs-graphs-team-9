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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistFordBellman(
    showFordBellman: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
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
                        onValueChange = { startName.value = it },
                        label = { Text("Start vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = endName.value ?: "",
                        onValueChange = { endName.value = it },
                        label = { Text("End Vertex") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
//                        viewModel::runFordBellman
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