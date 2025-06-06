package model.algorithms

import model.graph.Graph
import model.graph.Vertex
import kotlin.math.min

class FindBridges(graph: Graph) {
    private var timer: Int = 0
    private val tin = graph.getVertices().associateWith { 0 }.toMutableMap()
    private val fup = graph.getVertices().associateWith { 0 }.toMutableMap()
    private val used = graph.getVertices().associateWith { false }.toMutableMap()
    private val g = graph.getMap()
    private val bridges: MutableList<Pair<Vertex, Vertex>> = mutableListOf()

    private fun dsp(
        v: Vertex,
        p: Vertex = Vertex(-1, ""),
    ) {
        used[v] = true
        tin[v] = timer
        fup[v] = timer
        timer++
        for (i in 0..<(g[v]?.size ?: 0)) {
            val to = g[v]?.get(i)?.destination ?: continue
            if (to == v) continue
            if (to == p) continue
            if (used[to] == true) {
                fup[v] = min(fup[v] ?: 0, tin[to] ?: 0)
            } else {
                dsp(to, v)
                fup[v] = min(fup[v] ?: 0, fup[to] ?: 0)
                if ((fup[to] ?: 0) > (tin[v] ?: 0)) {
                    bridges.add(v to to)
                }
            }
        }
    }

    fun findBridges(): MutableList<Pair<Vertex, Vertex>> {
        timer = 0
        used.forEach { (key, _) -> used[key] = false }
        for (el in g) {
            if (used[el.key] == false) {
                dsp(el.key)
            }
        }
        return bridges
    }
}
