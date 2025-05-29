package view.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import viewModel.graph.VertexViewModel

@Composable
fun VertexView(
        viewModel: VertexViewModel,
        modifier: Modifier = Modifier,
        // onDragVertex: (VertexViewModel, Dp, Dp) -> Unit
) {
    val density = LocalDensity.current
    Box(modifier = modifier
            .size(viewModel.radius * 2, viewModel.radius * 2)
            .offset(viewModel.x, viewModel.y)
            .background(
                    color = viewModel.color,
                    shape = CircleShape
            )
            .pointerInput(viewModel) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaXDp: Dp
                    val deltaYDp: Dp
                    with(density) {
                        deltaXDp = dragAmount.x.toDp()
                        deltaYDp = dragAmount.y.toDp()
                    }
                    // onDragVertex(viewModel, deltaXDp, deltaYDp)
                    viewModel.onDrag(dragAmount)
                }
            }
    ) {
        if (viewModel.labelVisible) {
            Text(
                    modifier = Modifier
                            .align(Alignment.Center)
                            .offset(0.dp, -viewModel.radius - 10.dp),
                    text = viewModel.label.name.toString(),
            )
        }
    }
}