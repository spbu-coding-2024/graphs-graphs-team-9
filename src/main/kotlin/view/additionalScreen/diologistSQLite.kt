package viewModel.additionalScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import viewModel.screen.MainScreenViewModel

@Composable
fun diologistSQLite(
        showSQLiteDialog: MutableState<Boolean>,
        showSQLiteSaveClearButtonsPanel: MutableState<Boolean>,
        viewModel: MainScreenViewModel
) {
    if (showSQLiteDialog.value) {
        Dialog(
                onDismissRequest = { showSQLiteDialog.value = false }
        ) {
            Surface(
                    modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text = "SQLite Database Configuration",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                            value = viewModel.dbPath.value ?: "",
                            onValueChange = { viewModel.setDbPath(it) },
                            label = { Text("Database Path") },
                            placeholder = { Text("path/to/database.db") },
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                                onClick = {
                                    viewModel.initializeSQLiteDB()
                                }
                        ) {
                            Text("Initialize DB")
                        }

                        Button(
                                onClick = {
                                    showSQLiteDialog.value = false
                                    showSQLiteSaveClearButtonsPanel.value = true
                                }
                        ) {
                            Text("Connect")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                            onClick = { showSQLiteDialog.value = false }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}