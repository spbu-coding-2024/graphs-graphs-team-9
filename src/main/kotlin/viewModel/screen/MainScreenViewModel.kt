package viewModel.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import viewModel.graph.GraphViewModel
import viewModel.graph.VertexViewModel
import viewModel.screen.layouts.ForceAtlas2
import viewModel.screen.layouts.RepresentationStrategy
import kotlin.math.abs

class MainScreenViewModel(
        initialGraph: Graph,
        private val representationStrategy: RepresentationStrategy = ForceAtlas2(initialGraph)
) {
    private var _showVerticesLabels = mutableStateOf(false)
    var showVerticesLabels: Boolean
        get() = _showVerticesLabels.value
        set(v) { _showVerticesLabels.value = v }

    private var _showEdgesLabels = mutableStateOf(false)
    var showEdgesLabels: Boolean
        get() = _showEdgesLabels.value
        set(v) { _showEdgesLabels.value = v }

    var graphViewModel = GraphViewModel(
            initialGraph,
            _showVerticesLabels,
            _showEdgesLabels
    )

    private var rawVertexPositions: Map<Vertex, Pair<Float, Float>>? = null
    private var layoutApplied = false
    private var lastCanvasW: Dp = 0.dp
    private var lastCanvasH: Dp = 0.dp
    private var lastGraphHash = graphViewModel.graph.hashCode()

    init {
        lastGraphHash = graphViewModel.graph.hashCode()
    }

    fun initializeOrUpdatePlacement(canvasW: Dp, canvasH: Dp) {
        val graphChanged = graphViewModel.graph.hashCode() != lastGraphHash
        val sizeChanged = canvasW != lastCanvasW || canvasH != lastCanvasH
        lastCanvasW = canvasW; lastCanvasH = canvasH

        if (!layoutApplied || graphChanged) applyLayout(canvasW, canvasH)
        else if (sizeChanged) reCenter(canvasW, canvasH)
    }

    fun processVertexDrag(vm: VertexViewModel, dx: Dp, dy: Dp) {
        val diameter = vm.radius * 2
        vm.x = lastCanvasW.clamp(vm.x + dx, diameter)
        vm.y = lastCanvasH.clamp(vm.y + dy, diameter)
    }

    fun resetGraphView() {
        resetColor()
        layoutApplied = false
        rawVertexPositions = null
        if (lastCanvasW > 0.dp && lastCanvasH > 0.dp)
            applyLayout(lastCanvasW, lastCanvasH)
    }

    fun runNeo4j() = withNeoDB { readFromDB(true, true).also { setNewGraph(it) } }
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

            // работающая чистка графа
    fun clearGraph() {
        val g = graphViewModel.graph
        val newG = when {
            g.isDirected() && g.isWeighted()   -> GraphFactory.createDirectedWeightedGraph()
            g.isDirected() && !g.isWeighted()  -> GraphFactory.createDirectedUnweightedGraph()
            !g.isDirected() && g.isWeighted()  -> GraphFactory.createUndirectedWeightedGraph()
            else                                -> GraphFactory.createUndirectedUnweightedGraph()
        }
        setNewGraph(newG)
        layoutApplied = true
        rawVertexPositions = emptyMap()
        updatePositions(lastCanvasW, lastCanvasH)
    }

    // Algorithm triggers
    fun runFordBellman() {
        resetColor()
        _startName.value?.let { start -> _endName.value?.let { end ->
            graphViewModel.startFordBellman(start, end)
        }}
    }

    fun runFindBridge() {
        resetColor()
        graphViewModel.startFindBridges()
    }

    // Vertex size binding
    val vertexSize: State<Float> get() = graphViewModel.vertexSize
    fun updateVertexSize(v: Float) = graphViewModel.updateVertexSize(v)

    // Credentials/state
    private var _startName = mutableStateOf<String?>(null)
    val startName: State<String?> = _startName
    private var _endName   = mutableStateOf<String?>(null)
    val endName: State<String?>   = _endName
    private var _uri      = mutableStateOf("bolt://localhost:7687")
    val uri: State<String?>       = _uri
    private var _user     = mutableStateOf("neo4j")
    val username: State<String?>  = _user
    private var _pass     = mutableStateOf("")
    val password: State<String?>  = _pass

    fun resetColor() {
        graphViewModel.vertices.forEach { it.color = Color.Gray }
        graphViewModel.edges   .forEach { it.color = Color.Gray }
    }

    private fun applyLayout(canvasW: Dp, canvasH: Dp) {
        rawVertexPositions = if (graphViewModel.graph.getVertices().isEmpty()) emptyMap()
        else representationStrategy.layout(
                graphViewModel.graph
        )
        layoutApplied = true
        lastGraphHash = graphViewModel.graph.hashCode()
        updatePositions(canvasW, canvasH)
    }

    private fun updatePositions(canvasW: Dp, canvasH: Dp) {
        rawVertexPositions
                ?.takeIf { it.isNotEmpty() }
                ?.let { positions -> applyScaled(positions, canvasW, canvasH) }
                ?: centerAll(canvasW, canvasH)
    }

    private fun computeScaleOffset(
            raw: Map<Vertex, Pair<Float,Float>>, canvasW: Dp, canvasH: Dp,
            padding: Dp = 50.dp
    ): Triple<Float, Dp, Dp> {
        var minX = Float.MAX_VALUE; var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE; var maxY = Float.MIN_VALUE
        raw.values.forEach { (x,y) ->
            minX = minOf(minX,x); maxX = maxOf(maxX,x)
            minY = minOf(minY,y); maxY = maxOf(maxY,y)
        }
        if (abs(maxX-minX)<0.001f && abs(maxY-minY)<0.001f) {
            val cx = canvasW / 2f; val cy = canvasH / 2f
            return Triple(1f, cx - padding, cy - padding)
        }

        val rawW = maxX - minX; val rawH = maxY - minY
        val tgtW = (canvasW - padding*2).coerceAtLeast(1.dp).value
        val tgtH = (canvasH - padding*2).coerceAtLeast(1.dp).value
        val scale = maxOf(0.01f, minOf(tgtW/rawW, tgtH/rawH))
        val scaledW = (rawW*scale).dp; val scaledH = (rawH*scale).dp
        val offX = padding + (canvasW - padding*2 - scaledW)/2 - (minX*scale).dp
        val offY = padding + (canvasH - padding*2 - scaledH)/2 - (minY*scale).dp
        return Triple(scale, offX, offY)
    }

    private fun applyScaled(
            raw: Map<Vertex, Pair<Float,Float>>, canvasW: Dp, canvasH: Dp
    ) {
        val (scale, offX, offY) = computeScaleOffset(raw, canvasW, canvasH)
        graphViewModel.vertices.forEach { vm ->
            raw[vm.label]?.let { (x,y) ->
                vm.x = (x*scale).dp + offX
                vm.y = (y*scale).dp + offY
            } ?: centerAll(canvasW, canvasH)
        }
    }

    private fun reCenter(canvasW: Dp, canvasH: Dp) {
        val verts = graphViewModel.vertices
        if (verts.isEmpty()) return
        var minX = Float.MAX_VALUE.dp; var maxX = Float.MIN_VALUE.dp
        var minY = Float.MAX_VALUE.dp; var maxY = Float.MIN_VALUE.dp
        verts.forEach { v ->
            minX = minOf(minX, v.x)
            maxX = maxOf(maxX, v.x + v.radius*2)
            minY = minOf(minY, v.y)
            maxY = maxOf(maxY, v.y + v.radius*2)
        }
        if (minX > maxX || minY > maxY) { centerAll(canvasW, canvasH); return }
        val cx = (minX + maxX) / 2; val cy = (minY + maxY) / 2
        val shiftX = canvasW / 2f - cx; val shiftY = canvasH / 2f - cy
        verts.forEach { vm ->
            vm.x = canvasW.clamp(vm.x + shiftX, vm.radius*2)
            vm.y = canvasH.clamp(vm.y + shiftY, vm.radius*2)
        }
    }

    private fun centerAll(canvasW: Dp, canvasH: Dp) {
        val cx = canvasW / 2f; val cy = canvasH / 2f
        graphViewModel.vertices.forEach { v ->
            v.x = cx - v.radius
            v.y = cy - v.radius
        }
    }

    // clamp a Dp position within [0, limit-diameter]
    private fun Dp.clamp(pos: Dp, diameter: Dp): Dp = pos.coerceAtLeast(0.dp)
            .coerceAtMost(this - diameter)

    // Neo4j helper
    private fun withNeoDB(action: Neo4j.() -> Unit) {
        val uri = _uri.value; val usr = _user.value
        if (uri.isNullOrBlank() || usr.isNullOrBlank()) {
            println("Neo4j: missing credentials"); return
        }
        Neo4j(uri, usr, _pass.value ?: "").action()
    }

    private fun setNewGraph(g: Graph) {
        graphViewModel = GraphViewModel(g, _showVerticesLabels, _showEdgesLabels)
        layoutApplied = false; rawVertexPositions = null
        if (lastCanvasW > 0.dp && lastCanvasH > 0.dp)
            applyLayout(lastCanvasW, lastCanvasH)
    }
}
