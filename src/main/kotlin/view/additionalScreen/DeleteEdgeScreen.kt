package view.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistDeleteEdgeScreen(
    showDeleteEdge: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {
    val firstVertex = mutableStateOf<String?>(null)
    val secondVertex = mutableStateOf<String?>(null)

    AnimatedVisibility(
        visible = showDeleteEdge.value,
    ) {
        AlertDialog(
            modifier = Modifier.width(250.dp).padding(5.dp)
//                .wrapContentHeight()
            ,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = { showDeleteEdge.value = false },
            title = { Text("Edge") },
            text = {
//                Spacer(modifier = Modifier.height(80.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = firstVertex.value ?: "",
                        onValueChange = {
                            firstVertex.value = it
                            viewModel.setStartVertex(it)
                        },
                        label = { Text("First vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier)
                    OutlinedTextField(
                        value = secondVertex.value ?: "",
                        onValueChange = {
                            secondVertex.value = it
                            viewModel.setEndVertex(it)
                        },
                        label = { Text("Second vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = {
                            viewModel.delEdge()
                            showDeleteEdge.value = false
                        },
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                Row {
                    Button(
                        onClick = {
                            showDeleteEdge.value = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
