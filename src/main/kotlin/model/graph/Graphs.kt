package model.graph

interface Graph {
    fun addVertex(vertex: Vertex)
    fun removeVertex(vertex: Vertex)
    fun removeEdge(from: Vertex, to: Vertex)
    fun getNeighbors(vertex: Vertex): List<Vertex>
    fun containsVertex(vertex: Vertex): Boolean
    fun containsEdge(from: Vertex, to: Vertex): Boolean
    fun getVertexCount(): Int
    fun getEdgeCount(): Int
    fun isDirected(): Boolean
}

interface WeightedGraphInterface : Graph {
    fun addEdge(from: Vertex, to: Vertex, weight: Double)

    fun getEdgeWeight(from: Vertex, to: Vertex): Double?

    fun isWeighted(): Boolean = true
}

interface UnweightedGraphInterface : Graph {
    fun addEdge(from: Vertex, to: Vertex)

    fun isWeighted(): Boolean = false
}