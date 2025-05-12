package model.algorithms

import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex

class FordBellman(val graph: Graph, private val vertexFirst: Vertex, private val vertexSecond: Vertex? = null) {
    private val infinity = Double.POSITIVE_INFINITY
    private val distance =
        graph.getVertices().associateWith { if (it == vertexFirst) 0.0 else infinity }.toMutableMap()
    private val previousVertex: MutableMap<Vertex, Vertex?> = mutableMapOf()

    public fun fordBellman(): Pair<Double, List<Vertex>> {
        for (i in 1 until distance.size) {
            for (edges in graph) {
                val ver1 = distance[edges.first]
                for (e in edges.second) {
                    val ver2 = distance[e.destination]
                    if ((ver1 != null && ver2 != null) && (ver1 + (e.weight ?: 0.0) < ver2)) {
                        distance[e.source] = ver1 + (e.weight ?: 0.0)
                        previousVertex[e.source] = edges.first
                    }
                }
            }
        }

        if (vertexSecond == null) return Pair(0.0, emptyList())
        val pathStartToEnd: Pair<Double, MutableList<Vertex>> = Pair(0.0, mutableListOf(vertexSecond))
        var cur = vertexSecond
        var prev = previousVertex[cur]
        while (prev != vertexFirst){
            // что-то сделать с циклом
            pathStartToEnd.second.add(prev ?: return Pair(0.0, emptyList()))
            cur = prev
            prev = previousVertex[cur]
        }
        pathStartToEnd.second.reversed()


        for (edges in graph) {
            val ver1 = distance[edges.first]
            for (e in edges.second) {
                val ver2 = distance[e.destination]
                if ((ver1 != null && ver2 != null) && (ver1 + (e.weight ?: 0.0) < ver2)) {
                    if (pathStartToEnd.second.indexOf(edges.first) < pathStartToEnd.second.indexOf(e.destination))
                        error("Граф содержит отрицательный цикл")
                }
            }
        }
        return Pair(pathStartToEnd.first, pathStartToEnd.second.toList())
    }
}