package model.graph

class GraphImpl(
    private val isDirected: Boolean = false,
    private val isWeighted: Boolean = false,
) : Graph {
    private var id = 1
    var positive = 0
    private val adjList: MutableMap<Vertex, MutableSet<Edge>> = mutableMapOf()

    override fun getPositive(): Boolean {
        return positive >= 0
    }

    override fun getMap(): Map<Vertex, List<Edge>> {
        val map = adjList.mapValues { it.value.toList() }
        return map
    }

    override fun addVertex(vertex: String) {
        if (getVertexByName(vertex) == null) {
            adjList[Vertex(id++, vertex)] = mutableSetOf()
        }
    }

    override fun removeVertex(vertex: String) {
        val vertex = getVertexByName(vertex)
        adjList.remove(vertex)
        adjList.values.forEach { edges ->
            edges.removeAll { it.destination == vertex }
        }
    }

    override fun addEdge(
        fromName: String,
        toName: String,
        weight: Double?,
    ) {
        if ((weight ?: 0.0) < 0.0) {
            positive--
        }
        val from = getVertexByName(fromName) ?: return
        val to = getVertexByName(toName) ?: return

        val actualWeight = if (isWeighted) weight else null

        removeEdge(fromName, toName)

        val edge = Edge(from, to, actualWeight)
        adjList.getOrPut(from) { mutableSetOf() }.add(edge)

        if (!isDirected) {
            val reverseEdge = Edge(to, from, actualWeight)
            adjList.getOrPut(to) { mutableSetOf() }.add(reverseEdge)
        }
    }

    override fun removeEdge(
        fromName: String,
        toName: String,
    ) {
        val from = getVertexByName(fromName) ?: return
        val to = getVertexByName(toName) ?: return
        if ((getEdgeByVertex(from, to)?.weight ?: 0.0) < 0.0) {
            positive--
        }
        adjList[from]?.removeAll { it.destination == to }
        if (!isDirected) {
            adjList[to]?.removeAll { it.destination == from }
        }
    }

    override fun getNeighbors(vertex: Vertex): List<Vertex> {
        return adjList[vertex]?.map { it.destination } ?: emptyList()
    }

    override fun containsVertex(vertex: String): Boolean {
        return getVertexByName(vertex) != null
    }

    override fun containsEdge(
        from: Vertex,
        to: Vertex,
    ): Boolean {
        return adjList[from]?.any { it.destination == to } ?: false
    }

    override fun getEdgeWeight(
        from: Vertex,
        to: Vertex,
    ): Double? {
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
        for ((_, adjacentEdges) in adjList) {
            edges.addAll(adjacentEdges)
        }

        return if (isDirected) {
            edges
        } else {
            edges.distinctBy {
                setOf(it.source.id, it.destination.id)
            }
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

    override fun getEdgeByVertex(
        firstV: Vertex,
        secondV: Vertex,
    ): Edge? {
        val edges = adjList[getVertexByKey(firstV.id)] ?: emptySet()
        val secondVertex = getVertexByKey(secondV.id)
        for (el in edges) {
            if (el.destination == secondVertex) {
                return el
            }
        }
        return null
    }

    override fun getVertexByName(name: String): Vertex? {
        getVertices().forEach { i -> if (i.name == name) return i }
        return null
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
