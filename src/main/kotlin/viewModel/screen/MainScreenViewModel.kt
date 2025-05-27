package viewModel.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.graph.Graph
import model.graph.GraphFactory // Убедитесь, что импорт правильный
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import viewModel.graph.GraphViewModel
import viewModel.screen.layouts.ForceAtlas2
import viewModel.screen.layouts.RepresentationStrategy
import kotlin.math.abs
import kotlin.math.min

class MainScreenViewModel(
        initialGraph: Graph,
        private val representationStrategy: RepresentationStrategy = ForceAtlas2(initialGraph)
) {
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

    var graphViewModel = GraphViewModel(initialGraph, _showVerticesLabels, _showEdgesLabels)

    private var rawVertexPositions: Map<Vertex, Pair<Float, Float>>? = null
    private var layoutAlgorithmApplied = false
    private var lastKnownCanvasWidth: Float = 0f
    private var lastKnownCanvasHeight: Float = 0f
    private var lastGraphHashForLayout: Int = 0 // Для отслеживания изменений графа

    init {
        lastGraphHashForLayout = graphViewModel.graph.hashCode()
        // Первичная компоновка будет вызвана из initializeOrUpdatePlacement при первом получении размеров
    }

    private fun applyLayoutAlgorithmAndPlace(canvasWidth: Float, canvasHeight: Float) {
        if (graphViewModel.graph.getVertices().isEmpty()) {
            rawVertexPositions = emptyMap()
            layoutAlgorithmApplied = true
            lastGraphHashForLayout = graphViewModel.graph.hashCode()
            updateViewPositions(canvasWidth, canvasHeight) // Обновить для пустого графа (если есть VM)
            return
        }

        rawVertexPositions = representationStrategy.layout(
                graphViewModel.graph,
                canvasWidth.toDouble(),
                canvasHeight.toDouble()
        )
        layoutAlgorithmApplied = true
        lastGraphHashForLayout = graphViewModel.graph.hashCode()
        updateViewPositions(canvasWidth, canvasHeight)
    }

    private fun updateViewPositions(canvasWidth: Float, canvasHeight: Float) {
        val currentRawPositions = rawVertexPositions
        if (currentRawPositions == null) { // layoutAlgorithmApplied здесь не так важен, важнее наличие raw позиций
            // Это состояние не должно достигаться, если initializeOrUpdatePlacement работает корректно
            // Если сырых позиций нет, значит, алгоритм компоновки еще не отработал.
            // Это может произойти, если окно изменило размер до первого вызова applyLayoutAlgorithmAndPlace
            // или если граф был изменен и rawVertexPositions были сброшены.
            // В таком случае, applyLayoutAlgorithmAndPlace должен быть вызван из initializeOrUpdatePlacement.
            println("Warning: updateViewPositions called with null rawVertexPositions. Re-applying layout.")
            applyLayoutAlgorithmAndPlace(canvasWidth, canvasHeight)
            return
        }

        if (graphViewModel.vertices.isEmpty() && currentRawPositions.isEmpty()) {
            return // Нечего обновлять
        }

        // Если currentRawPositions пуст, но в graphViewModel есть вершины (например, после очистки графа и добавления новых вершин без перекомпоновки)
        if (currentRawPositions.isEmpty() && graphViewModel.vertices.isNotEmpty()) {
            val centerXDp = (canvasWidth / 2f).dp
            val centerYDp = (canvasHeight / 2f).dp
            graphViewModel.vertices.forEach { vmVertex ->
                vmVertex.x = centerXDp
                vmVertex.y = centerYDp
            }
            return
        }
        if (currentRawPositions.isEmpty()){ // Иначе, если и вершин нет, и позиций нет
            return
        }

        val relevantRawPositions = currentRawPositions.filterKeys { vertex ->
            graphViewModel.vertices.any { it.label == vertex } // Исправлено it.vertex на it.label
        }

        if (relevantRawPositions.isEmpty()) {
            if (graphViewModel.vertices.isNotEmpty()) {
                val centerXDp = (canvasWidth / 2f).dp
                val centerYDp = (canvasHeight / 2f).dp
                graphViewModel.vertices.forEach { vmVertex ->
                    vmVertex.x = centerXDp
                    vmVertex.y = centerYDp
                }
            }
            return
        }

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        relevantRawPositions.values.forEach { (x, y) ->
            minX = minOf(minX, x)
            maxX = maxOf(maxX, x)
            minY = minOf(minY, y)
            maxY = maxOf(maxY, y)
        }

        val isSinglePoint = abs(maxX - minX) < 0.001f && abs(maxY - minY) < 0.001f

        if (isSinglePoint) {
            val finalXPos = canvasWidth / 2f
            val finalYPos = canvasHeight / 2f
            graphViewModel.vertices.forEach { vertexVM ->
                relevantRawPositions[vertexVM.label]?.let { rawP -> // Исправлено vertexVM.vertex на vertexVM.label
                    vertexVM.x = (finalXPos - (rawP.first - minX)).dp
                    vertexVM.y = (finalYPos - (rawP.second - minY)).dp
                } ?: run {
                    vertexVM.x = finalXPos.dp
                    vertexVM.y = finalYPos.dp
                }
            }
            return
        }

        val rawGraphWidth = maxX - minX
        val rawGraphHeight = maxY - minY

        val padding = 50f
        val targetDisplayWidth = (canvasWidth - 2 * padding).coerceAtLeast(1f)
        val targetDisplayHeight = (canvasHeight - 2 * padding).coerceAtLeast(1f)

        val scaleXFactor = if (rawGraphWidth > 0.001f) targetDisplayWidth / rawGraphWidth else 1f
        val scaleYFactor = if (rawGraphHeight > 0.001f) targetDisplayHeight / rawGraphHeight else 1f
        val finalScale = minOf(scaleXFactor, scaleYFactor).coerceAtLeast(0.01f)

        val scaledPositions = mutableMapOf<Vertex, Pair<Float, Float>>()
        var newMinX = Float.MAX_VALUE
        var newMaxX = Float.MIN_VALUE
        var newMinY = Float.MAX_VALUE
        var newMaxY = Float.MIN_VALUE

        relevantRawPositions.forEach { (vertex, pos) ->
            val sX = (pos.first - minX) * finalScale
            val sY = (pos.second - minY) * finalScale
            scaledPositions[vertex] = sX to sY
            newMinX = minOf(newMinX, sX)
            newMaxX = maxOf(newMaxX, sX)
            newMinY = minOf(newMinY, sY)
            newMaxY = maxOf(newMaxY, sY)
        }

        if (abs(newMaxX - newMinX) < 0.001f && abs(newMaxY - newMinY) < 0.001f && relevantRawPositions.isNotEmpty()) {
            val firstScaledX = if (newMinX.isFinite() && newMinX != Float.MAX_VALUE) newMinX else 0f
            val firstScaledY = if (newMinY.isFinite() && newMinY != Float.MAX_VALUE) newMinY else 0f
            val finalXPos = padding + (targetDisplayWidth / 2f) - firstScaledX
            val finalYPos = padding + (targetDisplayHeight / 2f) - firstScaledY
            graphViewModel.vertices.forEach { vertexVM ->
                scaledPositions[vertexVM.label]?.let { (sX, sY) -> // Исправлено vertexVM.vertex на vertexVM.label
                    vertexVM.x = (sX + (finalXPos - sX)).dp
                    vertexVM.y = (sY + (finalYPos - sY)).dp
                } ?: run {
                    vertexVM.x = (canvasWidth / 2f).dp
                    vertexVM.y = (canvasHeight / 2f).dp
                }
            }
            return
        }

        val scaledGraphActualWidth = (newMaxX - newMinX).coerceAtLeast(0f)
        val scaledGraphActualHeight = (newMaxY - newMinY).coerceAtLeast(0f)

        val offsetX = padding + (targetDisplayWidth - scaledGraphActualWidth) / 2f - newMinX
        val offsetY = padding + (targetDisplayHeight - scaledGraphActualHeight) / 2f - newMinY

        graphViewModel.vertices.forEach { vertexVM ->
            scaledPositions[vertexVM.label]?.let { (sX, sY) -> // Исправлено vertexVM.vertex на vertexVM.label
                vertexVM.x = (sX + offsetX).dp
                vertexVM.y = (sY + offsetY).dp
            } ?: run {
                vertexVM.x = (canvasWidth / 2f).dp
                vertexVM.y = (canvasHeight / 2f).dp
            }
        }
    }

    fun initializeOrUpdatePlacement(canvasWidth: Float, canvasHeight: Float) {
        val graphJustChanged = graphViewModel.graph.hashCode() != lastGraphHashForLayout
        val sizeChangedSignificantly = abs(canvasWidth - lastKnownCanvasWidth) >= 1f ||
                abs(canvasHeight - lastKnownCanvasHeight) >= 1f

        if (!layoutAlgorithmApplied || graphJustChanged) {
            applyLayoutAlgorithmAndPlace(canvasWidth, canvasHeight)
        } else if (sizeChangedSignificantly) {
            updateViewPositions(canvasWidth, canvasHeight)
        }

        lastKnownCanvasWidth = canvasWidth
        lastKnownCanvasHeight = canvasHeight
    }

    fun resetGraphView(useForceAtlas2: Boolean = true) { // useForceAtlas2 теперь больше для совместимости API
        resetColor()
        layoutAlgorithmApplied = false // Помечаем, что нужно заново применить алгоритм
        rawVertexPositions = null      // Сбрасываем сырые позиции
        if (lastKnownCanvasWidth > 0f && lastKnownCanvasHeight > 0f) {
            applyLayoutAlgorithmAndPlace(lastKnownCanvasWidth, lastKnownCanvasHeight)
        }
        // Если размеры еще не известны, initializeOrUpdatePlacement будет вызван из LaunchedEffect позже
    }

    fun runNeo4j() {
        val uriString = _uri.value
        val usernameString = _username.value
        val passwordString = _password.value

        if (uriString.isNullOrBlank() || usernameString.isNullOrBlank()) {
            println("Neo4j Error: URI or Username is missing.")
            return
        }
        try {
            val newGraph = Neo4j(uriString, usernameString, passwordString ?: "").readFromDB(
                    true, // Предположим, что всегда читаем ориентированный
                    true  // и взвешенный, или передавать актуальные флаги
            )
            graphViewModel = GraphViewModel(newGraph, _showVerticesLabels, _showEdgesLabels)
            layoutAlgorithmApplied = false
            rawVertexPositions = null
            if (lastKnownCanvasWidth > 0f && lastKnownCanvasHeight > 0f) {
                applyLayoutAlgorithmAndPlace(lastKnownCanvasWidth, lastKnownCanvasHeight)
            }
        } catch (e: Exception) {
            println("Error running Neo4j load: ${e.message}")
        }
    }

    fun clearGraph() {
        val currentGraphIsDirected = graphViewModel.graph.isDirected()
        val currentGraphIsWeighted = graphViewModel.graph.isWeighted()

        val newEmptyGraph = when { // Используем GraphFactory из model.graph
            currentGraphIsDirected && currentGraphIsWeighted -> GraphFactory.createDirectedWeightedGraph()
            currentGraphIsDirected && !currentGraphIsWeighted -> GraphFactory.createDirectedUnweightedGraph()
            !currentGraphIsDirected && currentGraphIsWeighted -> GraphFactory.createUndirectedWeightedGraph()
            else -> GraphFactory.createUndirectedUnweightedGraph()
        }
        graphViewModel = GraphViewModel(newEmptyGraph, _showVerticesLabels, _showEdgesLabels)
        rawVertexPositions = emptyMap()
        layoutAlgorithmApplied = true // Для пустого графа считаем, что компоновка "применена"
        lastGraphHashForLayout = newEmptyGraph.hashCode()

        if (lastKnownCanvasWidth > 0f && lastKnownCanvasHeight > 0f) {
            updateViewPositions(lastKnownCanvasWidth, lastKnownCanvasHeight)
        }
    }

    val vertexSize: State<Float>
        get() = graphViewModel.vertexSize
    fun updateVertexSize(newSize: Float) = graphViewModel.updateVertexSize(newSize)

    // Остальные UI-специфичные состояния и методы
    private var _startName = mutableStateOf<String?>(null)
    val startName: State<String?> get() = _startName

    private var _endName = mutableStateOf<String?>(null)
    val endName: State<String?> get() = _endName

    private var _uri = mutableStateOf<String?>("bolt://localhost:7687")
    val uri: State<String?> get() = _uri

    private var _username = mutableStateOf<String?>("neo4j")
    val username: State<String?> get() = _username

    private var _password = mutableStateOf<String?>("")
    val password: State<String?> get() = _password

    fun resetColor() {
        graphViewModel.vertices.forEach { v -> v.color = Color.Gray }
        graphViewModel.edges.forEach { e -> e.color = Color.Gray } // CoolColors.edgeBasic
    }

    private fun clearAlgorithmIds() { // Переименовал для ясности
        _startName.value = null
        _endName.value = null
    }

    fun runFordBellman() {
        resetColor()
        try {
            val startVertexName = _startName.value
            val endVertexName = _endName.value
            if (startVertexName != null && endVertexName != null) {
                // Проверка на существование вершин по имени в графе
                val startV = graphViewModel.graph.getVertexByName(startVertexName)
                val endV = graphViewModel.graph.getVertexByName(endVertexName)
                if (startV.id != -1 && endV.id != -1) { // Проверка, что вершины найдены
                    graphViewModel.startFordBellman(startVertexName, endVertexName)
                } else {
                    println("FordBellman Error: Start or End vertex name not found in graph.")
                }
            } else {
                println("FordBellman Error: Start or End vertex name is null.")
            }
        } catch (e: Exception) {
            println("Error in runFordBellman: ${e.message}")
        } finally {
            clearAlgorithmIds()
        }
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
    }
    fun saveToNeo4j() {
        val uriString = _uri.value
        val usernameString = _username.value
        val passwordString = _password.value

        if (uriString.isNullOrBlank() || usernameString.isNullOrBlank()) {
            println("Neo4j Error: URI or Username is missing for save.")
            return
        }
        if (graphViewModel.graph.getVertices().isEmpty()) {
            println("Neo4j Save: Graph is empty, nothing to save.")
            return
        }
        try {
            val neo4jUtil = Neo4j(uriString, usernameString, passwordString ?: "")
            neo4jUtil.clearDatabase()
            neo4jUtil.writeDB(graphViewModel.graph)
            println("Graph saved to Neo4j successfully.")
        } catch (e: Exception) {
            println("Error saving to Neo4j: ${e.message}")
        }
    }

    fun clearNeo4jDatabase() {
        val uriString = _uri.value
        val usernameString = _username.value
        val passwordString = _password.value

        if (uriString.isNullOrBlank() || usernameString.isNullOrBlank()) {
            println("Neo4j Error: URI or Username is missing for clear.")
            return
        }
        try {
            Neo4j(uriString, usernameString, passwordString ?: "").clearDatabase()
            println("Neo4j database cleared successfully.")
        } catch (e: Exception) {
            println("Error clearing Neo4j database: ${e.message}")
        }
    }
    // Методы applyForceAtlas2Layout (если они нужны с кастомными параметрами) потребуют доработки,
    // чтобы передавать параметры в representationStrategy.layout или создавать временную стратегию.
    fun applyForceAtlas2Layout() { resetGraphView() }
    fun applyForceAtlas2Layout(iterations: Int, barnesHutOptimize: Boolean, scalingRatio: Double, gravity: Double ) {
        println("Custom ForceAtlas2 params: iter=$iterations, barnes=$barnesHutOptimize, scale=$scalingRatio, grav=$gravity. Applying default re-layout for now.")
        resetGraphView() // Пока что просто сброс, нужна доработка если нужны кастомные параметры "на лету"
    }
}