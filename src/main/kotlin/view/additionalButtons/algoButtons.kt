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
fun algoButton(
    viewModel: MainScreenViewModel,
    showAlgoButtons: MutableState<Boolean>,
    showFordBellman: MutableState<Boolean>,
    showDijkstraScreen: MutableState<Boolean>,
    ) {
    AnimatedVisibility(
        visible = showAlgoButtons.value,
    ) {
        Column(
            modifier = Modifier
                .absolutePadding(left = 8.dp, right = 8.dp)
        ) {

            Button(
                onClick = {showDijkstraScreen.value = !showDijkstraScreen.value},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dijkstra")
            }
            Button(
                onClick = viewModel::runFindBridge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Bridges")
            }
            Button(
                onClick = { showFordBellman.value = !showFordBellman.value },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ford Bellman")
            }
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tarjan")
            }
        }
    }
}