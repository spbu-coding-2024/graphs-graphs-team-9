package model.graph

interface Graph {
    fun addVertex(vertex: Vertex)
    fun removeVertex(vertex: Vertex)
    fun addEdge(from: Vertex, to: Vertex, weight: Double? = null)
    fun removeEdge(from: Vertex, to: Vertex)
    fun getEdgeWeight(from: Vertex, to: Vertex): Double?
    fun getNeighbors(vertex: Vertex): List<Vertex>
    fun containsVertex(vertex: Vertex): Boolean
    fun containsEdge(from: Vertex, to: Vertex): Boolean
    fun getVertexCount(): Int
    fun getEdgeCount(): Int
    fun getVertices(): List<Vertex>
    fun getEdges(): List<Edge>

    fun isDirected(): Boolean
    fun isWeighted(): Boolean
}