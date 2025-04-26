package model.graph;

public class Graph (var isDirected: Boolean = false) {
    val edges = hashMapOf<Int, Edge>()
    val vertexes = hashMapOf<Int, Vertex>()

    var edgeCount = 0

    fun addEdge(firstId: Int, secondId: Int, weight: Double) {
        if (vertexes[firstId] == null || vertexes[secondId] == null)
            return // Нужен вывод ошибок

        edges.getOrPut(firstId + secondId) { Edge(edgeCount, weight, Pair(vertexes[firstId], vertexes[secondId])) }
        edgeCount++
    }

    fun addVertex(id: Int, name: String) {
        vertexes.getOrPut(id) {Vertex(id, name)}
    }

    fun printG(){
        for (i in -5.. edgeCount + 10)
            print(edges[i]?.connect?.first?.name + " " + edges[i]?.connect?.second?.name + "\n")
    }
}