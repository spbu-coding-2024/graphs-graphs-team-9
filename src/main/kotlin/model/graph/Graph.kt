package model.graph;

public class Graph(var isDirected: Boolean = false) {
    val vertexes = hashMapOf<Int, Vertex>()
    val edges = hashMapOf<Pair<Vertex, Vertex>, Pair<Boolean?, Double>>()

    var edgeCount = 0

    fun addEdge(firstId: Int, secondId: Int, weight: Double) {
        if (vertexes[firstId] == null || vertexes[secondId] == null)
            return // Нужен вывод ошибок
        // Не уверен в "as Pair<Vertex, Vertex>"
        edges.getOrPut(Pair(vertexes[firstId], vertexes[secondId]) as Pair<Vertex, Vertex>) { Pair(true, weight) }
        edgeCount++
    }

    fun addVertex(id: Int, name: String) {
        vertexes.getOrPut(id) { Vertex(id, name) }
    }

    fun deleteEdge(first: Int, second: Int){
        val pair = Pair(vertexes[first], vertexes[second])
        edges.remove(pair)
    }

    fun deleteVertex(key: Int){
        val el = vertexes[key]
        var delEdge: MutableList<Pair<Vertex, Vertex>> = mutableListOf()
        edges.forEach { each -> if (each.key.first == el || each.key.second == el) delEdge.add(each.key)}
        vertexes.remove(key)
        delEdge.forEach {del -> edges.remove(del)}
    }

    fun getEdge(first: Int, second: Int): Pair<Boolean?, Double>? {
        return edges[Pair(vertexes[first], vertexes[second])]
    }
    
    fun printG() {
    edges.forEach {el ->
        println(el.key.first.name + " " + el.key.second.name)
    }
    }
}