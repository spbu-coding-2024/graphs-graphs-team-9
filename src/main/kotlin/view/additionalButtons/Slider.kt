package view.additionalButtons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.screen.MainScreenViewModel

@Composable
fun VertexSizeSlider(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Vertex Size",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 2.dp),
        )

        Slider(
            value = viewModel.vertexSize.value,
            onValueChange = { newValue ->
                viewModel.updateVertexSize(newValue)
            },
            valueRange = 10f..50f,
            steps = 39,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
