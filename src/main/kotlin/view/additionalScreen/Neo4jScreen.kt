package viewModel.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun diologistNeo4j(
    showNeo4j: MutableState<Boolean>,
    showSaveClearButton: MutableState<Boolean>,
) {
    AnimatedVisibility(
        visible = showNeo4j.value,
    ) {
        AlertDialog(
            modifier = Modifier.padding(5.dp),
            onDismissRequest = { showNeo4j.value = false },
            title = { Text("Neo4j Connection") },
            text = {
//                Spacer(modifier = Modifier.height(8.dp))
//                Column {
//                    try {
//                        OutlinedTextField(
//                            value = uri.value ?: throw Exception(),
//                            onValueChange = { uri.value },
//                            label = { Text("URI") },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = username.value ?: throw Exception(),
//                            onValueChange = { username.value },
//                            label = { Text("Username") },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = password.value ?: throw Exception(),
//                            onValueChange = { password.value },
//                            label = { Text("Password") },
////                                    visualTransformation = PasswordVisualTransformation(),
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    } catch (e: Exception) {
//                        throw Exception("Failed to write graph", e)
//                    }
//                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveClearButton.value = !showSaveClearButton.value
                        showNeo4j.value = false
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showSaveClearButton.value = false
                        showNeo4j.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}