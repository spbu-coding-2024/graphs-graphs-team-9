package viewModel

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.graph.Edge
import model.graph.Graph
import model.graph.Vertex
import kotlin.time.Duration.Companion.seconds

class GraphViewModel(
    private val graph: Graph,
//    private val placement: MutableMap<Vertex, Pair<Dp?, Dp?>?>,
    showVerticesLabels: State<Boolean>,
    showEdgesLabels: State<Boolean>,
){
    private val _vertices = graph.getVertices().associateWith { v ->
        VertexViewModel(0.dp, 0.dp, Color.Gray, v, showVerticesLabels)
    }

    private val _edges = graph.getEdges().associateWith { e ->
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
}
