package viewModel.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.graph.Vertex

class VertexViewModel(
    x: Dp = 0.dp,
    y: Dp = 0.dp,
    color: Color,
    private val v: Vertex,
    val _labelVisible: State<Boolean>,
) {
    val Id
        get() = v.id

    private val _radius = mutableStateOf(25.dp)
    var radius: Dp
        get() = _radius.value
        set(value) {
            _radius.value = value
        }

    var relativeSizeFactor: Float = 1.0f

    private var _x = mutableStateOf(x)
    var x: Dp
        get() = _x.value
        set(value) {
            _x.value = value
        }
    private var _y = mutableStateOf(y)
    var y: Dp
        get() = _y.value
        set(value) {
            _y.value = value
        }
    private var _color = mutableStateOf(color)
    var color: Color
        get() = _color.value
        set(value) {
            _color.value = value
        }

    val vertex
        get() = v

    val labelVisible
        get() = _labelVisible.value

    fun onDrag(offset: Offset) {
        _x.value += offset.x.dp
        _y.value += offset.y.dp
    }
}
