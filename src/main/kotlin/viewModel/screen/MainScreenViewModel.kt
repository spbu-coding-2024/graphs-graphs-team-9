package viewModel.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import model.graph.*
import model.io.Neo4j.Neo4j
import viewModel.graph.GraphViewModel
import viewModel.screen.layouts.RepresentationStrategy
import androidx.compose.runtime.State
import java.io.File
import model.io.SQLite.SQLiteService
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

class MainScreenViewModel(
    private var graph: Graph,
    val representationStrategy: RepresentationStrategy,
    private val sqliteServiceInstance: SQLiteService = SQLiteService()
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
        graphViewModel.graph.removeVertex(graphViewModel.graph.getVertexByName(_vertex.value ?: "").toString())
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }

    private var _startVertex = mutableStateOf<String?>(null)
    val startVertex: State<String?> = _startVertex

    private var _endVertex = mutableStateOf<String?>(null)
    val endVertex: State<String?> = _endVertex

    private var _width = mutableStateOf<Double?>(null)
    val width: State<Double?> = _width

    fun setStartVertex(startV: String) {
        _startVertex.value = startV
    }

    fun setEndVertex(endV: String) {
        _endVertex.value = endV
    }

    fun setWidthVertex(width: Double?) {
        _width.value = width
    }

    fun addEdge(): Boolean {
        val g = graphViewModel.graph
        val start = g.getVertexByName(startVertex.value ?: return false)
        val end = g.getVertexByName(endVertex.value ?: return false)

        graphViewModel.graph.addEdge(startVertex.value.toString(), endVertex.value.toString(), width.value)
        graphViewModel.refreshGraph()
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)

        return true
    }

    fun delEdge(): Boolean {
        val g = graphViewModel.graph
        val start = g.getVertexByName(startVertex.value ?: return false)
        val end = g.getVertexByName(endVertex.value ?: return false)

        graphViewModel.graph.removeEdge(startVertex.value.toString(), endVertex.value.toString())
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
    }

    fun runTarjan() {
        resetColor()
        graphViewModel.startTarjan()
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
                readFromDB(graphViewModel.isDirected(), graphViewModel.isWeighted()).also { setNewGraph(it) }
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

    private fun showSQLiteOpenFileChooserPlatform(
            initialDirectory: String?,
            onFileSelected: (String) -> Unit
    ) {
        try {
            for (info in UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus" == info.name) {
                    UIManager.setLookAndFeel(info.className)
                    break
                }
            }
        } catch (e: Exception) {
        }

        val chooser = JFileChooser(initialDirectory ?: System.getProperty("user.home"))
        chooser.dialogTitle = "Open SQLite Database File"
        chooser.fileFilter = FileNameExtensionFilter("SQLite Databases (*.db, *.sqlite, *.sqlite3)", "db", "sqlite", "sqlite3")
        chooser.fileSelectionMode = JFileChooser.FILES_ONLY

        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            onFileSelected(chooser.selectedFile.absolutePath)
        }
    }

    fun requestSQLiteFileOpen() {
        val initialDir = _currentSQLiteDbPath.value?.let { File(it).parent } ?: System.getProperty("user.home")
        showSQLiteOpenFileChooserPlatform(initialDir, this::onSQLiteFileSelectedForOpen)
    }

    // Для моков
    private val sqliteService = sqliteServiceInstance

    private val _showSaveAsSQLiteDialog = mutableStateOf(false)
    val showSaveAsSQLiteDialog: State<Boolean> = _showSaveAsSQLiteDialog

    private val _saveAsFileName = mutableStateOf("database.db")
    val saveAsFileName: State<String> = _saveAsFileName
    fun setSaveAsFileName(name: String) { _saveAsFileName.value = name }

    private val _saveAsDirectoryPath = mutableStateOf<String?>(System.getProperty("user.home"))
    val saveAsDirectoryPath: State<String?> = _saveAsDirectoryPath
    fun setSaveAsDirectoryPath(path: String?) { _saveAsDirectoryPath.value = path }

    private val _currentSQLiteDbPath = mutableStateOf<String?>(null)
    val currentSQLiteDbPath: State<String?> = _currentSQLiteDbPath

    fun openSaveAsSQLiteDialog() {
        val currentPath = _currentSQLiteDbPath.value
        _saveAsDirectoryPath.value = currentPath?.let { File(it).parent } ?: System.getProperty("user.home")
        _saveAsFileName.value = currentPath?.let { File(it).name } ?: "database.db"
        _showSaveAsSQLiteDialog.value = true
    }

    fun cancelSaveAsSQLiteDialog() {
        _showSaveAsSQLiteDialog.value = false
    }

    fun confirmSaveAsSQLite() {
        val dirPath = _saveAsDirectoryPath.value
        val fileName = _saveAsFileName.value

        if (dirPath.isNullOrBlank()) {
            handleError(IllegalArgumentException("Directory path not selected for saving."))
            return
        }
        if (fileName.isBlank()) {
            handleError(IllegalArgumentException("File name cannot be empty for saving."))
            return
        }

        val result = sqliteService.saveGraphToNewFile(graphViewModel.graph, dirPath, fileName)

        result.onSuccess { savedFilePath ->
            _currentSQLiteDbPath.value = savedFilePath
            _showSaveAsSQLiteDialog.value = false
            println("Graph saved successfully to: $savedFilePath")
        }.onFailure { exception ->
            handleError(exception)
        }
    }

    fun onSQLiteFileSelectedForOpen(filePath: String) {
        val result = sqliteService.loadGraphFromFile(filePath)

        result.onSuccess { (loadedGraph, path) ->
            setNewGraph(loadedGraph)
            _currentSQLiteDbPath.value = path
            println("Graph loaded successfully from: $path")
        }.onFailure { exception ->
            handleError(exception)
        }
    }

    fun saveToCurrentSQLiteFile() {
        val currentPath = _currentSQLiteDbPath.value
        if (currentPath == null) {
            handleError(IllegalStateException("No current SQLite database file set. Use 'Save As...' first."))
            openSaveAsSQLiteDialog()
            return
        }
        val result = sqliteService.saveGraphToCurrentFile(graphViewModel.graph, currentPath)

        result.onSuccess {
            println("Graph saved successfully to current file: $currentPath")
        }.onFailure { exception ->
            handleError(exception)
        }
    }

    fun setNewGraph(newGraph: Graph) {
        this.graph = newGraph
        graphViewModel.updateGraph(newGraph)
        representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
    }
}
