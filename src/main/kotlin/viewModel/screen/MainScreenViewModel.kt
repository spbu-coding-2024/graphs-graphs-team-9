package viewModel.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import model.graph.Graph
import model.io.Neo4j.Neo4j
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

    var graphViewModel = GraphViewModel(graph, _showVerticesLabels, _showEdgesLabels)

    init {
        representationStrategy.place(800.0, 600.0, graphViewModel.vertices)
    }

    private var _startName = mutableStateOf<String?>(null)
    val startName: State<String?>
        get() = _startName

    private var _endName = mutableStateOf<String?>(null)
    val endName: State<String?>
        get() = _endName
//        set(value) =

    private var _uri = mutableStateOf<String?>("")
    val uri: State<String?>
        get() = _uri

    private var _username = mutableStateOf<String?>("")
    val username: State<String?>
        get() = _username

    private var _password = mutableStateOf<String?>("")
    val password: State<String?>
        get() = _password

    private fun clearId(){
        _startName.value = null
        _endName.value = null
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
                startName.value ?: throw Exception("Incorrect id"),
            endName.value ?: throw Exception("Incorrect id")
            )
        }catch (e: Exception){

        }
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
    }

    fun runNeo4j(){
        graphViewModel.startNeo4j(uri.value ?: "", username.value ?: "", password.value ?: "")
    }

    fun clearGraph(){
        graphViewModel.clearGraph()
    }

    val vertexSize: State<Float>
        get() = graphViewModel.vertexSize

    fun updateVertexSize(newSize: Float) = graphViewModel.updateVertexSize(newSize)
}
