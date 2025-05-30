package viewModel.graph

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.algorithms.DijkstraAlgorithm
import model.algorithms.FindBridges
import model.algorithms.FordBellman
import model.algorithms.TarjanAlgorithm
import model.graph.Edge
import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import kotlin.random.Random


class GraphViewModel(
        var graph: Graph,
        private val _showVerticesLabels: State<Boolean>,
        private val _showEdgesLabels: State<Boolean>
) {
    val showVerticesLabels = _showVerticesLabels
    val showEdgesLabels = _showEdgesLabels
    
    private val _vertices = mutableStateOf(
            graph.getVertices().associateWith { v ->
                VertexViewModel(
                        0.dp, 0.dp, Color.Gray, v, showVerticesLabels
                )
            }
    )

    private val _edges = mutableStateOf(
            createEdgesViewModels(graph.getEdges(), showVerticesLabels, showEdgesLabels)
    )

    val vertices: Collection<VertexViewModel>
        get() = _vertices.value.values

    val edges: Collection<EdgeViewModel>
        get() = _edges.value.values

    /**
     * Обновляет все ViewModels (вершин и рёбер) для синхронизации с текущим состоянием
     * базового объекта [graph].
     *
     * Этот метод следует вызывать, если структура графа (вершины или рёбра) была изменена
     * методами, не вызывающими `updateGraph` (например, `graph.addVertex()` был вызван напрямую
     * на объекте `graph`, а не через методы `GraphViewModel` или `MainScreenViewModel`,
     * которые бы вызвали `updateGraph` или `refreshGraph`).
     *
     * Он гарантирует, что UI будет отображать актуальные данные графа.
     * Сохраняет существующие экземпляры [VertexViewModel] для вершин, которые остались в графе,
     * добавляет новые для появившихся вершин и удаляет ViewModels для исчезнувших вершин.
     * Рёбра полностью пересоздаются на основе текущего списка рёбер графа и обновленных вершин.
     */
    fun refreshGraph() {
        refreshVertices()
        refreshEdges()
    }

    /**
     * Синхронизирует коллекцию [VertexViewModel] ([_vertices]) с текущим списком вершин в [graph].
     *
     * Добавляет [VertexViewModel] для новых вершин, появившихся в [graph],
     * и удаляет [VertexViewModel] для вершин, которые были удалены из [graph].
     * Существующие [VertexViewModel] для не измененных вершин сохраняются.
     * Новые [VertexViewModel] инициализируются с текущей настройкой видимости меток.
     */
    fun refreshVertices() {
        val currentVertices = _vertices.value.toMutableMap()

        graph.getVertices().forEach { vertex ->
            if (!currentVertices.containsKey(vertex)) {
                currentVertices[vertex] = VertexViewModel(
                        0.dp, 0.dp, Color.Gray, vertex, showVerticesLabels
                )
            }
        }

        val graphVertices = graph.getVertices().toSet()
        currentVertices.keys.retainAll(graphVertices)

        _vertices.value = currentVertices
    }

    /**
     * Полностью пересоздает коллекцию [EdgeViewModel] ([_edges]) на основе
     * текущего списка рёбер в [graph] и обновленной коллекции [_vertices].
     *
     * Этот метод обычно вызывается после [refreshVertices], чтобы гарантировать,
     * что [EdgeViewModel] будут связаны с актуальными экземплярами [VertexViewModel].
     */
    fun refreshEdges() {
        val showVerticesLabels = _vertices.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)
        val showEdgesLabels = _edges.value.values.firstOrNull()?._labelVisible
                ?: mutableStateOf(false)

        _edges.value = createEdgesViewModels(graph.getEdges(), showVerticesLabels, showEdgesLabels)
    }

    private fun createEdgesViewModels(
            edges: List<Edge>,
            showVerticesLabels: State<Boolean>,
            showEdgesLabels: State<Boolean>
    ): Map<Edge, EdgeViewModel> {
        return edges.associateWith { e ->
            val fst = _vertices.value[e.source]
                    ?: throw IllegalStateException("VertexView for ${e.source} not found")
            val snd = _vertices.value[e.destination]
                    ?: throw IllegalStateException("VertexView for ${e.destination} not found")
            EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination), showVerticesLabels, showEdgesLabels, e.weight)
        }
    }

    /**
     * Запускает алгоритм Форда-Беллмана для поиска кратчайшего пути между двумя вершинами.
     * Найденный путь и его вершины подсвечиваются на графе.
     * Если вершины не найдены или путь не существует, никаких изменений в подсветке не происходит.
     *
     * @param startName Имя начальной вершины. Если null или пустая строка, или вершина не найдена, метод завершится.
     * @param endName Имя конечной вершины. Если null или пустая строка, или вершина не найдена, метод завершится.
     * @throws IllegalStateException если граф содержит отрицательный цикл (согласно реализации [FordBellman]).
     */
    fun startFordBellman(startName: String?, endName: String?) {
        val bellman = FordBellman.fordBellman(graph, graph.getVertexByName(startName ?: "") ?: return, graph.getVertexByName(endName ?: "") ?: return)
        val path = bellman.first ?: return

        for (i in 0..path.size - 1) {
            _vertices.value[path[i]]?.color = Color.Red
            _vertices.value[path[i]]?.color = Color.Cyan
            if (i + 1 != path.size) {
                _edges.value[graph.getEdgeByVertex(path[i], path[i + 1])]?.color = Color.Blue
            }
        }
    }

    /**
     * Запускает алгоритм поиска мостов в графе.
     * Найденные мосты (рёбра) подсвечиваются на графе цветом [Color.Cyan].
     */
    fun startFindBridges(){
        val bridges = FindBridges(graph).findBridges()
        bridges.forEach{ edge ->
            _edges.value[graph.getEdgeByVertex(edge.first, edge.second)]?.color = Color.Cyan
        }
    }

    /**
     * Запускает алгоритм Дейкстры для поиска кратчайшего пути между двумя вершинами.
     * Найденный путь и его вершины подсвечиваются на графе.
     * Если вершины не найдены или путь не существует, никаких изменений в подсветке не происходит.
     *
     * @param start Имя начальной вершины.
     * @param end Имя конечной вершины.
     * @throws IllegalArgumentException если граф содержит рёбра с отрицательным весом (согласно реализации [DijkstraAlgorithm]).
     */
    fun startDijkstra(start: String, end: String){
        val d = DijkstraAlgorithm().findShortestPath(graph, graph.getVertexByName(start) ?: return, graph.getVertexByName(end) ?: return)
        val path = d?.path ?: return

        for (i in 0..path.size - 1) {
            _vertices.value[path[i]]?.color = Color.Cyan
            if (i + 1 != path.size) {
                _edges.value[graph.getEdgeByVertex(path[i], path[i + 1])]?.color = Color.Blue
            }
        }
    }

    /**
     * Запускает алгоритм Тарьяна для поиска сильно связанных компонент (SCC) в графе.
     * Каждая найденная компонента подсвечивается на графе уникальным случайным цветом.
     */
    fun startTarjan(){
        val T = TarjanAlgorithm().findStronglyConnectedComponents(graph)
        T.forEach { s ->
            val color = Color(Random.nextInt() % 256, Random.nextInt() % 256, Random.nextInt() % 256)
            s.forEach { v ->
                _vertices.value[v]?.color = color
            }
        }
    }

    /**
     * Загружает граф из базы данных Neo4j, заменяя текущий граф.
     * Тип загружаемого графа (ориентированный/неориентированный, взвешенный/невзвешенный)
     * определяется текущими свойствами [graph.isDirected] и [graph.isWeighted].
     * После загрузки необходимо вызвать [refreshGraph] и пересчитать лэйаут.
     *
     * @param uri URI для подключения к базе Neo4j.
     * @param username Имя пользователя для подключения.
     * @param password Пароль для подключения.
     * @throws Exception если происходит ошибка при подключении или чтении из Neo4j.
     */
    fun startNeo4j(uri: String, username: String, password: String){
        graph = Neo4j(uri, username, password).readFromDB(graph.isDirected(), graph.isWeighted())
                // доработать
    }

    /**
     * Очищает текущий граф, заменяя его новым пустым экземпляром [GraphImpl].
     * Тип нового графа (ориентированный/невзвешенный и т.д.) наследуется от текущего.
     * После очистки вызывает [updateGraph] для обновления ViewModels.
     */
    fun clearGraph(){
        graph = GraphImpl(isWeighted = graph.isWeighted(), isDirected = graph.isDirected())
        updateGraph(graph)
        // доработать
    }

    private val _vertexSize = mutableStateOf(25f)
    val vertexSize: State<Float>
        get() = _vertexSize

    /**
     * Обновляет базовый размер для всех вершин в графе и соответствующим образом
     * изменяет радиус каждого [VertexViewModel].
     *
     * Это позволяет динамически изменять визуальный размер вершин в пользовательском интерфейсе.
     * После обновления радиусов [VertexViewModel], UI, наблюдающий за ними, должен перерисоваться.
     *
     * @param newSize Новый базовый размер вершин (в абстрактных единицах, например, float).
     *                Это значение будет преобразовано в [Dp] для установки радиуса.
     */
    fun updateVertexSize(newSize: Float) {
        _vertexSize.value = newSize
        _vertices.value.values.forEach { vertex ->
            vertex.radius = newSize.dp
        }
    }

    fun isDirected(): Boolean{
        return graph.isDirected()
    }

    fun isWeighted(): Boolean{
        return graph.isWeighted()
    }

    /**
     * Обновляет ViewModel для отображения нового графа `newGraph`.
     *
     * Заменяет текущий внутренний граф и полностью пересоздает ViewModels для его вершин и рёбер.
     * Используется при загрузке или создании нового графа.
     *
     * **Примечание:** После вызова этого метода обычно требуется обновить расположение элементов графа
     * с помощью соответствующей стратегии размещения.
     *
     * @param newGraph Новый граф для отображения.
     */
    fun updateGraph(newGraph: Graph) {
        graph = newGraph
        _vertices.value = updateVertices(showVerticesLabels)
        _edges.value = updateEdges(showVerticesLabels, showEdgesLabels)
    }

    private fun updateVertices(showVerticesLabels: State<Boolean>): Map<Vertex, VertexViewModel> {
        return graph.getVertices().associateWith { v ->
            VertexViewModel(0.dp, 0.dp, Color.Gray, v, showVerticesLabels)
        }
    }

    private fun updateEdges(
            showVerticesLabels: State<Boolean>,
            showEdgesLabels: State<Boolean>
    ): Map<Edge, EdgeViewModel> {
        return graph.getEdges().associateWith { e ->
            val fst = _vertices.value[e.source]
                    ?: throw IllegalStateException("VertexView for ${e.source} not found")
            val snd = _vertices.value[e.destination]
                    ?: throw IllegalStateException("VertexView for ${e.destination} not found")
            EdgeViewModel(fst, snd, Color.Gray, Edge(e.source, e.destination),
                    showVerticesLabels, showEdgesLabels, e.weight)
        }
    }
}
