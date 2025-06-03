package viewModel.additionalScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.launch

@Composable
fun diologistNeo4j(
    showNeo4j: MutableState<Boolean>,
    showSaveClearButton: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {
    val uri = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isDirected = remember { mutableStateOf(false) }
    val isWighted = remember { mutableStateOf(false) }
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

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
                    OutlinedTextField(
                        value = uri.value,
                        onValueChange = {
                            uri.value = it
                            viewModel.setUri(it)
                        },
                        label = { Text("URI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username.value,
                        onValueChange = {
                            username.value = it
                            viewModel.setUsername(it)
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = {
                            password.value = it
                            viewModel.setPassword(it)
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f)
                                .clickable(onClick = { isDirected.value = !isDirected.value })
                        ) {
                            Switch(
                                checked = isDirected.value,
                                onCheckedChange = { isDirected.value = it })

                            Text(modifier = Modifier.fillMaxWidth().padding(12.dp), text = "Directed")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable(onClick = { isWighted.value = !isWighted.value })
                        ) {
                            Switch(
                                checked = isWighted.value,
                                onCheckedChange = { isWighted.value = it })

                            Text(modifier = Modifier.fillMaxWidth().padding(12.dp), text = "Weighted")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            uri.value.isBlank() -> {
                                errorMessage.value = "URI cannot be empty"
                                showErrorDialog.value = true
                            }

                            username.value.isBlank() -> {
                                errorMessage.value = "Username cannot be empty"
                                showErrorDialog.value = true
                            }

                            password.value.isBlank() -> {
                                errorMessage.value = "Password cannot be empty"
                                showErrorDialog.value = true
                            }

                            else -> {
                                try {
                                    viewModel.setIsDirect(isDirected.value)
                                    viewModel.setIsWeight(isWighted.value)
                                    coroutineScope.launch {
                                        viewModel.runNeo4j()
                                    }
                                    showSaveClearButton.value = true
                                    showNeo4j.value = false
                                } catch (e: Exception) {
                                    showSaveClearButton.value = false
                                    viewModel.handleError(e)
                                }
                            }
                        }
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
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = { Text("Error") },
            text = { Text(errorMessage.value) },
            confirmButton = {
                Button(onClick = { showErrorDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}