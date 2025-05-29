package view.additionalButtons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Switch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.screen.MainScreenViewModel

@Composable
fun switch(
    viewModel: MainScreenViewModel,
    showLabels: MutableState<Boolean>,
) {
    Row(
        modifier = Modifier.clickable(onClick = { viewModel.showVerticesLabels = !viewModel.showVerticesLabels })
            .fillMaxWidth()
    ) {
        Switch(
            checked = viewModel.showVerticesLabels,
            onCheckedChange = { viewModel.showVerticesLabels = it }
        )
        Text(
            text = if (viewModel.graphViewModel.isWeighted())  "Vertexes and edges" else "Vertexes",
            fontSize = if (viewModel.graphViewModel.isWeighted()) 14.sp else 18.sp,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
//            textAlign = TextAlign.Center
        )
    }
}