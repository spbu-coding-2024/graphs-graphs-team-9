package view.additionalButtons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.additionalScreen.diologistDijkstraScreen
import viewModel.additionalScreen.diologistFordBellman
import viewModel.screen.MainScreenViewModel

@Composable
fun algoButton(
    viewModel: MainScreenViewModel,
    showAlgoButtons: MutableState<Boolean>,
//    showFordBellman: MutableState<Boolean>,
//    showDijkstra: MutableState<Boolean>,
) {

    val showDijkstra = remember { mutableStateOf(false) }
    val showFordBellman = remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = showAlgoButtons.value,
    ) {
        Column(
            modifier = Modifier
                .absolutePadding(left = 8.dp, right = 8.dp)
        ) {
            if (viewModel.graphViewModel.isWeighted() && viewModel.graphViewModel.graph.getPositive()) {
                Button(
                    onClick = { showDijkstra.value = !showDijkstra.value },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dijkstra")
                }
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
                onClick = viewModel::runTarjan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tarjan")
            }
            Button(
                onClick = viewModel::runFindKey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find key vertex")
            }
        }
    }
    diologistDijkstraScreen(showDijkstra, viewModel)
    diologistFordBellman(showFordBellman, viewModel)
}