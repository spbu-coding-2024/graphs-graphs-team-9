package model.algorithms

import model.graph.Graph
import model.graph.Vertex
import java.util.PriorityQueue

class DijkstraAlgorithm {

    data class PathResult(
        val path: List<Vertex>,
        val distance: Double
    )

    fun findShortestPath(graph: Graph, start: Vertex, end: Vertex): PathResult? {
        if (start == end) {
            return PathResult(listOf(start), 0.0)
        }

        val distances = HashMap<Vertex, Double>()
        val predecessors = HashMap<Vertex, Vertex?>()

        val priorityQueue = PriorityQueue<Pair<Vertex, Double>>(compareBy { it.second })

        for (vertex in graph.getVertices()) {
            distances[vertex] = if (vertex == start) 0.0 else Double.POSITIVE_INFINITY
            predecessors[vertex] = null
        }

        priorityQueue.add(Pair(start, 0.0))

        while (priorityQueue.isNotEmpty()) {
            val (currentVertex, currentDistance) = priorityQueue.poll()

            if (currentVertex == end) {
                break
            }

            if (currentDistance > distances[currentVertex]!!) {
                continue
            }

            for (neighbor in graph.getNeighbors(currentVertex)) {
                val edgeWeight = graph.getEdgeWeight(currentVertex, neighbor)
                        ?: continue

                if (edgeWeight < 0) {
                    throw IllegalArgumentException("Dijkstra's algorithm does not support negative edge weights")
                }

                val distanceThroughCurrent = distances[currentVertex]!! + edgeWeight

                if (distanceThroughCurrent < distances[neighbor]!!) {
                    distances[neighbor] = distanceThroughCurrent
                    predecessors[neighbor] = currentVertex

                    priorityQueue.add(Pair(neighbor, distanceThroughCurrent))
                }
            }
        }

        if (predecessors[end] == null) {
            return null
        }

        val path = reconstructPath(start, end, predecessors)

        return PathResult(path, distances[end]!!)
    }

    private fun reconstructPath(start: Vertex, end: Vertex, predecessors: Map<Vertex, Vertex?>): List<Vertex> {
        val path = mutableListOf<Vertex>()
        var current: Vertex? = end

        while (current != null) {
            path.add(0, current)

            if (current == start) {
                break
            }

            current = predecessors[current]
        }

        return path
    }
}