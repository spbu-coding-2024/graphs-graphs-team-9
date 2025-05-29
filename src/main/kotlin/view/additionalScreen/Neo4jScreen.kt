package viewModel.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Recomposer

@Composable
fun diologistNeo4j(
    showNeo4j: MutableState<Boolean>,
    showSaveClearButton: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
//    uri: State<String?>,
//    username: State<String?>,
//    password: State<String?>,
) {

    val uri = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = showNeo4j.value,
    ) {
        AlertDialog(
            modifier = Modifier.padding(5.dp),
            onDismissRequest = { showNeo4j.value = false },
            title = { Text("Neo4j Connection") },
            text = {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
//                    try {
                        OutlinedTextField(
                            value = uri.value ?: "",
                            onValueChange = {
                                uri.value = it
                                viewModel.setUri(it)
                            },
                            label = { Text("URI") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = username.value ?: "",
                            onValueChange = {
                                username.value = it
                                viewModel.setUsername(it)
                            },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password.value ?: "",
                            onValueChange = {
                                password.value = it
                                viewModel.setPassword(it)
                            },
                            label = { Text("Password") },
//                                    visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
//                    } catch (e: Exception) {
//                        throw Exception("Failed to write graph", e)
//                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.runNeo4j()
                        showSaveClearButton.value = true
                        showNeo4j.value = false
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.createGraph(true, false)
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