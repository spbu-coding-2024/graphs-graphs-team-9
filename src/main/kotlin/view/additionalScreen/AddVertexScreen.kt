package view.additionalScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun diologistAddVertexScreen(
    showAddVertex: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {
    val vertex = mutableStateOf("")
    AnimatedVisibility(
        visible = showAddVertex.value,
    ) {
        AlertDialog(
            modifier = Modifier.padding(5.dp).width(250.dp),
            onDismissRequest = { showAddVertex.value = false },
            title = { Text("Vertex") },
            text = {
                Column {
                    OutlinedTextField(
                        value = vertex.value ?: "",
                        onValueChange = {
                            vertex.value = it
                            viewModel.setVertex(it) },
                        label = { Text("Vertex") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addVertex()
                        showAddVertex.value = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAddVertex.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
