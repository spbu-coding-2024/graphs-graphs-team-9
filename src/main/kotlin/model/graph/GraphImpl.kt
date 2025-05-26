package model.graph

class GraphImpl(
    private val isDirected: Boolean = false,
    private val isWeighted: Boolean = false
) : Graph {

    private val adjList: MutableMap<Vertex, MutableSet<Edge>> = mutableMapOf()

    override fun getMap(): Map<Vertex, List<Edge>> {
        val map = adjList.mapValues { it.value.toList() }
        return map
    }

    override fun addVertex(vertex: Vertex) {
        if (!adjList.containsKey(vertex)) {
            adjList[vertex] = mutableSetOf()
        }
    }

    override fun removeVertex(vertex: Vertex) {
        adjList.remove(vertex)
        adjList.values.forEach { edges ->
            edges.removeAll { it.destination == vertex }
        }
    }

    override fun addEdge(from: Vertex, to: Vertex, weight: Double?) {
        if (!containsVertex(from) || !containsVertex(to)) {
            return
        }

        val actualWeight = if (isWeighted) weight else null

        removeEdge(from, to)

        val edge = Edge(from, to, actualWeight)
        adjList.getOrPut(from) { mutableSetOf() }.add(edge)

        if (!isDirected) {
            val reverseEdge = Edge(to, from, actualWeight)
            adjList.getOrPut(to) { mutableSetOf() }.add(reverseEdge)
        }
    }

    override fun removeEdge(from: Vertex, to: Vertex) {
        adjList[from]?.removeAll { it.destination == to }
        if (!isDirected) {
            adjList[to]?.removeAll { it.destination == from }
        }
    }

    override fun getNeighbors(vertex: Vertex): List<Vertex> {
        return adjList[vertex]?.map { it.destination } ?: emptyList()
    }

    override fun containsVertex(vertex: Vertex): Boolean {
        return adjList.containsKey(vertex)
    }

    override fun containsEdge(from: Vertex, to: Vertex): Boolean {
        return adjList[from]?.any { it.destination == to } ?: false
    }

    override fun getEdgeWeight(from: Vertex, to: Vertex): Double? {
        if (!containsEdge(from, to)) {
            return null
        }

        val edge = adjList[from]?.find { it.destination == to }
        return edge?.weight
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

    override fun isWeighted(): Boolean {
        return isWeighted
    }

    override fun getVertices(): List<Vertex> {
        return adjList.keys.toList()
    }

    override fun getEdges(): List<Edge> {
        val edges = mutableListOf<Edge>()
        for ((vertex, adjacentEdges) in adjList) {
            edges.addAll(adjacentEdges)
        }

        return if (isDirected) edges else edges.distinctBy {
            setOf(it.source.id, it.destination.id)
        }
    }

    override fun getVertexByKey(id: Int): Vertex? {
        for (i in getVertices()) {
            if (i.id == id) {
                return i
            }
        }
        return null
    }

    override fun getEdgeByVertex(firstV: Vertex, secondV: Vertex): Edge? {
        val edges = adjList[getVertexByKey(firstV.id)] ?: emptySet()
        val secondVertex = getVertexByKey(secondV.id)
        for (el in edges) {
            if (el.destination == secondVertex){
                return el
            }
        }
        return null
    }
    // Пофиксить
    override fun getVertexByName(name: String): Vertex {
        getVertices().forEach { i -> if (i.name == name) return i }
        return Vertex(-1)
    }

    inner class Iterate : Iterator<Pair<Vertex, MutableSet<Edge>>> {
        private var deque: ArrayDeque<Pair<Vertex, MutableSet<Edge>>> = ArrayDeque()
        private var firstInitDeqState: Boolean = false

        override fun next(): Pair<Vertex, MutableSet<Edge>> {
            return deque.removeFirst()
        }

        override fun hasNext(): Boolean {
            if (!isDequeInited()) {
                initDeque()
                switchState()
            }
            return deque.isNotEmpty()
        }

        private fun initDeque() {
            for (el in adjList) {
                deque.add(el.toPair())
            }
        }

        private fun isDequeInited(): Boolean {
            return firstInitDeqState
        }

        private fun switchState() {
            firstInitDeqState = true
        }
    }

    override operator fun iterator(): Iterate {
        return this.Iterate()
    }
}
