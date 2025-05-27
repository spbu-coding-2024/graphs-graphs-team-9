package viewModel.screen.layouts

import model.graph.Graph
import model.graph.Vertex
import org.gephi.graph.api.Node
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2 as GephiForceAtlas2

class ForceAtlas2(private val graphModelForStrategy: Graph) : GephiAdapter(), RepresentationStrategy {

    override fun apply(graph: Graph): Map<Vertex, Pair<Float, Float>> {
        return applyWithParams(graph, 100, true, 35.0, 1.5)
    }

    fun applyWithParams(
            graph: Graph,
            iterations: Int = 100,
            barnesHutOptimize: Boolean = true,
            scalingRatio: Double = 35.0,
            gravity: Double = 1.5
    ): Map<Vertex, Pair<Float, Float>> {
        val pc: ProjectController = Lookup.getDefault().lookup(ProjectController::class.java)
        pc.newProject()

        // Поздняя инициализация graphModel и gephiGraph
        this.graphModel = Lookup.getDefault().lookup(org.gephi.graph.api.GraphController::class.java).graphModel
        this.gephiGraph = if (graph.isDirected()) this.graphModel.directedGraph else this.graphModel.undirectedGraph

        val algorithm = GephiForceAtlas2(null)
        val map: HashMap<Vertex, Node> = convertToGephi(graph)

        algorithm.setGraphModel(this.graphModel)
        algorithm.resetPropertiesValues()
        algorithm.isBarnesHutOptimize = barnesHutOptimize
        algorithm.scalingRatio = scalingRatio
        algorithm.gravity = gravity
        algorithm.isLinLogMode = true

        algorithm.initAlgo()
        repeat(iterations) {
            if (algorithm.canAlgo()) {
                algorithm.goAlgo()
            } else {
                return@repeat
            }
        }
        algorithm.endAlgo()

        return convertBackWithXY(map)
    }

    override fun layout(
            graph: Graph,
            canvasWidthHint: Double,
            canvasHeightHint: Double
    ): Map<Vertex, Pair<Float, Float>> {
        return apply(graph)
    }
}
