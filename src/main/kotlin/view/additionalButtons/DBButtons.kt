package view.additionalButtons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel

@Composable
fun DBButtons(
        viewModel: MainScreenViewModel,
        showNeo4jSaveUploadButton: MutableState<Boolean>,
        showSQLiteSaveUploadButton: MutableState<Boolean>,
        showUploadSaveButtons: MutableState<Boolean>,
        showNeo4jScreen: MutableState<Boolean>,
) {
    AnimatedVisibility(
            visible = showUploadSaveButtons.value,
    ) {
        Column(
                modifier = Modifier
                        .absolutePadding(left = 8.dp, right = 8.dp)
        ) {

            Button(
                    onClick = {
                        showSQLiteSaveUploadButton.value = !showSQLiteSaveUploadButton.value
                        if (showSQLiteSaveUploadButton.value) {
                            showNeo4jSaveUploadButton.value = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text("SQLite")
            }
            Button(
                    onClick = {
                        showNeo4jScreen.value = true
                        showSQLiteSaveUploadButton.value = false
                        showNeo4jSaveUploadButton.value = true
                    },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text("Neo4j")
            }

            AnimatedVisibility(
                    visible = showSQLiteSaveUploadButton.value,
            ) {
                Column {
                    Button(
                            onClick = {
                                viewModel.requestSQLiteFileOpen()
                            },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open as")
                    }
                    Button(
                            onClick = { viewModel.openSaveAsSQLiteDialog() },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save as")
                    }
                    Button(
                            onClick = { viewModel.saveToCurrentSQLiteFile() },
                            enabled = viewModel.currentSQLiteDbPath.value != null,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                }
            }

            AnimatedVisibility(
                    visible = showNeo4jSaveUploadButton.value,
            ) {
                Column {
                    Button(
                            onClick = { viewModel.uploadGraph() },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Graph")
                    }
                    Button(
                            onClick = { viewModel.saveToNeo4j() },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Graph")
                    }
                }
            }

            AnimatedVisibility(
                    visible = showSQLiteSaveUploadButton.value || showNeo4jSaveUploadButton.value,
            ) {
                Button(
                        onClick = {
                            if (showSQLiteSaveUploadButton.value) {
                                viewModel.clearGraph()
                            } else if (showNeo4jSaveUploadButton.value) {
                                viewModel.clearNeo4jDatabase()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Graph")
                }
            }
        }
    }
}
