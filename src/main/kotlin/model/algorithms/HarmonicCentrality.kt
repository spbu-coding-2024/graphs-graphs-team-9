package model.algorithms

import model.graph.Graph
import model.graph.Vertex
import java.util.*

// fun GraphImpl.calculateHarmonicCentrality(): Map<Vertex, Double> {
class HarmonicCentrality(graph: Graph) {
    private val vertices = graph.getVertices()
    val centrality = mutableMapOf<Vertex, Double>()

    init {
        for (vertex in vertices) {
            val distances =
                if (graph.isWeighted()) {
                    FordBellman.fordBellman(graph, vertex)
                } else {
                    bfs(graph, vertex)
                }

            var sumInverseDistances = 0.0

            for (otherVertex in vertices) {
                if (otherVertex == vertex) continue
                val distance = distances[otherVertex] ?: continue
                if (distance == Double.POSITIVE_INFINITY) continue
                sumInverseDistances += (1.0 / distance.toDouble())
            }

            centrality[vertex] = sumInverseDistances
        }
    }
}

private fun bfs(
    graph: Graph,
    startVertex: Vertex,
): Map<Vertex, Int> {
    val distances = mutableMapOf<Vertex, Int>()
    val queue: Queue<Vertex> = LinkedList()
    queue.add(startVertex)
    distances[startVertex] = 0

    while (queue.isNotEmpty()) {
        val current = queue.poll()
        val currentDistance = distances[current] ?: continue

        for (neighbor in graph.getNeighbors(current)) {
            if (!distances.containsKey(neighbor)) {
                distances[neighbor] = currentDistance + 1
                queue.add(neighbor)
            }
        }
    }

    return distances
}
