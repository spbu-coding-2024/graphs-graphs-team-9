package viewModel.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.algorithms.FindBridges
import model.algorithms.FordBellman
import model.graph.Edge
import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex
import model.io.Neo4j.Neo4j


class GraphViewModel(
        var graph: Graph,
        showVerticesLabels: State<Boolean>,
        showEdgesLabels: State<Boolean>
) {

    private var _vertices = graph.getVertices().associateWith { v ->
        VertexViewModel(
                0.dp, 0.dp, Color.Gray, v, showVerticesLabels
        )
    }
    private var _edges = graph.getEdges().associateWith { e ->
        val fst = _vertices[e.source]
                ?: throw IllegalStateException("VertexView for ${e.source} not found")
        val snd = _vertices[e.destination]
                ?: throw IllegalStateException("VertexView for ${e.destination} not found")
        EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination), showVerticesLabels, showEdgesLabels, e.weight)
    }

    val vertices: Collection<VertexViewModel>
        get() = _vertices.values

    val edges: Collection<EdgeViewModel>
        get() = _edges.values

    fun startFordBellman(startName: String?, endName: String?): Boolean {
        val bellman = FordBellman.fordBellman(graph, graph.getVertexByName(startName ?: ""), graph.getVertexByName(endName ?: ""))
        val path = bellman.first ?: return false

        for (i in 0..path.size - 1) {
            _vertices[path[i]]?.color = Color.Red
            if (i + 1 != path.size) {
                _edges[graph.getEdgeByVertex(path[i], path[i + 1])]?.color = Color.Yellow
            }
        }
        return true
    }
    fun startFindBridges(){
        val bridges = FindBridges(graph).findBridges()
        bridges.forEach{ edge ->
            _edges[graph.getEdgeByVertex(edge.first, edge.second)]?.color = Color.Red
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
        _vertices.values.forEach { vertex ->
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
            val fst = _vertices[e.source]
                ?: throw IllegalStateException("VertexView for ${e.source} not found")
            val snd = _vertices[e.destination]
                ?: throw IllegalStateException("VertexView for ${e.destination} not found")
            EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination),
                showVerticesLabels, showEdgesLabels, e.weight)
        }
    }

    val showVerticesLabels1 = showVerticesLabels
    val showEdgesLabels1 = showEdgesLabels

    fun updateGraph(newGraph: Graph) {
        graph = newGraph
        _vertices = updateVertices(showVerticesLabels1)
        _edges = updateEdges(showVerticesLabels1, showEdgesLabels1)
    }
}