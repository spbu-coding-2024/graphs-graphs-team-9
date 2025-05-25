package viewModel.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import model.graph.Graph
import viewModel.graph.GraphViewModel


class MainScreenViewModel(graph: Graph, private val representationStrategy: RepresentationStrategy) {
    private var _showVerticesLabels = mutableStateOf(false)
    var showVerticesLabels: Boolean
        get() = _showVerticesLabels.value
        set(value) {
            _showVerticesLabels.value = value
        }

    private var _showEdgesLabels = mutableStateOf(false)
    var showEdgesLabels: Boolean
        get() = _showEdgesLabels.value
        set(value) {
            _showEdgesLabels.value = value
        }

    val graphViewModel = GraphViewModel(graph, _showVerticesLabels, _showEdgesLabels)

    init {
        representationStrategy.place(800.0, 600.0, graphViewModel.vertices)
    }

    private var _startId = mutableStateOf<String?>(null)
    val startId: State<String?>
        get() = _startId

    private var _endId = mutableStateOf<String?>(null)
    val endId: State<String?>
        get() = _endId
//        set(value) =

    private fun clearId(){
        _startId.value = null
        _endId.value = null
    }

    fun resetGraphView() {
        representationStrategy.place(800.0, 600.0, graphViewModel.vertices)
        graphViewModel.vertices.forEach { v -> v.color = Color.Gray }
    }

    fun resetColor() {
        graphViewModel.vertices.forEach { v -> v.color = Color.Gray }
        graphViewModel.edges.forEach { e -> e.color = Color.White }
    }

    fun runFordBellman() {
        resetColor()
        try {
            graphViewModel.startFordBellman(
                startId.value?.toInt() ?: throw Exception("Incorrect id"),
                endId.value?.toInt() ?: throw Exception("Incorrect id")
            )
        }catch (e: Exception){

        }
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
    }
}
