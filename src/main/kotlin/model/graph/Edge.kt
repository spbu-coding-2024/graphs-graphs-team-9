package model.graph;

data class Edge(
//    var num: Int,
    val source: Vertex,
    val destination: Vertex,
    var weight: Double? = null
)
