package view.additionalScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import viewModel.screen.MainScreenViewModel
import javax.swing.JFileChooser
import javax.swing.UIManager
import java.io.File

@Composable
fun SaveAsSQLiteDialog(
        showDialog: Boolean,
        viewModel: MainScreenViewModel,
        onDismissRequest: () -> Unit
) {
    if (showDialog) {
        LaunchedEffect(Unit) {
            try {
                for (info in UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.getName()) {
                        UIManager.setLookAndFeel(info.getClassName())
                        break
                    }
                }
            } catch (e: Exception) {
                println("Nimbus L&F not found, using default for JFileChooser.")
            }
        }

        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                    modifier = Modifier.width(480.dp).wrapContentHeight(),
                    shape = MaterialTheme.shapes.large
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                            text = "Save SQLite Database As",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 20.dp).align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                            value = viewModel.saveAsFileName.value,
                            onValueChange = { viewModel.setSaveAsFileName(it) },
                            label = { Text("File Name (e.g., mygraph.db)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                                value = viewModel.saveAsDirectoryPath.value ?: "No directory selected",
                                onValueChange = { /* Read-only */ },
                                label = { Text("Directory Path") },
                                readOnly = true,
                                singleLine = true,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Button(onClick = {
                            val currentDir = viewModel.saveAsDirectoryPath.value?.let { File(it) }
                                    ?: File(System.getProperty("user.home"))

                            val fileChooser = JFileChooser(currentDir).apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Select Directory to Save Database"
                            }
                            val result = fileChooser.showSaveDialog(null)
                            if (result == JFileChooser.APPROVE_OPTION) {
                                viewModel.setSaveAsDirectoryPath(fileChooser.selectedFile.absolutePath)
                            }
                        }) {
                            Text("Browse...")
                        }
                    }

                    val fullPathPreview = remember(viewModel.saveAsDirectoryPath.value, viewModel.saveAsFileName.value) {
                        val dir = viewModel.saveAsDirectoryPath.value
                        val name = viewModel.saveAsFileName.value
                        if (!dir.isNullOrBlank() && name.isNotBlank()) {
                            val finalName = if (!name.contains(".")) "$name.db" else name
                            "Will save to: ${File(dir, finalName).absolutePath}"
                        } else {
                            "Select directory and enter file name."
                        }
                    }
                    Text(
                            text = fullPathPreview,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                    )


                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                                onClick = {
                                    viewModel.confirmSaveAsSQLite()
                                },
                                enabled = !viewModel.saveAsDirectoryPath.value.isNullOrBlank() &&
                                        viewModel.saveAsFileName.value.isNotBlank()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
