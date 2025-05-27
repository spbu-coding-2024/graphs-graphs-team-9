package viewModel.screen.layouts

import model.graph.Graph
import model.graph.Vertex

interface RepresentationStrategy {
    /**
     * Рассчитывает "сырые" или относительные позиции для вершин графа.
     * @param graph Граф для компоновки.
     * @param canvasWidthHint Подсказка по ширине канвы (может использоваться алгоритмом).
     * @param canvasHeightHint Подсказка по высоте канвы (может использоваться алгоритмом).
     * @return Map, где ключ - это Vertex, а значение - Pair из X и Y Float координат.
     */
    fun layout(graph: Graph): Map<Vertex, Pair<Float, Float>>
}