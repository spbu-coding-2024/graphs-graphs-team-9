package model.algorithms

import model.graph.Graph
import model.graph.Vertex
import java.util.*

class TarjanAlgorithm {
    fun findStronglyConnectedComponents(graph: Graph): List<Set<Vertex>> {
        val result = mutableListOf<Set<Vertex>>()

        val index = mutableMapOf<Vertex, Int>()
        val lowLink = mutableMapOf<Vertex, Int>()

        val onStack = mutableSetOf<Vertex>()
        val stack = Stack<Vertex>()

        var currentIndex = 0

        fun strongConnect(vertex: Vertex) {
            index[vertex] = currentIndex
            lowLink[vertex] = currentIndex
            currentIndex++

            stack.push(vertex)
            onStack.add(vertex)

            for (neighbor in graph.getNeighbors(vertex)) {
                if (!index.containsKey(neighbor)) {
                    strongConnect(neighbor)
                    lowLink[vertex] =
                        minOf(
                            lowLink.getValue(vertex),
                            lowLink.getValue(neighbor),
                        )
                } else if (onStack.contains(neighbor)) {
                    lowLink[vertex] =
                        minOf(
                            lowLink.getValue(vertex),
                            lowLink.getValue(neighbor),
                        )
                }
            }

            if (lowLink.getValue(vertex) == index.getValue(vertex)) {
                val component = mutableSetOf<Vertex>()
                var w: Vertex

                do {
                    w = stack.pop()
                    onStack.remove(w)
                    component.add(w)
                } while (w != vertex)

                result.add(component)
            }
        }

        for (vertex in graph.getVertices()) {
            if (!index.containsKey(vertex)) {
                strongConnect(vertex)
            }
        }

        return result
    }
}
