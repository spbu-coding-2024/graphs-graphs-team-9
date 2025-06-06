package model.graph

object GraphFactory {
    fun createDirectedWeightedGraph(): Graph {
        return GraphImpl(isDirected = true, isWeighted = true)
    }

    fun createUndirectedWeightedGraph(): Graph {
        return GraphImpl(isDirected = false, isWeighted = true)
    }

    fun createDirectedUnweightedGraph(): Graph {
        return GraphImpl(isDirected = true, isWeighted = false)
    }

    fun createUndirectedUnweightedGraph(): Graph {
        return GraphImpl(isDirected = false, isWeighted = false)
    }
}
