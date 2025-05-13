package model.algorithms

import model.graph.Graph
import model.graph.Vertex

object FordBellman {

    fun fordBellman(graph: Graph, startVertex: Vertex, endVertex: Vertex? = null): Pair<List<Vertex>, Double> {
        val infinity = Double.POSITIVE_INFINITY
        val distance =
            graph.getVertices().associateWith { if (it == startVertex) 0.0 else infinity }.toMutableMap()
        val previousVertex: MutableMap<Vertex, Vertex?> = mutableMapOf()

        for (i in 1 until graph.getVertices().size) {
            for (entry in graph) {

                for (edge in entry.second) {
                    val weight = edge.weight ?: 0.0

                    if ((distance[entry.first] != null && distance[edge.destination] != null) &&
                        (distance[entry.first] ?: 0.0) + weight < (distance[edge.destination] ?: 0.0)
                    ) {
                        distance[edge.destination] = (distance[entry.first] ?: 0.0) + weight
                        previousVertex[edge.destination] = entry.first
                    }
                }
            }
        }

        for (entry in graph) {
            for (edge in entry.second) {
                val weight = edge.weight ?: 0.0
                if ((distance[entry.first] != null && distance[edge.destination] != null) &&
                    (distance[entry.first] ?: 0.0) + weight < (distance[edge.destination] ?: 0.0)
                ) {
                    error("Граф содержит отрицательный цикл")
                }
            }
        }
        if (endVertex == null) return distance.keys.toList() to distance.values.sum()
        val path: MutableList<Vertex> = mutableListOf()
        var cur: Vertex? = endVertex
        while (cur != null) {
            path.add(cur)
            cur = previousVertex[cur]
        }
        path.reverse()

        return path to (distance[endVertex] ?: infinity)
    }
}
//object FordBellman{
//    fun fordBellman(graph: Graph, vertexFirst: Vertex, vertexSecond: Vertex): Pair<Double, List<Vertex>> {
//        val infinity = Double.POSITIVE_INFINITY
//        val distance =
//            graph.getVertices().associateWith { if (it == vertexFirst) 0.0 else infinity }.toMutableMap()
//        val previousVertex: MutableMap<Vertex, Vertex?> = mutableMapOf()
//
//        for (i in 1 until graph.getVertices().size) {
//            for (edges in graph) {
//                val ver1 = distance[edges.first]
//                for (e in edges.second) {
//                    val ver2 = distance[e.destination]
//                    if ((ver1 != null && ver2 != null) && (ver1 + (e.weight ?: 0.0) < ver2)) {
//                        distance[e.destination] = ver1 + (e.weight ?: 0.0)
//                        previousVertex[e.destination] = edges.first
//                    }
//                }
//            }
//        }
//        for (entry in graph) {
//            for (edge in entry.second) {
//                val weight = edge.weight ?: 0.0
//                if ((distance[entry.first] != null && distance[edge.destination] != null) &&
//                    (distance[entry.first] ?: 0.0) + weight < (distance[edge.destination] ?: 0.0)
//                ) {
//                    error("Граф содержит отрицательный цикл")
//                }
//            }
//        }
//        val pathStartToEnd: Pair<Double, MutableList<Vertex>> = Pair(0.0, mutableListOf(vertexSecond))
//        var cur = vertexSecond
//        var prev = previousVertex[cur]
//        while (prev != vertexFirst){
//            // что-то сделать с циклом
//            pathStartToEnd.second.add(prev ?: return Pair(0.0, emptyList()))
//            cur = prev
//            prev = previousVertex[cur]
//        }
//        pathStartToEnd.second.reversed()
//
//        return Pair(pathStartToEnd.first, pathStartToEnd.second.toList())
//    }}