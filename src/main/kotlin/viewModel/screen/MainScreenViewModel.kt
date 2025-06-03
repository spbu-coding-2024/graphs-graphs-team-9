package viewModel.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import model.graph.*
import model.io.Neo4j.Neo4jRepository
import viewModel.graph.GraphViewModel
import viewModel.screen.layouts.RepresentationStrategy
import androidx.compose.runtime.State
import androidx.compose.ui.unit.dp
import java.io.File
import model.io.SQLite.SQLiteService
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private var graph: Graph,
    val representationStrategy: RepresentationStrategy,
    private val sqliteServiceInstance: SQLiteService = SQLiteService()
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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
        requestLayoutUpdate()
    }

    private fun requestLayoutUpdate() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                representationStrategy.layout(currentCanvasHeight, currentCanvasWidth, graphViewModel)
            } catch (e: Exception) {
                handleError(Exception("Layout failed: ${e.message}", e))
            }
        }
    }

    fun resetGraphView() {
        graphViewModel.vertices.forEach { v ->
            v.color = Color.Gray
            v.radius = 25.dp
        }
        requestLayoutUpdate()
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
        requestLayoutUpdate()
    }

    fun delVertex() {
        graphViewModel.graph.removeVertex(_vertex.value ?: "")
        graphViewModel.refreshGraph()
        requestLayoutUpdate()
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
        requestLayoutUpdate()
        return true
    }

    fun delEdge(): Boolean {
        graphViewModel.graph.removeEdge(_startVertex.value ?: "", _endVertex.value ?: "")
        graphViewModel.refreshGraph()
        requestLayoutUpdate()
        return true
    }

    fun getVertexes(): Map<Vertex, List<Edge>> {
        return graphViewModel.graph.getMap()
    }

    fun createGraph(isDirected: Boolean, isWeighted: Boolean) {
        graphViewModel.graph = GraphImpl(isDirected, isWeighted)
        graphViewModel.refreshGraph()
        requestLayoutUpdate()
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
    }

    val findResult = mutableStateOf<String?>(null)

    fun getFindResult(): String{
        return findResult.value ?: ""
    }

    fun runFordBellman() {
        resetColor()
        val startNodeName = _startName.value
        val endNodeName = _endName.value

        if (startNodeName.isNullOrBlank() || endNodeName.isNullOrBlank()) {
            findResult.value = "Не указана начальная или конечная вершина"
            return
        }

        viewModelScope.launch {
            try {
                val result = graphViewModel.startFordBellman(startNodeName, endNodeName)
                if (result.first != null && result.second != null && result.second != Double.POSITIVE_INFINITY) {
                    findResult.value = result.second.toString()
                    graphViewModel.highlightFordBellmanPath(result)
                } else if (result.second == Double.POSITIVE_INFINITY) {
                    findResult.value = ""
                } else {
                    findResult.value = "Ошибка при поиске пути"
                }
            } catch (e: IllegalStateException) {
                findResult.value = "Ошибка: ${e.message}"
                handleError(e)
            }
            catch (e: Exception) {
                findResult.value = "Произошла ошибка"
                handleError(e)
            }
        }
    }

    fun runDijkstra() {
        resetColor()
        val startNodeName = _startName.value
        val endNodeName = _endName.value

        if (startNodeName.isNullOrBlank() || endNodeName.isNullOrBlank()) {
            findResult.value = "Не указана начальная или конечная вершина"
            return
        }

        viewModelScope.launch {
            try {
                val result = graphViewModel.startDijkstra(startNodeName, endNodeName)
                if (result != null) {
                    findResult.value = result.distance.toString()
                    graphViewModel.highlightDijkstraPath(result.path)
                } else {
                    findResult.value = ""
                }
            } catch (e: IllegalArgumentException) {
                findResult.value = "Ошибка: ${e.message}"
                handleError(e)
            } catch (e: Exception) {
                findResult.value = "Произошла ошибка"
                handleError(e)
            }
        }
    }

    fun runFindBridge() {
        resetColor()
        viewModelScope.launch {
            try {
                val bridges = graphViewModel.startFindBridges()
                graphViewModel.highlightBridges(bridges)
            } catch (e: Exception) {
                findResult.value = "Произошла ошибка при поиске мостов"
                handleError(e)
            }
        }
    }

    fun runTarjan() {
        resetColor()
        viewModelScope.launch {
            try {
                val components = graphViewModel.startTarjan()
                graphViewModel.highlightTarjanComponents(components)
            } catch (e: Exception) {
                findResult.value = "Произошла ошибка при поиске компонент"
                handleError(e)
            }
        }
    }

    fun runFindKey() {
        viewModelScope.launch {
            try {
                val centralityMap = graphViewModel.startFindKeyVertex()
                graphViewModel.applyKeyVertexVisuals(centralityMap)
            } catch (e: Exception) {
                findResult.value = "Произошла ошибка при поиске ключевых вершин"
                handleError(e)
            }
        }
    }

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

    fun setIsDirect(f: Boolean) {
        _isDirect.value = f
    }

    fun setIsWeight(f: Boolean) {
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

    private suspend fun withNeoDB(action: suspend Neo4jRepository.() -> Unit) {
        val uri = _uri.value;
        val usr = _user.value
        if (uri.isNullOrBlank() || usr.isNullOrBlank()) {
            println("Neo4j: missing credentials"); return
        }
        Neo4jRepository(uri, usr, _pass.value ?: "").action()
    }

    suspend fun runNeo4j() =
            withNeoDB { }

    suspend fun uploadGraph() {
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

    suspend fun saveToNeo4j() {
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


    suspend fun clearNeo4jDatabase() {
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

        viewModelScope.launch {
            val result = sqliteService.saveGraphToNewFile(graphViewModel.graph, dirPath, fileName)
            result.onSuccess { savedFilePath ->
                _currentSQLiteDbPath.value = savedFilePath
                _showSaveAsSQLiteDialog.value = false
                println("Graph saved successfully to: $savedFilePath")
            }.onFailure { exception ->
                handleError(exception)
            }
        }
    }

    fun onSQLiteFileSelectedForOpen(filePath: String) {
        viewModelScope.launch {
            val result = sqliteService.loadGraphFromFile(filePath)
            result.onSuccess { (loadedGraph, path) ->
                setNewGraph(loadedGraph)
                _currentSQLiteDbPath.value = path
                println("Graph loaded successfully from: $path")
            }.onFailure { exception ->
                handleError(exception)
            }
        }
    }

    fun saveToCurrentSQLiteFile() {
        val currentPath = _currentSQLiteDbPath.value
        if (currentPath == null) {
            handleError(IllegalStateException("No current SQLite database file set. Use 'Save As...' first."))
            openSaveAsSQLiteDialog()
            return
        }
        viewModelScope.launch {
            val result = sqliteService.saveGraphToCurrentFile(graphViewModel.graph, currentPath)
            result.onSuccess {
                println("Graph saved successfully to current file: $currentPath")
            }.onFailure { exception ->
                handleError(exception)
            }
        }
    }

    fun setNewGraph(newGraph: Graph) {
        this.graph = newGraph
        graphViewModel.updateGraph(newGraph)
        requestLayoutUpdate()
    }
}
