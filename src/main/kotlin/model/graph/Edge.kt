package model.graph;

// Получается больше не нужен edge
import kotlin.Pair;

class Edge(
//    var num: Int,
    val source: Vertex,
    val destination: Vertex,
    var weight: Double? = null
)