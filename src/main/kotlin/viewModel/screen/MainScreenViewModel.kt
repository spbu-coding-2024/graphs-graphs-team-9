package viewModel.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import viewModel.graph.GraphViewModel
import viewModel.graph.VertexViewModel
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

    var graphViewModel = GraphViewModel(
            initialGraph,
            _showVerticesLabels,
            _showEdgesLabels
    )

    private var rawVertexPositions: Map<Vertex, Pair<Float, Float>>? = null
    private var layoutAlgorithmApplied = false
    private var lastKnownCanvasWidthDp: Dp = 0.dp
    private var lastKnownCanvasHeightDp: Dp = 0.dp
    private var lastGraphHashForLayout: Int = 0

    init {
        lastGraphHashForLayout = graphViewModel.graph.hashCode()
    }

    private fun applyLayoutAlgorithmAndPlace(canvasWidthDp: Dp, canvasHeightDp: Dp) {
        if (graphViewModel.graph.getVertices().isEmpty()) {
            rawVertexPositions = emptyMap()
            layoutAlgorithmApplied = true
            lastGraphHashForLayout = graphViewModel.graph.hashCode()
            updateViewPositions(canvasWidthDp, canvasHeightDp)
            return
        }

        rawVertexPositions = representationStrategy.layout(
                graphViewModel.graph,
                canvasWidthDp.value.toDouble(),
                canvasHeightDp.value.toDouble()
        )
        layoutAlgorithmApplied = true
        lastGraphHashForLayout = graphViewModel.graph.hashCode()
        updateViewPositions(canvasWidthDp, canvasHeightDp)
    }

    private fun updateViewPositions(canvasWidthDp: Dp, canvasHeightDp: Dp) {
        val currentRawPositions = rawVertexPositions
        if (currentRawPositions == null) {
            if (graphViewModel.graph.getVertices().isNotEmpty()) {
                representationStrategy.layout(
                        graphViewModel.graph,
                        canvasWidthDp.value.toDouble(),
                        canvasHeightDp.value.toDouble()
                ).let {
                    rawVertexPositions = it
                    if (rawVertexPositions != null) updateViewPositions(canvasWidthDp, canvasHeightDp)
                    else {
                        val centerXDp = canvasWidthDp / 2f
                        val centerYDp = canvasHeightDp / 2f
                        graphViewModel.vertices.forEach { vmVertex ->
                            vmVertex.x = centerXDp - vmVertex.radius
                            vmVertex.y = centerYDp - vmVertex.radius
                        }
                    }
                }
            }
            return
        }

        if (graphViewModel.vertices.isEmpty() && currentRawPositions.isEmpty()) {
            return
        }

        if (currentRawPositions.isEmpty() && graphViewModel.vertices.isNotEmpty()) {
            val centerXDp = canvasWidthDp / 2f
            val centerYDp = canvasHeightDp / 2f
            graphViewModel.vertices.forEach { vmVertex ->
                vmVertex.x = centerXDp - vmVertex.radius
                vmVertex.y = centerYDp - vmVertex.radius
            }
            return
        }
        if (currentRawPositions.isEmpty()){
            return
        }

        val relevantRawPositions = currentRawPositions.filterKeys { vertex ->
            graphViewModel.vertices.any { it.label == vertex }
        }

        if (relevantRawPositions.isEmpty()) {
            if (graphViewModel.vertices.isNotEmpty()) {
                val centerXDp = canvasWidthDp / 2f
                val centerYDp = canvasHeightDp / 2f
                graphViewModel.vertices.forEach { vmVertex ->
                    vmVertex.x = centerXDp - vmVertex.radius
                    vmVertex.y = centerYDp - vmVertex.radius
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
            val finalXPosDp = canvasWidthDp / 2f
            val finalYPosDp = canvasHeightDp / 2f
            graphViewModel.vertices.forEach { vertexVM ->
                vertexVM.x = finalXPosDp - vertexVM.radius
                vertexVM.y = finalYPosDp - vertexVM.radius
            }
            return
        }

        val rawGraphWidth = maxX - minX
        val rawGraphHeight = maxY - minY

        val paddingDp = 50.dp
        val targetDisplayWidthDp = (canvasWidthDp - 2 * paddingDp).coerceAtLeast(1.dp)
        val targetDisplayHeightDp = (canvasHeightDp - 2 * paddingDp).coerceAtLeast(1.dp)

        val scaleXFactor = if (rawGraphWidth > 0.001f) targetDisplayWidthDp.value / rawGraphWidth else 1f
        val scaleYFactor = if (rawGraphHeight > 0.001f) targetDisplayHeightDp.value / rawGraphHeight else 1f
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
            val finalXPosDp = canvasWidthDp / 2f
            val finalYPosDp = canvasHeightDp / 2f
            graphViewModel.vertices.forEach { vertexVM ->
                vertexVM.x = finalXPosDp - vertexVM.radius
                vertexVM.y = finalYPosDp - vertexVM.radius
            }
            return
        }

        val scaledGraphActualWidth = (newMaxX - newMinX).coerceAtLeast(0f)
        val scaledGraphActualHeight = (newMaxY - newMinY).coerceAtLeast(0f)

        val offsetXdp = paddingDp + (targetDisplayWidthDp - scaledGraphActualWidth.dp) / 2f - newMinX.dp
        val offsetYdp = paddingDp + (targetDisplayHeightDp - scaledGraphActualHeight.dp) / 2f - newMinY.dp


        graphViewModel.vertices.forEach { vertexVM ->
            scaledPositions[vertexVM.label]?.let { (sX, sY) ->
                vertexVM.x = sX.dp + offsetXdp
                vertexVM.y = sY.dp + offsetYdp
            } ?: run {
                vertexVM.x = canvasWidthDp / 2f - vertexVM.radius
                vertexVM.y = canvasHeightDp / 2f - vertexVM.radius
            }
        }
    }

    fun initializeOrUpdatePlacement(canvasWidthDp: Dp, canvasHeightDp: Dp) {
        this.lastKnownCanvasWidthDp = canvasWidthDp
        this.lastKnownCanvasHeightDp = canvasHeightDp

        val graphJustChanged = graphViewModel.graph.hashCode() != lastGraphHashForLayout
        val sizeChangedSignificantly = lastKnownCanvasWidthDp != canvasWidthDp || lastKnownCanvasHeightDp != canvasHeightDp


        if (!layoutAlgorithmApplied || graphJustChanged) {
            applyLayoutAlgorithmAndPlace(canvasWidthDp, canvasHeightDp)
        } else if (sizeChangedSignificantly) {
            reCenterGraph(canvasWidthDp, canvasHeightDp)
        }
    }

    private fun reCenterGraph(canvasWidthDp: Dp, canvasHeightDp: Dp) {
        if (graphViewModel.vertices.isEmpty()) {
            return
        }

        var currentMinXdp = Float.MAX_VALUE.dp
        var currentMaxXdp = Float.MIN_VALUE.dp
        var currentMinYdp = Float.MAX_VALUE.dp
        var currentMaxYdp = Float.MIN_VALUE.dp

        graphViewModel.vertices.forEach { vmVertex ->
            currentMinXdp = minOf(currentMinXdp, vmVertex.x)
            currentMaxXdp = maxOf(currentMaxXdp, vmVertex.x + vmVertex.radius * 2)
            currentMinYdp = minOf(currentMinYdp, vmVertex.y)
            currentMaxYdp = maxOf(currentMaxYdp, vmVertex.y + vmVertex.radius * 2)
        }

        if (currentMinXdp > currentMaxXdp || currentMinYdp > currentMaxYdp) {
            val centerX = canvasWidthDp / 2f
            val centerY = canvasHeightDp / 2f
            graphViewModel.vertices.forEach { vmVertex ->
                vmVertex.x = centerX - vmVertex.radius
                vmVertex.y = centerY - vmVertex.radius
            }
            return
        }

        val currentGraphWidthDp = currentMaxXdp - currentMinXdp
        val currentGraphHeightDp = currentMaxYdp - currentMinYdp

        val currentGraphCenterXdp = currentMinXdp + currentGraphWidthDp / 2
        val currentGraphCenterYdp = currentMinYdp + currentGraphHeightDp / 2

        val targetCanvasCenterXdp = canvasWidthDp / 2f
        val targetCanvasCenterYdp = canvasHeightDp / 2f

        val shiftXdp = targetCanvasCenterXdp - currentGraphCenterXdp
        val shiftYdp = targetCanvasCenterYdp - currentGraphCenterYdp

        graphViewModel.vertices.forEach { vmVertex ->
            val shiftedX = vmVertex.x + shiftXdp
            val shiftedY = vmVertex.y + shiftYdp

            var finalX = shiftedX
            var finalY = shiftedY
            val vertexDiameter = vmVertex.radius * 2

            finalX = finalX.coerceAtLeast(0.dp)
            if (canvasWidthDp > vertexDiameter) {
                finalX = finalX.coerceAtMost(canvasWidthDp - vertexDiameter)
            } else {
                finalX = 0.dp
            }

            finalY = finalY.coerceAtLeast(0.dp)
            if (canvasHeightDp > vertexDiameter) {
                finalY = finalY.coerceAtMost(canvasHeightDp - vertexDiameter)
            } else {
                finalY = 0.dp
            }

            vmVertex.x = finalX
            vmVertex.y = finalY
        }
    }

    fun processVertexDrag(vertexVM: VertexViewModel, deltaXDp: Dp, deltaYDp: Dp) {
        var newX = vertexVM.x + deltaXDp
        var newY = vertexVM.y + deltaYDp

        val vertexDiameter = vertexVM.radius * 2

        newX = newX.coerceAtLeast(0.dp)
        if (lastKnownCanvasWidthDp > vertexDiameter) {
            newX = newX.coerceAtMost(lastKnownCanvasWidthDp - vertexDiameter)
        } else {
            newX = 0.dp
        }

        newY = newY.coerceAtLeast(0.dp)
        if (lastKnownCanvasHeightDp > vertexDiameter) {
            newY = newY.coerceAtMost(lastKnownCanvasHeightDp - vertexDiameter)
        } else {
            newY = 0.dp
        }

        vertexVM.x = newX
        vertexVM.y = newY
    }

    fun resetGraphView(useForceAtlas2: Boolean = true) {
        resetColor()
        layoutAlgorithmApplied = false
        rawVertexPositions = null
        if (lastKnownCanvasWidthDp > 0.dp && lastKnownCanvasHeightDp > 0.dp) {
            applyLayoutAlgorithmAndPlace(lastKnownCanvasWidthDp, lastKnownCanvasHeightDp)
        }
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
                    true,
                    true
            )
            graphViewModel = GraphViewModel(
                    newGraph,
                    _showVerticesLabels,
                    _showEdgesLabels
            )
            layoutAlgorithmApplied = false
            rawVertexPositions = null
            if (lastKnownCanvasWidthDp > 0.dp && lastKnownCanvasHeightDp > 0.dp) {
                applyLayoutAlgorithmAndPlace(lastKnownCanvasWidthDp, lastKnownCanvasHeightDp)
            }
        } catch (e: Exception) {
            println("Error running Neo4j load: ${e.message}")
        }
    }

    fun clearGraph() {
        val currentGraphIsDirected = graphViewModel.graph.isDirected()
        val currentGraphIsWeighted = graphViewModel.graph.isWeighted()

        val newEmptyGraph = when {
            currentGraphIsDirected && currentGraphIsWeighted -> GraphFactory.createDirectedWeightedGraph()
            currentGraphIsDirected && !currentGraphIsWeighted -> GraphFactory.createDirectedUnweightedGraph()
            !currentGraphIsDirected && currentGraphIsWeighted -> GraphFactory.createUndirectedWeightedGraph()
            else -> GraphFactory.createUndirectedUnweightedGraph()
        }
        graphViewModel = GraphViewModel(
                newEmptyGraph,
                _showVerticesLabels,
                _showEdgesLabels
        )
        rawVertexPositions = emptyMap()
        layoutAlgorithmApplied = true
        lastGraphHashForLayout = newEmptyGraph.hashCode()

        if (lastKnownCanvasWidthDp > 0.dp && lastKnownCanvasHeightDp > 0.dp) {
            updateViewPositions(lastKnownCanvasWidthDp, lastKnownCanvasHeightDp)
        }
    }

    val vertexSize: State<Float>
        get() = graphViewModel.vertexSize
    fun updateVertexSize(newSize: Float) = graphViewModel.updateVertexSize(newSize)

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
        graphViewModel.edges.forEach { e -> e.color = Color.Gray }
    }

    private fun clearAlgorithmIds() {
        _startName.value = null
        _endName.value = null
    }

    fun runFordBellman() {
        resetColor()
        try {
            val startVertexName = _startName.value
            val endVertexName = _endName.value
            if (startVertexName != null && endVertexName != null) {
                val startV = graphViewModel.graph.getVertexByName(startVertexName)
                val endV = graphViewModel.graph.getVertexByName(endVertexName)
                if (startV.id != -1 && endV.id != -1) {
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
    fun applyForceAtlas2Layout() { resetGraphView() }
    fun applyForceAtlas2Layout(iterations: Int, barnesHutOptimize: Boolean, scalingRatio: Double, gravity: Double ) {
        println("Custom ForceAtlas2 params: iter=$iterations, barnes=$barnesHutOptimize, scale=$scalingRatio, grav=$gravity. Applying default re-layout for now.")
        resetGraphView()
    }
}