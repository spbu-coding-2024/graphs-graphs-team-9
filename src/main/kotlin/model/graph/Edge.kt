package model.graph;

import kotlin.Pair;

class Edge(
    var num: Int,
    var weight: Double,
    var connect: Pair<Vertex?, Vertex?>
)