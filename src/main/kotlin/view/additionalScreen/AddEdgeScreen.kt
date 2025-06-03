package view.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistAddEdgeScreen(
    showAddEdge: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val firstVertex = mutableStateOf("")
    val secondVertex = mutableStateOf("")
    val weight =  mutableStateOf("")

    AnimatedVisibility(
        visible = showAddEdge.value,
    ) {
        AlertDialog(
            modifier = Modifier.width(250.dp).padding(5.dp),
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = { showAddEdge.value = false },
            title = { Text("Edge") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = firstVertex.value,
                        onValueChange = {
                            firstVertex.value = it
                            viewModel.setStartVertex(it)
                        },
                        label = { Text("First vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier)
                    OutlinedTextField(
                        value = secondVertex.value,
                        onValueChange = {
                            secondVertex.value = it
                            viewModel.setEndVertex(it)
                        },
                        label = { Text("Second vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    if (viewModel.graphViewModel.isWeighted()) {
                        Spacer(modifier = Modifier)
                        OutlinedTextField(
                            value = weight.value,
                            onValueChange = {
                                weight.value = it
                                it.toDoubleOrNull()?.let { doubleValue ->
                                    viewModel.setWidthVertex(doubleValue)
                                }
                            },
                            label = { Text("Weight") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                    if (errorMessage.value != null) {
                        Text(
                            text = errorMessage.value?: "",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = {
                            if (!viewModel.graphViewModel.isWeighted() ||
                                weight.value.toDoubleOrNull() != null) {
                                viewModel.addEdge()
                                showAddEdge.value = false
                            } else {
                                errorMessage.value = "Please enter a valid weight"
                            }
                        },
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                Row {
                    Button(
                        onClick = {
                            showAddEdge.value = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
