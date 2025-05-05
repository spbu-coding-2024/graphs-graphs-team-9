package model.graph

class UnweightedGraph(private val isDirected: Boolean) : UnweightedGraphInterface {
    private val adjList: MutableMap<Vertex, MutableList<Vertex>> = mutableMapOf()

    override fun addVertex(vertex: Vertex) {
        if (!adjList.containsKey(vertex)) {
            adjList[vertex] = mutableListOf()
        }
    }

    override fun removeVertex(vertex: Vertex) {
        adjList.remove(vertex)
        adjList.values.forEach { neighbors ->
            neighbors.remove(vertex)
        }
    }

    override fun addEdge(from: Vertex, to: Vertex) {
        addVertex(from)
        addVertex(to)
        adjList.getOrPut(from) { mutableListOf() }.add(to)
        if (!isDirected) {
            adjList.getOrPut(to) { mutableListOf() }.add(from)
        }
    }

    override fun removeEdge(from: Vertex, to: Vertex) {
        adjList[from]?.remove(to)
        if (!isDirected) {
            adjList[to]?.remove(from)
        }
    }

    override fun getNeighbors(vertex: Vertex): List<Vertex> {
        return adjList[vertex]?.toList() ?: emptyList()
    }

    override fun containsVertex(vertex: Vertex): Boolean {
        return adjList.containsKey(vertex)
    }

    override fun containsEdge(from: Vertex, to: Vertex): Boolean {
        return adjList[from]?.contains(to) ?: false
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
                mas.add(Pair(Pair(v_f.key, v_s), null))
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

    inner class Iterate : Iterator<Pair<Vertex, MutableList<Vertex>>?> {
        var array: ArrayDeque<Pair<Vertex, MutableList<Vertex>>> = ArrayDeque()
        var f: Boolean = true

        override fun next(): Pair<Vertex, MutableList<Vertex>> {
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