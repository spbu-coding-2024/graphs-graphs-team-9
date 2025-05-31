package viewModel.screen.layouts

import androidx.compose.ui.unit.dp
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.Node
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import viewModel.graph.GraphViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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
            node.setX(abs(Random.nextFloat()))
            node.setY(abs(Random.nextFloat()))
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
        algorithm.scalingRatio = 40.0
        algorithm.gravity = 1.0
        algorithm.isLinLogMode = true

        for (i in 1..100) {
            if (algorithm.canAlgo()) {
                algorithm.goAlgo()
            } else {
                break
            }
        }
        algorithm.endAlgo()

        // Дальше идет фулл иишный код, который пытается разместить все вершины графа в зоне видимости экрана
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (vertex in vertices) {
            val node = mapping[vertex.Id.toString()]
            if (node != null) {
                val x = node.x()
                val y = node.y()
                minX = min(minX, x)
                maxX = max(maxX, x)
                minY = min(minY, y)
                maxY = max(maxY, y)
            }
        }

        // Если границы не определены (например, один узел), используем небольшую область
        if (minX == Float.MAX_VALUE || maxX == Float.MIN_VALUE) {
            minX = -1f
            maxX = 1f
        }
        if (minY == Float.MAX_VALUE || maxY == Float.MIN_VALUE) {
            minY = -1f
            maxY = 1f
        }

        // Вычисляем размеры полученного графа
        val graphWidth = maxX - minX
        val graphHeight = maxY - minY

        // Защита от деления на ноль
        val safeGraphWidth = if (graphWidth > 0) graphWidth else 1f
        val safeGraphHeight = if (graphHeight > 0) graphHeight else 1f

        // Определяем коэффициент масштабирования с отступами
        val padding = 50.0 // отступ от краёв в пикселях
        val scaleX = (width - 2 * padding) / safeGraphWidth
        val scaleY = (height - 2 * padding) / safeGraphHeight
        val scale = min(scaleX, scaleY) // используем меньший коэффициент для сохранения пропорций

        // Применяем нормализацию и центрирование к координатам вершин
        for (vertex in vertices) {
            val node = mapping[vertex.Id.toString()]
            if (node != null) {
                // Нормализуем и масштабируем координаты
                val scaledX = (node.x() - minX) * scale
                val scaledY = (node.y() - minY) * scale

                // Вычисляем размеры масштабированного графа
                val scaledGraphWidth = graphWidth * scale
                val scaledGraphHeight = graphHeight * scale

                // Центрируем граф в заданной области
                val centeredX = scaledX + (width - scaledGraphWidth) / 2
                val centeredY = scaledY + (height - scaledGraphHeight) / 2

                vertex.x = centeredX.dp
                vertex.y = centeredY.dp
            } else {
                // Если узел не найден, оставляем исходные координаты
                vertex.x = vertex.x
                vertex.y = vertex.y
            }
        }
    }
}
