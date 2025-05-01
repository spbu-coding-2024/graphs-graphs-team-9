package model.graph

class WeightedGraph(private val isDirected: Boolean) : WeightedGraphInterface {
    private val adjList: MutableMap<Vertex, MutableList<Pair<Vertex, Double>>> = mutableMapOf()

    override fun addVertex(vertex: Vertex) {
        if (!adjList.containsKey(vertex)) {
            adjList[vertex] = mutableListOf()
        }
    }

    override fun removeVertex(vertex: Vertex) {
        adjList.remove(vertex)
        adjList.values.forEach { neighbors ->
            neighbors.removeAll { it.first == vertex }
        }
    }

    override fun addEdge(from: Vertex, to: Vertex, weight: Double) {
        if (!containsVertex(from) || !containsVertex(to)) {
            return
        }
        adjList.getOrPut(from) { mutableListOf() }.add(Pair(to, weight))
        if (!isDirected) {
            adjList.getOrPut(to) { mutableListOf() }.add(Pair(from, weight))
        }
    }

    override fun removeEdge(from: Vertex, to: Vertex) {
        adjList[from]?.removeAll { it.first == to }
        if (!isDirected) {
            adjList[to]?.removeAll { it.first == from }
        }
    }

    override fun getNeighbors(vertex: Vertex): List<Vertex> {
        return adjList[vertex]?.map { it.first } ?: emptyList()
    }

    override fun getEdgeWeight(from: Vertex, to: Vertex): Double? {
        if (!adjList.containsKey(from)) {
            return null
        }
        val edgePair = adjList[from]?.find { neighborPair -> neighborPair.first == to }

        return edgePair?.second
    }

    override fun containsVertex(vertex: Vertex): Boolean {
        return adjList.containsKey(vertex)
    }

    override fun containsEdge(from: Vertex, to: Vertex): Boolean {
        return adjList[from]?.any { it.first == to } ?: false
    }

    override fun getVertexCount(): Int {
        return adjList.size
    }

    override fun getEdgeCount(): Int {
        val totalEdges = adjList.values.sumOf { it.size }
        return if (isDirected) totalEdges else totalEdges / 2
    }

    override fun isDirected(): Boolean {
        return isDirected
    }
}