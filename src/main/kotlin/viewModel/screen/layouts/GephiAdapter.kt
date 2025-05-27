package viewModel.screen.layouts

import model.graph.Graph
import model.graph.Vertex
import org.gephi.graph.api.GraphModel
import org.gephi.graph.api.Node
import kotlin.random.Random

abstract class GephiAdapter {

    lateinit var graphModel: GraphModel
    lateinit var gephiGraph: org.gephi.graph.api.Graph

    fun convertToGephi(graph: Graph): HashMap<Vertex, Node> {
        val array = hashMapOf<Vertex, Node>()

        for (vertex in graph.getVertices()) {
            val node = graphModel.factory().newNode()
            node.setX(Random.nextFloat())
            node.setY(Random.nextFloat())
            gephiGraph.addNode(node)
            array[vertex] = node
        }

        for (edge in graph.getEdges()) {
            val newEdge = graphModel.factory().newEdge(
                    array[edge.source],
                    array[edge.destination],
                    graph.isDirected()
            )
            gephiGraph.addEdge(newEdge)
        }

        return array
    }

    fun convertBackWithXY(map: HashMap<Vertex, Node>): HashMap<Vertex, Pair<Float, Float>> {
        val result = hashMapOf<Vertex, Pair<Float, Float>>()

        for (node in gephiGraph.nodes) {
            val vertex = map.entries.first { it.value == node }.key
            // Возвращаем "сырые" координаты X и Y из Gephi, без масштабирования
            result[vertex] = Pair(node.x(), node.y())
        }

        return result
    }

    abstract fun apply(graph: Graph): Map<Vertex, Pair<Float, Float>>
}