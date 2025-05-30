package model.graph

interface Graph {
    fun getMap(): Map<Vertex, List<Edge>>
    fun addVertex(vertex: String)
    fun removeVertex(vertex: String)
    fun addEdge(fromName: String, toName: String, weight: Double? = null)
    fun removeEdge(fromName: String, toName: String)
    fun getEdgeWeight(from: Vertex, to: Vertex): Double?
    fun getNeighbors(vertex: Vertex): List<Vertex>
    fun containsVertex(vertex: String): Boolean
    fun containsEdge(from: Vertex, to: Vertex): Boolean
    fun getVertexCount(): Int
    fun getEdgeCount(): Int
    fun getVertices(): List<Vertex>
    fun getEdges(): List<Edge>

    fun isDirected(): Boolean
    fun isWeighted(): Boolean

    fun getVertexByKey(id: Int): Vertex?
    fun getEdgeByVertex(firstV: Vertex, secondV: Vertex): Edge?
    operator fun iterator(): Iterator<Pair<Vertex, MutableSet<Edge>>>
    fun getVertexByName(name: String): Vertex?
}
