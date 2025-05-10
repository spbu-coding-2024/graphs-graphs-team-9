package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.graph.GraphView
import viewModel.MainScreenViewModel

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    Row(
//        horizontalArrangement = Arrangement.spacedBy(20.dp)
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .padding(8.dp)) {
            Row {
                Checkbox(checked = viewModel.showVerticesLabels,
//                    colors = CheckboxDefaults.colors(
//                        checkedColor = MaterialTheme.colors.primary,
//                        uncheckedColor = Color.Gray,
//                        checkmarkColor = Color.Black
//                    ),
                    onCheckedChange = {
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
        }

        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        Column(
            modifier = Modifier.weight(3f),
        ) {
            GraphView(viewModel.graphViewModel)
        }

    }
}
