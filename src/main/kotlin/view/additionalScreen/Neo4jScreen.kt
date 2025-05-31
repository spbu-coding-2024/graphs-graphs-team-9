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
//                                    visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
//                    } catch (e: Exception) {
//                        throw Exception("Failed to write graph", e)
//                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Row {
                        Row(modifier = Modifier.fillMaxWidth(0.5f).clickable(onClick = {isDirected.value = !isDirected.value})) {
                            Switch(
                                checked = isDirected.value,
                                onCheckedChange = { isDirected.value = it })

                            Text(modifier = Modifier.fillMaxWidth().padding(12.dp), text = "Directed")
                        }
                        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = {isWighted.value = !isWighted.value})) {
                            Switch(
                                checked = isWighted.value,
                                onCheckedChange = { isWighted.value = it })

                            Text(modifier = Modifier.fillMaxWidth().padding(12.dp), text = "Weighted")
                        }
                    }
                }
            },
            confirmButton = {
//                if (uri.value == "" || username.value == "" || password.value == ""){
//
//                }

                Button(
                    onClick = {
                        try {
                            viewModel.setIsDirect(isDirected.value)
                            viewModel.setIsWeight(isWighted.value)
                            viewModel.runNeo4j()
                            showSaveClearButton.value = true
                            showNeo4j.value = false
                        } catch (e: Exception) {
                            showSaveClearButton.value = false
                            viewModel.handleError(e)
                        }
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