package viewModel.graph

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.algorithms.DijkstraAlgorithm
import model.algorithms.FindBridges
import model.algorithms.FordBellman
import model.algorithms.TarjanAlgorithm
import model.graph.Edge
import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import kotlin.random.Random


class GraphViewModel(
        var graph: Graph,
        showVerticesLabels: State<Boolean>,
        showEdgesLabels: State<Boolean>
) {
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

//    private var _edges = graph.getEdges().associateWith { e ->
//        val fst = _vertices[e.source]
//                ?: throw IllegalStateException("VertexView for ${e.source} not found")
//        val snd = _vertices[e.destination]
//                ?: throw IllegalStateException("VertexView for ${e.destination} not found")
//        EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination), showVerticesLabels, showEdgesLabels, e.weight)
//    }

    val vertices: Collection<VertexViewModel>
        get() = _vertices.value.values

    val edges: Collection<EdgeViewModel>
        get() = _edges.value.values

    fun refreshGraph() {
        refreshVertices()
        refreshEdges()
    }

    fun refreshEdges() {
        val showVerticesLabels = _vertices.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)
        val showEdgesLabels = _edges.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)

        _edges.value = createEdgesViewModels(graph.getEdges(), showVerticesLabels, showEdgesLabels)
    }

    fun refreshVertices() {
        val currentVertices = _vertices.value.toMutableMap()
        val state = showVerticesLabels1

        graph.getVertices().forEach { vertex ->
            if (!currentVertices.containsKey(vertex)) {
                currentVertices[vertex] = VertexViewModel(
                        0.dp, 0.dp, Color.Gray, vertex, showVerticesLabels1
                )
            }
        }

        val graphVertices = graph.getVertices().toSet()
        currentVertices.keys.retainAll(graphVertices)

        _vertices.value = currentVertices
    }

    fun startFordBellman(startName: String?, endName: String?) {
        val bellman = FordBellman.fordBellman(graph, graph.getVertexByName(startName ?: ""), graph.getVertexByName(endName ?: ""))
        val path = bellman.first ?: return

        for (i in 0..path.size - 1) {
            _vertices.value[path[i]]?.color = Color.Red
            _vertices.value[path[i]]?.color = Color.Cyan
            if (i + 1 != path.size) {
                _edges.value[graph.getEdgeByVertex(path[i], path[i + 1])]?.color = Color.Blue
            }
        }
    }
    fun startFindBridges(){
        val bridges = FindBridges(graph).findBridges()
        bridges.forEach{ edge ->
            _edges.value[graph.getEdgeByVertex(edge.first, edge.second)]?.color = Color.Cyan
        }
    }
    fun startDijkstra(start: String, end: String){
        val d = DijkstraAlgorithm().findShortestPath(graph, graph.getVertexByName(start), graph.getVertexByName(end))
        val path = d?.path ?: return

        for (i in 0..path.size - 1) {
            _vertices.value[path[i]]?.color = Color.Cyan
            if (i + 1 != path.size) {
                _edges.value[graph.getEdgeByVertex(path[i], path[i + 1])]?.color = Color.Blue
            }
        }
    }
    fun startTarjan(){
        val T = TarjanAlgorithm().findStronglyConnectedComponents(graph)
        T.forEach { s ->
            val color = Color(Random.nextInt() % 256, Random.nextInt() % 256, Random.nextInt() % 256)
            s.forEach { v ->
                _vertices.value[v]?.color = color
            }
        }
    }
    fun startNeo4j(uri: String, username: String, password: String){
        graph = Neo4j(uri, username, password).readFromDB(graph.isDirected(), graph.isWeighted())
    }

    fun clearGraph(){
        graph = GraphImpl(isWeighted = graph.isWeighted(), isDirected = graph.isDirected())
    }

    private val _vertexSize = mutableStateOf(25f)
    val vertexSize: State<Float>
        get() = _vertexSize

    fun updateVertexSize(newSize: Float) {
        _vertexSize.value = newSize
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

    private fun updateVertices(showVerticesLabels: State<Boolean>): Map<Vertex, VertexViewModel> {
        return graph.getVertices().associateWith { v ->
            VertexViewModel(0.dp, 0.dp, Color.Gray, v, showVerticesLabels)
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

    val showVerticesLabels1 = showVerticesLabels
    val showEdgesLabels1 = showEdgesLabels

    fun updateGraph(newGraph: Graph) {
        graph = newGraph
        _vertices.value = updateVertices(showVerticesLabels1)
        _edges.value = updateEdges(showVerticesLabels1, showEdgesLabels1)
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
}