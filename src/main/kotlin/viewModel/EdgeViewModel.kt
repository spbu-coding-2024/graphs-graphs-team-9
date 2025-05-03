package viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import model.graph.Edge

class EdgeViewModel (
    val u: VertexViewModel,
    val v: VertexViewModel,
    color: Color,
    private val e: Edge,
    private val _labelVisible: State<Boolean>,
    private val _weightVisible: State<Boolean>,
    width: Double?,
) {
    val label
        get() = e.num.toString()

    val labelVisible
        get() = _labelVisible.value

    val Id
        get() = e.num

    private var _color = mutableStateOf(color)
    var color: Color
        get() = _color.value
        set(value) {
            _color.value = value
        }

    private var _width = mutableStateOf(width)
    var width: Double?
        get() = _width.value
        set(value) {
            _width.value = value
        }
}