package viewModel.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.graph.*
import model.io.Neo4j.Neo4j
import viewModel.graph.GraphViewModel
import viewModel.graph.VertexViewModel
import viewModel.screen.layouts.RepresentationStrategy
import kotlin.math.abs

class MainScreenViewModel(
    initialGraph: Graph,
    val representationStrategy: RepresentationStrategy
) {
    private var _showVerticesLabels = mutableStateOf(false)
    var showVerticesLabels: Boolean
        get() = _showVerticesLabels.value
        set(v) {
            _showVerticesLabels.value = v
        }

    private var _showEdgesLabels = mutableStateOf(false)
    var showEdgesLabels: Boolean
        get() = _showEdgesLabels.value
        set(v) {
            _showEdgesLabels.value = v
        }

    var graphViewModel = GraphViewModel(
        initialGraph,
        _showVerticesLabels,
        _showEdgesLabels
    )

    private var currentCanvasWidth: Double = 1000.0
    private var currentCanvasHeight: Double = 800.0

    init {
        representationStrategy.layout(800.0, 1000.0, graphViewModel)
    }

    fun updateCanvasSize(width: Double, height: Double) {
        currentCanvasWidth = width
        currentCanvasHeight = height
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }
    

    private var _vertex = mutableStateOf<String?>(null)
    val vertex: State<String?> = _vertex

    fun setVertex(name: String){
        _vertex.value = name
    }

    var i = 0
    val map: MutableMap<String?, Int> = mutableMapOf()
    fun addVertex(){
        map[vertex.value] = i
        graphViewModel.graph.addVertex(Vertex(i++, vertex.value))
        graphViewModel.updateGraph(graphViewModel.graph)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)

    }

    fun delVertex(){
        graphViewModel.graph.removeVertex(graphViewModel.graph.getVertexByName(vertex.value ?: ""))
        graphViewModel.updateGraph(graphViewModel.graph)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    private var _startVertex = mutableStateOf<String?>(null)
    val startVertex: State<String?> = _startVertex

    private var _endVertex = mutableStateOf<String?>(null)
    val endVertex: State<String?> = _endVertex

    private var _width = mutableStateOf<Double?>(null)
    val width: State<Double?> = _width

    fun setStartVertex(startV: String){
        _startVertex.value = startV
    }

    fun setEndVertex(endV: String){
        _endVertex.value = endV
    }

    fun setWidthVertex(width: Double?){
        _width.value = width
    }

    fun addEdge(): Boolean {
        val g = graphViewModel.graph
        val start = g.getVertexByName(startVertex.value ?: return false)
        val end = g.getVertexByName(endVertex.value ?: return false)

        graphViewModel.graph.addEdge(start, end, width.value)
        graphViewModel.updateGraph(graphViewModel.graph)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)

        return true
    }

    fun delEdge(): Boolean{
        val g = graphViewModel.graph
        val start = g.getVertexByName(startVertex.value ?: return false)
        val end = g.getVertexByName(endVertex.value ?: return false)

        graphViewModel.graph.removeEdge(start, end)
        graphViewModel.updateGraph(graphViewModel.graph)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
        return true
    }

    fun getVertexs(): Map<Vertex, List<Edge>>{
        return graphViewModel.graph.getMap()
    }

    fun createGraph(isDirected: Boolean, isWeighted: Boolean){
        graphViewModel.graph = GraphImpl(isDirected, isWeighted)
        graphViewModel.updateGraph(graphViewModel.graph)
    }

    fun runNeo4j() = withNeoDB { readFromDB(graphViewModel.isDirected(), graphViewModel.isWeighted()).also { setNewGraph(it) } }
    fun saveToNeo4j() = withNeoDB {
        val graph = graphViewModel.graph
        if (graph.getVertices().isEmpty()) {
            println("Neo4j Save: graph empty")
            return@withNeoDB
        }
        clearDatabase()
        writeDB(graph)
        println("Neo4j: graph saved successfully")
    }

    fun clearNeo4jDatabase() = withNeoDB { clearDatabase(); println("Neo4j: database cleared") }

    fun clearGraph() {
        val g = graphViewModel.graph
        val newG = when {
            g.isDirected() && g.isWeighted() -> GraphFactory.createDirectedWeightedGraph()
            g.isDirected() && !g.isWeighted() -> GraphFactory.createDirectedUnweightedGraph()
            !g.isDirected() && g.isWeighted() -> GraphFactory.createUndirectedWeightedGraph()
            else -> GraphFactory.createUndirectedUnweightedGraph()
        }
        setNewGraph(newG)

        graphViewModel.updateGraph(g)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    // Algorithm triggers
    fun runFordBellman() {
        resetColor()
        _startName.value?.let { start ->
            _endName.value?.let { end ->
                graphViewModel.startFordBellman(start, end)
            }
        }
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
//        graphViewModel.updateGraph(graphViewModel.graph)
//        graphViewModel.updateEdges(_showVerticesLabels, _showEdgesLabels)
    }

    // Vertex size binding
    val vertexSize: State<Float> get() = graphViewModel.vertexSize
    fun updateVertexSize(v: Float) = graphViewModel.updateVertexSize(v)

    // Credentials/state
    private var _startName = mutableStateOf<String?>(null)
    val startName: State<String?> = _startName
    private var _endName = mutableStateOf<String?>(null)
    val endName: State<String?> = _endName
    private var _uri = mutableStateOf<String?>(null)
    val uri: State<String?> = _uri
    private var _user = mutableStateOf<String?>(null)
    val username: State<String?> = _user
    private var _pass = mutableStateOf<String?>(null)
    val password: State<String?> = _pass

    fun setStart(name: String) {
        _startName.value = name
    }

    fun setEnd(name: String) {
        _endName.value = name
    }

    fun setUri(uri: String){
        _uri.value = uri
    }
    fun setUsername(user: String){
        _user.value = user

    }fun setPassword(pass: String){
        _pass.value = pass
    }
    fun resetColor() {
        graphViewModel.vertices.forEach { it.color = Color.Gray }
        graphViewModel.edges.forEach { it.color = Color.Gray }
    }

    // Neo4j helper
    private fun withNeoDB(action: Neo4j.() -> Unit) {
        val uri = _uri.value;
        val usr = _user.value
        if (uri.isNullOrBlank() || usr.isNullOrBlank()) {
            println("Neo4j: missing credentials"); return
        }
        Neo4j(uri, usr, _pass.value ?: "").action()
    }

    private fun setNewGraph(g: Graph) {
        graphViewModel = GraphViewModel(g, _showVerticesLabels, _showEdgesLabels)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }
}
