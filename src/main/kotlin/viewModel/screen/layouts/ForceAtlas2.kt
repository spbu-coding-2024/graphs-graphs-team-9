package viewModel.screen.layouts

import androidx.compose.ui.unit.dp
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.Node
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import viewModel.graph.GraphViewModel
import kotlin.math.abs
import kotlin.random.Random

class ForceAtlas2: RepresentationStrategy {
    override fun layout(height: Double, width: Double, graphViewModel: GraphViewModel) {

        val pc: ProjectController = Lookup.getDefault().lookup(ProjectController::class.java)
        pc.newProject()
        val workSpace = pc.currentWorkspace
        val graphModel = Lookup.getDefault().lookup(GraphController::class.java).getGraphModel(workSpace)
        val graph = graphModel.graph

        val vertices = graphViewModel.vertices
        val edges = graphViewModel.edges

        val mapping = mutableMapOf<String, Node>()

        for (vertex in vertices) {
            val id = vertex.Id.toString()
            val node: Node = graphModel.factory().newNode(id)
            node.label = id
            node.setX(abs(Random.nextFloat()*1000))
            node.setY(abs(Random.nextFloat()*1000))
            node.setSize(vertex.radius.value)
            graph.addNode(node)
            mapping[vertex.Id.toString()] = node
        }

        for (edge in edges) {
            graph.addEdge(graphModel.factory().newEdge(mapping[edge.u.Id.toString()], mapping[edge.v.Id.toString()]))
        }

        var algorithm = ForceAtlas2(null)
        algorithm.setGraphModel(graphModel)
        algorithm.initAlgo()
        algorithm.resetPropertiesValues()
        algorithm.isBarnesHutOptimize = true
        algorithm.scalingRatio = 35.0
        algorithm.gravity = 1.5
        algorithm.isLinLogMode = true

        for (i in 1..100) {
            if (algorithm.canAlgo()) {
                algorithm.goAlgo()
            } else {
                break
            }

            for (vertex in vertices) {
                val m = mapping[vertex.Id.toString()]
                val x = m?.x()?.dp ?: vertex.x
                val y = m?.y()?.dp ?: vertex.y
                vertex.x = x + (width / 2).dp
                vertex.y = y + (height / 2).dp
            }
        }
        algorithm.endAlgo()
    }
}