package viewModel.screen.layouts

import model.graph.Graph
import model.graph.Vertex
import model.graph.GraphImpl
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.GraphModel
import org.gephi.graph.api.Node
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import java.awt.Toolkit
import kotlin.random.Random

abstract class GephiAdapter {
    val width = Toolkit.getDefaultToolkit().screenSize.width
    val height = Toolkit.getDefaultToolkit().screenSize.height
    var graphModel: GraphModel = Lookup.getDefault().lookup(GraphController::class.java).graphModel

    var gephiGraph: org.gephi.graph.api.Graph = graphModel.directedGraph

    fun convertToGephi(graph: Graph): HashMap<Vertex, Node> {
        var array = hashMapOf<Vertex, Node>()

        for (vertex in graph.getVertices()) {
            array[vertex] = graphModel.factory().newNode()
            array[vertex]?.setX(Random.nextFloat())
            array[vertex]?.setY(Random.nextFloat())
            gephiGraph.addNode(array[vertex])
        }

        for (edge in graph.getEdges()) {
            gephiGraph.addEdge(graphModel.factory().newEdge(array[edge.source], array[edge.destination]))
        }

        return array
    }

    fun convertBackWithXY(map: HashMap<Vertex, Node>): HashMap<Vertex, Pair<Float, Float>> {
        val result = hashMapOf<Vertex, Pair<Float, Float>>()

        for (node in gephiGraph.nodes) {
            val vertex = map.filter { it.value == node }.keys.first()
            result[vertex] = Pair(node.x() * width, node.y() * height)
        }
        return result

    }
    abstract fun apply(graph: Graph): Map<Vertex, Pair<Float, Float>>
}