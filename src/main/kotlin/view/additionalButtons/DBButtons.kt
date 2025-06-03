package view.additionalButtons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.screen.MainScreenViewModel
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch

@Composable
fun DBButtons(
    viewModel: MainScreenViewModel,
    showNeo4jSaveUploadButton: MutableState<Boolean>,
    showSQLiteSaveUploadButton: MutableState<Boolean>,
    showUploadSaveButtons: MutableState<Boolean>,
    showNeo4jScreen: MutableState<Boolean>,
) {
    val coroutine = rememberCoroutineScope()

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
                modifier = Modifier.fillMaxWidth().testTag("sqliteSectionButton")
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
                        modifier = Modifier.fillMaxWidth().testTag("sqliteOpenAsButton")
                    ) {
                        Text("Open as")
                    }
                    Button(
                        onClick = { viewModel.openSaveAsSQLiteDialog() },
                        modifier = Modifier.fillMaxWidth().testTag("sqliteSaveAsButton")
                    ) {
                        Text("Save as")
                    }
                    Button(
                        onClick = { viewModel.saveToCurrentSQLiteFile() },
                        enabled = viewModel.currentSQLiteDbPath.value != null,
                        modifier = Modifier.fillMaxWidth().testTag("sqliteSaveButton")
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
                        onClick = {
                            coroutine.launch {
                                viewModel.uploadGraph()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Graph")
                    }
                    Button(
                        onClick = {
                            coroutine.launch {
                                viewModel.saveToNeo4j()
                            }
                        },
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
                            coroutine.launch {
                                viewModel.clearNeo4jDatabase()
                            }
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