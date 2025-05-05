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

    fun getEdge() : List<Pair<Pair<Vertex, Vertex>, Double?>> {
        val mas: MutableList<Pair<Pair<Vertex, Vertex>, Double?>> = mutableListOf()
        for (v_f in adjList){
            for (v_s in v_f.value){
                mas.add(Pair(Pair(v_f.key, v_s.first), v_s.second))
            }
        }
        return mas.toSet().toList()
    }

    fun getVertex(): List<Vertex> {
        val vertexes: MutableList<Vertex> = mutableListOf()
        for (v in getEdge()){
            vertexes.add(v.first.first)
            vertexes.add(v.first.second)
        }
        return vertexes.toSet().toList()
    }

    inner class Iterate : Iterator<Pair<Vertex, MutableList<Pair<Vertex, Double>>?>> {
        var array: ArrayDeque<Pair<Vertex, MutableList<Pair<Vertex, Double>>?>> = ArrayDeque()
        var f: Boolean = true

        override fun next(): Pair<Vertex, MutableList<Pair<Vertex, Double>>?> {
            return array.removeFirst()
        }

        override fun hasNext(): Boolean {
            if (f) {
                getGraph()
                f = false
            }
            return array.isNotEmpty()
        }

        fun getGraph() {
            for (el in adjList) {
                array.add(el.toPair())
            }
        }
    }

    operator fun iterator(): Iterate {
        return this.Iterate()
    }
}