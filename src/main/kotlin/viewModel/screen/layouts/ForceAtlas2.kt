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

class ForceAtlas2 : RepresentationStrategy {
    override fun layout(
        height: Double,
        width: Double,
        graphViewModel: GraphViewModel,
    ) {
        val pc: ProjectController = Lookup.getDefault().lookup(ProjectController::class.java)
        pc.newProject()
        val workSpace = pc.currentWorkspace
        val graphModel = Lookup.getDefault().lookup(GraphController::class.java).getGraphModel(workSpace)
        val gephiGraph = graphModel.graph

        val gephiNodeToVmMap = mutableMapOf<String, viewModel.graph.VertexViewModel>()

        for (vertexVM in graphViewModel.verticesMap.values) {
            val id = vertexVM.Id.toString()
            val node: Node = graphModel.factory().newNode(id)
            node.label = id
            node.setX(abs(Random.nextFloat()) * width.toFloat() - (width / 2).toFloat())
            node.setY(abs(Random.nextFloat()) * height.toFloat() - (height / 2).toFloat())
            node.setSize(vertexVM.radius.value)
            gephiGraph.addNode(node)
            gephiNodeToVmMap[id] = vertexVM
        }

        for (edgeVM in graphViewModel.edgesMap.values) {
            val sourceGephiNode = gephiGraph.getNode(edgeVM.u.Id.toString())
            val destGephiNode = gephiGraph.getNode(edgeVM.v.Id.toString())
            if (sourceGephiNode != null && destGephiNode != null) {
                gephiGraph.addEdge(graphModel.factory().newEdge(sourceGephiNode, destGephiNode))
            }
        }

        val algorithm = ForceAtlas2(null)
        algorithm.setGraphModel(graphModel)
        algorithm.initAlgo()
        algorithm.resetPropertiesValues()
        algorithm.isBarnesHutOptimize = true
        algorithm.scalingRatio = 20.0
        algorithm.gravity = 1.5
        algorithm.isLinLogMode = true

        for (i in 1..100) {
            if (algorithm.canAlgo()) {
                algorithm.goAlgo()
            } else {
                break
            }
        }
        algorithm.endAlgo()

        for (node in gephiGraph.nodes) {
            val vm = gephiNodeToVmMap[node.id.toString()]
            vm?.let {
                it.x = node.x().dp + (width / 2).dp
                it.y = node.y().dp + (height / 2).dp
            }
        }
    }
}
