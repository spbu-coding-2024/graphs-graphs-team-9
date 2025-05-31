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
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

private fun showSQLiteOpenFileChooser(
        initialDirectory: String?,
        onFileSelected: (String) -> Unit
) {
    try {
        for (info in UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus" == info.getName()) {
                UIManager.setLookAndFeel(info.getClassName())
                break
            }
        }
    } catch (e: Exception) {
        // Nimbus L&F not found, using default.
    }

    val chooser = JFileChooser(initialDirectory ?: System.getProperty("user.home"))
    chooser.dialogTitle = "Open SQLite Database File"
    chooser.fileFilter = FileNameExtensionFilter("SQLite Databases (*.db, *.sqlite, *.sqlite3)", "db", "sqlite", "sqlite3")
    chooser.fileSelectionMode = JFileChooser.FILES_ONLY

    val result = chooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        onFileSelected(chooser.selectedFile.absolutePath)
    }
}


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
                        showSQLiteSaveUploadButton.value = false // Ensure SQLite buttons are hidden when Neo4j is chosen
                        showNeo4jSaveUploadButton.value = true // Show Neo4j buttons
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
                                showSQLiteOpenFileChooser(
                                        initialDirectory = viewModel.currentSQLiteDbPath.value?.let { File(it).parent }
                                                ?: System.getProperty("user.home"),
                                        onFileSelected = { filePath ->
                                            viewModel.onSQLiteFileSelectedForOpen(filePath)
                                        }
                                )
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
                            } else if (showNeo4jSaveUploadButton.value) { // check if Neo4j buttons are active
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