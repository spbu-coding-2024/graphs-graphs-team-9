package view.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import viewModel.graph.GraphViewModel
import viewModel.toosl.CoolColors

@Composable
fun GraphView(
        viewModel: GraphViewModel,
        scale: Float,
) {
    val offset = remember { mutableStateOf(Offset.Zero) }

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(CoolColors.backgroundBasic)
    ) {
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                    onGesture = { centroid, pan, zoom, rotation ->
                                        offset.value += pan
                                    }
                            )
                        }
                        .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.value.x,
                                translationY = offset.value.y
                        )
        ) {
            viewModel.edges.forEach { e ->
                EdgeView(e, Modifier, viewModel.isDirected(), viewModel.vertexSize.value)

            }
            viewModel.vertices.forEach { v ->
                VertexView(
                        viewModel = v,
                        modifier = Modifier,
                )
            }
        }
    }
}
