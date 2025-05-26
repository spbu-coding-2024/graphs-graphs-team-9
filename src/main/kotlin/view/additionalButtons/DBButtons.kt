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
    showNeo4jSaveClearButton: MutableState<Boolean>,
    showSQLiteSaveClearButton: MutableState<Boolean>,
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
                    showSQLiteSaveClearButton.value = !showSQLiteSaveClearButton.value
                    showNeo4jSaveClearButton.value = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SQLite")
            }
            Button(
                onClick = {
                    showNeo4jScreen.value = true
//                    showNeo4jSaveClearButton.value = !showNeo4jSaveClearButton.value
                    showSQLiteSaveClearButton.value = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Neo4j")
            }
            AnimatedVisibility(
                visible = showSQLiteSaveClearButton.value,
            ) {
                Button(
                    onClick = { viewModel::clearGraph },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Graph")
                }
            }
            AnimatedVisibility(
                visible = showSQLiteSaveClearButton.value,
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Graph")
                }
            }


            AnimatedVisibility(
                visible = showNeo4jSaveClearButton.value,
            ) {
                Button(
                    onClick = { viewModel::clearGraph },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Graph")
                }
            }
            AnimatedVisibility(
                visible = showNeo4jSaveClearButton.value,
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Graph")
                }
            }
        }
    }
}