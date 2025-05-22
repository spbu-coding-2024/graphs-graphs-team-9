package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.unit.sp
import view.graph.GraphView
import viewModel.screen.MainScreenViewModel

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(modifier = Modifier.width(370.dp)) {
            Row {
                Checkbox(checked = viewModel.showVerticesLabels, onCheckedChange = {
                    viewModel.showVerticesLabels = it
                })
                Text("Show vertices labels", fontSize = 28.sp, modifier = Modifier.padding(4.dp))
            }
            Row {
                Checkbox(checked = viewModel.showEdgesLabels, onCheckedChange = {
                    viewModel.showEdgesLabels = it
                })
                Text("Show edges labels", fontSize = 28.sp, modifier = Modifier.padding(4.dp))
            }
            Button(
                onClick = viewModel::resetGraphView,
                enabled = true,
            ) {
                Text(
                    text = "Reset default settings",
                )
            }
//            Button(
//                onClick = viewModel::setVerticesColor,
//                enabled = true,
//            ) {
//                Text(
//                    text = "Set colors",
//                )
//            }
        }

        Surface(
            modifier = Modifier.weight(1f),
        ) {
            GraphView(viewModel.graphViewModel)
        }

    }
}
