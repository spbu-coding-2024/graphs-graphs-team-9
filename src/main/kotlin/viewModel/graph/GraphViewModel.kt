package viewModel.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
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
        private val _showVerticesLabels: State<Boolean>,
        private val _showEdgesLabels: State<Boolean>
) {
    val showVerticesLabels = _showVerticesLabels
    val showEdgesLabels = _showEdgesLabels

    private val _vertices = mutableStateOf(
            graph.getVertices().associateWith { v ->
                VertexViewModel(
                        0.dp, 0.dp, Color.Gray, v, showVerticesLabels
                )
            }
    )

    private val _edges = mutableStateOf(
            createEdgesViewModels(graph.getEdges(), showVerticesLabels, showEdgesLabels)
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
        refreshVertices()
        refreshEdges()
    }

    fun refreshVertices() {
        val oldVMs = _vertices.value
        val newVerticesMap = graph.getVertices().associateWith { vertex ->
            oldVMs[vertex] ?: VertexViewModel(
                    0.dp, 0.dp, Color.Gray, vertex, showVerticesLabels
            )
        }
        _vertices.value = newVerticesMap
    }


    fun refreshEdges() {
        val showVerticesLabels = _vertices.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)
        val showEdgesLabels = _edges.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)

        _edges.value = createEdgesViewModels(graph.getEdges(), showVerticesLabels, showEdgesLabels)
    }

    private fun createEdgesViewModels(
            edges: List<Edge>,
            showVerticesLabels: State<Boolean>,
            showEdgesLabels: State<Boolean>
    ): Map<Edge, EdgeViewModel> {
        return edges.associateWith { e ->
            val fst = _vertices.value[e.source]
                    ?: throw IllegalStateException("VertexView for ${e.source} not found")
            val snd = _vertices.value[e.destination]
                    ?: throw IllegalStateException("VertexView for ${e.destination} not found")
            EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination), showVerticesLabels, showEdgesLabels, e.weight)
        }
    }

    suspend fun startFordBellman(startName: String?, endName: String?): Pair<List<Vertex>?, Double?> {
        val startVertex = graph.getVertexByName(startName ?: "") ?: return null to null
        val endVertex = graph.getVertexByName(endName ?: "") ?: return null to null
        return withContext(Dispatchers.Default) {
            FordBellman.fordBellman(graph, startVertex, endVertex)
        }
    }

    fun highlightFordBellmanPath(result: Pair<List<Vertex>?, Double?>) {
        val path = result.first
        if (path == null) {
            return
        }
        for (i in path.indices) {
            _vertices.value[path[i]]?.color = Color(93, 167, 250)
            if (i + 1 < path.size) {
                val edge = graph.getEdgeByVertex(path[i], path[i+1])
                if(edge != null) _edges.value[edge]?.color = Color.Blue
            }
        }
    }

    suspend fun startFindBridges(): List<Pair<Vertex, Vertex>> {
        return withContext(Dispatchers.Default) {
            FindBridges(graph).findBridges()
        }
    }

    fun highlightBridges(bridges: List<Pair<Vertex, Vertex>>) {
        bridges.forEach { edgePair ->
            val edge = graph.getEdgeByVertex(edgePair.first, edgePair.second)
            if (edge != null) {
                _edges.value[edge]?.color = Color(93, 167, 250)
            }
            // Для неориентированных графов, мост может быть сохранен в обратном направлении
            val reverseEdge = graph.getEdgeByVertex(edgePair.second, edgePair.first)
            if (reverseEdge != null) {
                _edges.value[reverseEdge]?.color = Color(93, 167, 250)
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
        for (i in path.indices) {
            _vertices.value[path[i]]?.color = Color(93, 167, 250)
            if (i + 1 < path.size) {
                val edge = graph.getEdgeByVertex(path[i], path[i+1])
                if(edge != null) _edges.value[edge]?.color = Color.Blue
            }
        }
    }

    suspend fun startTarjan(): List<Set<Vertex>> {
        return withContext(Dispatchers.Default) {
            TarjanAlgorithm().findStronglyConnectedComponents(graph)
        }
    }

    fun highlightTarjanComponents(components: List<Set<Vertex>>) {
        components.forEach { s ->
            val color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            s.forEach { v ->
                _vertices.value[v]?.color = color
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
        graph = Neo4jRepository(uri, username, password).readFromDB(isDirected, isWeighted)
        // доработать
    }

    fun clearGraph(){
        graph = GraphImpl(isWeighted = graph.isWeighted(), isDirected = graph.isDirected())
        updateGraph(graph)
        // доработать
    }

    private val _vertexSize = mutableStateOf(25f)
    val vertexSize: State<Float>
        get() = _vertexSize

    fun updateVertexSize(newSize: Float) {
        _vertexSize.value += (newSize - _vertexSize.value )
        _vertices.value.values.forEach { vertex ->
            vertex.radius = newSize.dp
        }
    }

    fun isDirected(): Boolean{
        return graph.isDirected()
    }

    fun isWeighted(): Boolean{
        return graph.isWeighted()
    }

    fun updateGraph(newGraph: Graph) {
        graph = newGraph
        _vertices.value = updateVertices(showVerticesLabels)
        _edges.value = updateEdges(showVerticesLabels, showEdgesLabels)
    }

    private fun updateVertices(showVerticesLabels: State<Boolean>): Map<Vertex, VertexViewModel> {
        val oldVMs = _vertices.value
        return graph.getVertices().associateWith { v ->
            oldVMs[v] ?: VertexViewModel(0.dp, 0.dp, Color.Gray, v, showVerticesLabels)
        }
    }

    private fun updateEdges(
            showVerticesLabels: State<Boolean>,
            showEdgesLabels: State<Boolean>
    ): Map<Edge, EdgeViewModel> {
        return graph.getEdges().associateWith { e ->
            val fst = _vertices.value[e.source]
                    ?: throw IllegalStateException("VertexView for ${e.source} not found")
            val snd = _vertices.value[e.destination]
                    ?: throw IllegalStateException("VertexView for ${e.destination} not found")
            EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination),
                    showVerticesLabels, showEdgesLabels, e.weight)
        }
    }
}
