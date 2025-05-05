package viewModel

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.graph.Edge
import model.graph.Graph
import model.graph.UnweightedGraph
import model.graph.Vertex
import kotlin.time.Duration.Companion.seconds

class GraphViewModel(
    private val graph: Graph,
//    private val placement: MutableMap<Vertex, Pair<Dp?, Dp?>?>,
    showVerticesLabels: State<Boolean>,
    showEdgesLabels: State<Boolean>,
){
    private val _vertices = graph.getVertex().associateWith { v ->
        VertexViewModel(0.dp, 0.dp, Color.Gray, v, showVerticesLabels)
    }

    private val _edges = graph.getEdge().associateWith { e ->
        val fst = _vertices[e.first.first]
            ?: throw IllegalStateException("VertexView for ${e.first.first} not found")
        val snd = _vertices[e.first.second]
            ?: throw IllegalStateException("VertexView for ${e.first.second} not found")
        EdgeViewModel(fst, snd, Color.Gray, Edge(e.first, e.second), showVerticesLabels, showEdgesLabels, e.second)
    }

    val vertices: Collection<VertexViewModel>
        get() = _vertices.values

    val edges: Collection<EdgeViewModel>
        get() = _edges.values
}