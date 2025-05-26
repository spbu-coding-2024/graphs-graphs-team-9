package viewModel.screen.layouts

import model.graph.Graph
import model.graph.Vertex
import org.gephi.graph.api.Node

class ForceAtlas2(graph: Graph) : GephiAdapter() {
    override fun apply(graph: Graph): Map<Vertex, Pair<Float, Float>> {
        val algorithm= org.gephi.layout.plugin.forceAtlas2.ForceAtlas2(null)
        val map: HashMap<Vertex, Node> = convertToGephi(graph)

        algorithm.setGraphModel(graphModel)
        algorithm.resetPropertiesValues()
        algorithm.isBarnesHutOptimize = true
        algorithm.initAlgo()
        repeat(100) {
            if (algorithm.canAlgo())
                algorithm.goAlgo()
        }
        algorithm.endAlgo()

        // возвращается хешмап: вершина - (x,y) во флоте
        return convertBackWithXY(map)
    }
}