package model.graph;

// Получается больше не нужен edge
import kotlin.Pair;

class Edge(
//    var num: Int,
    var connect: Pair<Vertex?, Vertex?>,
    var weight: Double?,
)