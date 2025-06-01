package view.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import viewModel.graph.EdgeViewModel

@Composable
fun EdgeView(
    viewModel: EdgeViewModel,
    modifier: Modifier = Modifier,
    isDirect: Boolean,
    vertexSize: Float,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val  start = Offset(
            viewModel.u.x.toPx() + viewModel.u.radius.toPx(),
            viewModel.u.y.toPx() + viewModel.u.radius.toPx(),
        )
        val end = Offset(
            viewModel.v.x.toPx() + viewModel.v.radius.toPx(),
            viewModel.v.y.toPx() + viewModel.v.radius.toPx(),)
        drawLine(
            start = start,
            end = end,
            color = viewModel.color
        )
        if (isDirect) {
            drawArrow(
                start = start,
                end = end,
                arrowSize = 20f,
                color = viewModel.color,
                vertexSize
            )
        }

    }
    if (viewModel.labelVisible) {
        Text(
            modifier = Modifier
                .offset(
                    viewModel.u.x + (viewModel.v.x - viewModel.u.x) / 2,
                    viewModel.u.y + (viewModel.v.y - viewModel.u.y) / 2
                ),
            text = (viewModel.width ?: "").toString(),
        )
    }
}
private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    arrowSize: Float,
    color: Color,
    size: Float
) {
    val mid = Offset(
        (start.x + end.x) / 2,
        (start.y + end.y) / 2
    )

    val direction = Offset(end.x - start.x, end.y - start.y)
    val length = kotlin.math.sqrt(direction.x * direction.x + direction.y * direction.y)

    val normalizedDirection = if (length > 0) {
        Offset(direction.x / length, direction.y / length)
    } else {
        Offset(1f, 0f)
    }

    val perpendicular = Offset(-normalizedDirection.y, normalizedDirection.x)

    val arrowHead = mid
    val arrowPoint1 = Offset(
        mid.x - arrowSize * normalizedDirection.x + arrowSize * 0.5f * perpendicular.x,
        mid.y - arrowSize * normalizedDirection.y + arrowSize * 0.5f * perpendicular.y
    )
    val arrowPoint2 = Offset(
        mid.x - arrowSize * normalizedDirection.x - arrowSize * 0.5f * perpendicular.x,
        mid.y - arrowSize * normalizedDirection.y - arrowSize * 0.5f * perpendicular.y
    )
    drawPath(
        path = Path().apply {
            moveTo(arrowHead.x, arrowHead.y)
            lineTo(arrowPoint1.x, arrowPoint1.y)
            lineTo(arrowPoint2.x, arrowPoint2.y)
            close()
        },
        color = color
    )
}
