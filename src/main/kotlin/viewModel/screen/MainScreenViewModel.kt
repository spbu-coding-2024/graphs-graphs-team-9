package viewModel.screen

import androidx.compose.runtime.MutableState
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.io.SQLGraph

class MainScreenViewModel(
    private val graph: Graph,
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

    var graphViewModel = GraphViewModel(graph, _showVerticesLabels, _showEdgesLabels)

    private var currentCanvasWidth: Double = 800.0
    private var currentCanvasHeight: Double = 600.0

    init {
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    fun resetGraphView() {
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
        graphViewModel.vertices.forEach { v -> v.color = Color.Gray }
    }

    private var _vertex = mutableStateOf<String?>(null)
    val vertex: State<String?> = _vertex

    fun setVertex(name: String) {
        _vertex.value = name
    }

    val map: MutableMap<String?, Int> = mutableMapOf()
    fun addVertex() {
        graphViewModel.graph.addVertex(vertex.value ?: "")
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    fun delVertex() {
        graphViewModel.graph.removeVertex(_vertex.value ?: "")
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    private var _startVertex = mutableStateOf<String?>(null)
    val startVertex: State<String?> = _startVertex

    private var _endVertex = mutableStateOf<String?>(null)
    val endVertex: State<String?> = _endVertex

    private var _weight = mutableStateOf<Double?>(null)
    val width: State<Double?> = _weight

    fun setStartVertex(startV: String) {
        _startVertex.value = startV
    }

    fun setEndVertex(endV: String) {
        _endVertex.value = endV
    }

    fun setWidthVertex(width: Double?) {
        _weight.value = width
    }

    fun addEdge(): Boolean {
        graphViewModel.graph.addEdge(startVertex.value ?: "", endVertex.value ?: "", width.value)
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)

        return true
    }

    fun delEdge(): Boolean {
        graphViewModel.graph.removeEdge(_startVertex.value ?: "", _endVertex.value ?: "")
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
        return true
    }

    fun getVertexes(): Map<Vertex, List<Edge>> {
        return graphViewModel.graph.getMap()
    }

    fun createGraph(isDirected: Boolean, isWeighted: Boolean) {
        graphViewModel.graph = GraphImpl(isDirected, isWeighted)
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

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

    fun runFordBellman() {
        resetColor()
        _startName.value?.let { start ->
            _endName.value?.let { end ->
                graphViewModel.startFordBellman(start, end)
            }
        }
    }

    fun runDijkstra() {
        resetColor()
        _startName.value?.let { start ->
            _endName.value?.let { end ->
                graphViewModel.startDijkstra(start, end)
            }
        }
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
//        graphViewModel.updateGraph(graphViewModel.graph)
//        graphViewModel.updateEdges(_showVerticesLabels, _showEdgesLabels)
    }

    fun runTarjan() {
        resetColor()
        graphViewModel.startTarjan()
    }

    fun runFindKey(){
        resetGraphView()
        graphViewModel.startFindKeyVertex()
    }

    // Vertex size binding
    val vertexSize: State<Float> get() = graphViewModel.vertexSize
    fun updateVertexSize(v: Float) = graphViewModel.updateVertexSize(v)


    private var _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var _showErrorDialog = mutableStateOf(false)
    val showErrorDialog: State<Boolean> = _showErrorDialog

    fun clearError() {
        _errorMessage.value = null
        _showErrorDialog.value = false
    }

    fun handleError(error: Throwable) {
        _errorMessage.value = error.message ?: "Exception"
        _showErrorDialog.value = true
    }

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

    private var _isDirect = mutableStateOf<Boolean>(false)
    val isDirect: State<Boolean> = _isDirect
    private var _isWeight = mutableStateOf<Boolean>(false)
    val isWeight: State<Boolean> = _isWeight

    fun setIsDirect(f: Boolean){
        _isDirect.value = f
    }
    fun setIsWeight(f: Boolean){
        _isWeight.value = f
    }
    fun setStart(name: String) {
        _startName.value = name
    }

    fun setEnd(name: String) {
        _endName.value = name
    }

    fun setUri(uri: String) {
        _uri.value = uri
    }

    fun setUsername(user: String) {
        _user.value = user
    }

    fun setPassword(pass: String) {
        _pass.value = pass
    }

    fun resetColor() {
        graphViewModel.vertices.forEach { it.color = Color.Gray }
        graphViewModel.edges.forEach { it.color = Color.Gray }
    }

    private fun withNeoDB(action: Neo4j.() -> Unit) {
        val uri = _uri.value;
        val usr = _user.value
        if (uri.isNullOrBlank() || usr.isNullOrBlank()) {
            println("Neo4j: missing credentials"); return
        }
        Neo4j(uri, usr, _pass.value ?: "").action()
    }

    fun runNeo4j() =
        withNeoDB { }

    fun uploadGraph() {
        try {
            withNeoDB {
                readFromDB(isDirect.value, isWeight.value).also { setNewGraph(it) }
            }
        } catch (e: Exception) {
            handleError(e)
        }
        graphViewModel.refreshGraph()
        resetGraphView()
    }

    fun saveToNeo4j() {
        try {
            withNeoDB {
                val graph = graphViewModel.graph
                if (graph.getVertices().isEmpty()) {
                    throw Exception("Graph is empty")
                }
                clearDatabase()
                writeDB(graph)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }


    fun clearNeo4jDatabase() {
        try {
            withNeoDB {
                clearDatabase()
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private var _dbPath = mutableStateOf<String?>(null)
    val dbPath: State<String?> = _dbPath

    private fun withSQLiteDB(action: SQLGraph.() -> Unit) {
        val path = _dbPath.value
        if (path.isNullOrBlank()) {
            println("SQLite: missing database path")
            return
        }
        try {
            SQLGraph(path).action()
        } catch (e: Exception) {
            println("SQLite error: ${e.message}")
            e.printStackTrace()
        }
    }

    fun initializeSQLiteDB() {
        withSQLiteDB {
            initializeDatabase()
            println("SQLite: database initialized successfully")
        }
    }

    fun saveToSQLite() {
        withSQLiteDB {
            val graph = graphViewModel.graph
            if (graph.getVertices().isEmpty()) {
                println("SQLite Save: graph empty")
                return@withSQLiteDB
            }

            // Проверяем, что graph это GraphImpl
            if (graph is GraphImpl) {
                saveGraph(graph)
                println("SQLite: graph saved successfully")
            } else {
                println("SQLite Save: graph must be GraphImpl instance")
            }
        }
    }

    fun uploadFromSQLite() {
        withSQLiteDB {
            val loadedGraph = loadGraph()
            if (loadedGraph != null) {
                setNewGraph(loadedGraph)
                println("SQLite: graph loaded successfully")
            } else {
                println("SQLite: no graph found or database not initialized")
            }
        }
    }

    fun setDbPath(path: String) {
        _dbPath.value = path
    }

    private fun setNewGraph(g: Graph) {
        graphViewModel = GraphViewModel(g, _showVerticesLabels, _showEdgesLabels)
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }
}
