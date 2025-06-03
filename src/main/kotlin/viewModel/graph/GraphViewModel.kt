package viewModel.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import viewModel.toosl.CoolColors
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.algorithms.*
import model.graph.Edge
import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex
import model.io.Neo4j.Neo4jRepository
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphViewModel(
        var graph: Graph,
        private val _showVerticesLabelsInput: State<Boolean>,
        private val _showEdgesLabelsInput: State<Boolean>
) {
    val showVerticesLabels: State<Boolean> = _showVerticesLabelsInput
    val showEdgesLabels: State<Boolean> = _showEdgesLabelsInput

    private val _vertexSize = mutableStateOf(25f)
    val vertexSize: State<Float>
        get() = _vertexSize

    private val currentDefaultVertexRadiusDp: Dp
        get() = _vertexSize.value.dp

    private fun createVertexViewModels(
            vertices: List<Vertex>,
            showLabelsState: State<Boolean>,
            defaultColor: Color,
            defaultRadius: Dp
    ): Map<Vertex, VertexViewModel> {
        return vertices.associateWith { v ->
            VertexViewModel(
                    0.dp,
                    0.dp,
                    defaultColor,
                    v,
                    showLabelsState
            ).apply {
                radius = defaultRadius
            }
        }
    }

    private fun createEdgeViewModels(
            edges: List<Edge>,
            targetVertexViewModels: Map<Vertex, VertexViewModel>,
            showVerticesLabelsState: State<Boolean>,
            showEdgesLabelsState: State<Boolean>,
            defaultColor: Color
    ): Map<Edge, EdgeViewModel> {
        return edges.associateWith { e ->
            val fst = targetVertexViewModels[e.source]
                    ?: throw IllegalStateException("VertexView for source vertex ${e.source.name} not found when creating edge VMs.")
            val snd = targetVertexViewModels[e.destination]
                    ?: throw IllegalStateException("VertexView for destination vertex ${e.destination.name} not found when creating edge VMs.")

            EdgeViewModel(
                    fst,
                    snd,
                    defaultColor,
                    Edge(e.source, e.destination),
                    showVerticesLabelsState,
                    showEdgesLabelsState,
                    e.weight
            )
        }
    }

    private var _vertices = mutableStateOf(
            createVertexViewModels(
                    graph.getVertices(),
                    this.showVerticesLabels,
                    CoolColors.vertexBasic,
                    this.currentDefaultVertexRadiusDp
            )
    )

    private var _edges = mutableStateOf(
            createEdgeViewModels(
                    graph.getEdges(),
                    _vertices.value,
                    this.showVerticesLabels,
                    this.showEdgesLabels,
                    CoolColors.edgeBasic
            )
    )

    val vertices: Collection<VertexViewModel>
        get() = _vertices.value.values

    val edges: Collection<EdgeViewModel>
        get() = _edges.value.values

    val verticesMap: Map<Vertex, VertexViewModel>
        get() = _vertices.value

    val edgesMap: Map<Edge, EdgeViewModel>
        get() = _edges.value

    fun refreshGraph() {
        val newVertexVMs = createVertexViewModels(
                this.graph.getVertices(),
                this.showVerticesLabels,
                CoolColors.vertexBasic,
                this.currentDefaultVertexRadiusDp
        )
        _vertices.value = newVertexVMs

        _edges.value = createEdgeViewModels(
                this.graph.getEdges(),
                newVertexVMs,
                this.showVerticesLabels,
                this.showEdgesLabels,
                CoolColors.edgeBasic
        )
    }

    fun updateGraph(newGraph: Graph) {
        this.graph = newGraph
        refreshGraph()
    }

    suspend fun startFordBellman(startName: String?, endName: String?): Pair<List<Vertex>?, Double?> {
        val startVertex = graph.getVertexByName(startName ?: "") ?: return null to null
        val endVertex = graph.getVertexByName(endName ?: "") ?: return null to null
        return withContext(Dispatchers.Default) {
            FordBellman.fordBellman(graph, startVertex, endVertex)
        }
    }

    fun highlightFordBellmanPath(result: Pair<List<Vertex>?, Double?>) {
        val path = result.first ?: return
        path.forEachIndexed { i, vertex ->
            _vertices.value[vertex]?.color = CoolColors.pathHighlightVertex
            if (i + 1 < path.size) {
                graph.getEdgeByVertex(vertex, path[i + 1])?.let { edge ->
                    _edges.value[edge]?.color = CoolColors.pathHighlightEdge
                }
            }
        }
    }

    suspend fun startFindBridges(): List<Pair<Vertex, Vertex>> {
        return withContext(Dispatchers.Default) {
            FindBridges(graph).findBridges()
        }
    }

    fun highlightBridges(bridges: List<Pair<Vertex, Vertex>>) {
        val bridgeColor = CoolColors.pathHighlightVertex
        bridges.forEach { edgePair ->
            graph.getEdgeByVertex(edgePair.first, edgePair.second)?.let {
                _edges.value[it]?.color = bridgeColor
            }
            graph.getEdgeByVertex(edgePair.second, edgePair.first)?.let {
                _edges.value[it]?.color = bridgeColor
            }
        }
    }

    suspend fun startDijkstra(start: String, end: String): DijkstraAlgorithm.PathResult? {
        val startVertex = graph.getVertexByName(start) ?: return null
        val endVertex = graph.getVertexByName(end) ?: return null
        return withContext(Dispatchers.Default) {
            DijkstraAlgorithm().findShortestPath(graph, startVertex, endVertex)
        }
    }

    fun highlightDijkstraPath(path: List<Vertex>) {
        path.forEachIndexed { i, vertex ->
            _vertices.value[vertex]?.color = CoolColors.pathHighlightVertex
            if (i + 1 < path.size) {
                graph.getEdgeByVertex(vertex, path[i + 1])?.let { edge ->
                    _edges.value[edge]?.color = CoolColors.pathHighlightEdge
                }
            }
        }
    }

    suspend fun startTarjan(): List<Set<Vertex>> {
        return withContext(Dispatchers.Default) {
            TarjanAlgorithm().findStronglyConnectedComponents(graph)
        }
    }

    fun highlightTarjanComponents(components: List<Set<Vertex>>) {
        components.forEach { scc ->
            val componentColor = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            scc.forEach { v ->
                _vertices.value[v]?.color = componentColor
            }
        }
    }

    suspend fun startFindKeyVertex(): Map<Vertex, Double> {
        return withContext(Dispatchers.Default) {
            HarmonicCentrality(graph).centrality
        }
    }

    fun applyKeyVertexVisuals(centrality: Map<Vertex, Double>) {
        val minCentrality = centrality.values.minOrNull() ?: 0.0
        val maxCentrality = centrality.values.maxOrNull() ?: 1.0
        val minSize = 25.dp

        _vertices.value.forEach { (vertex, viewModel) ->
            val normalizedCentrality = if (maxCentrality - minCentrality != 0.0) {
                ((centrality[vertex] ?: 0.0) - minCentrality) / (maxCentrality - minCentrality)
            } else {
                0.5
            }
            val newSize = minSize + minSize * normalizedCentrality.toFloat()
            viewModel.radius = newSize
        }
    }

    suspend fun startNeo4j(uri: String, username: String, password: String, isDirected: Boolean, isWeighted: Boolean){
        val newGraph = Neo4jRepository(uri, username, password).readFromDB(isDirected, isWeighted)
        updateGraph(newGraph)
    }

    fun clearGraph(){
        val newGraph = GraphImpl(isWeighted = graph.isWeighted(), isDirected = graph.isDirected())
        updateGraph(newGraph)
    }

    fun updateVertexSize(newSize: Float) {
        _vertexSize.value = newSize
        _vertices.value.values.forEach { vertexVM ->
            vertexVM.radius = newSize.dp
        }
    }

    fun isDirected(): Boolean{
        return graph.isDirected()
    }

    fun isWeighted(): Boolean{
        return graph.isWeighted()
    }
}
