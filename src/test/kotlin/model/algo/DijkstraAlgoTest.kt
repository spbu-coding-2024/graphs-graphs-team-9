package model.graph.algo

import model.graph.GraphImpl
import model.graph.Vertex
import model.algorithms.DijkstraAlgorithm

import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class DijkstraAlgorithmTest {

    private lateinit var dijkstra: DijkstraAlgorithm

    @BeforeEach
    fun setUp() {
        dijkstra = DijkstraAlgorithm()
    }

    /**
     * Тест для нахождения кратчайшего пути в простом взвешенном направленном графе
     */
    @Test
    @DisplayName("Нахождение кратчайшего пути в ориентированном взвешенном графе")
    fun DijkstraInDirectedGraph() {

        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")
        val e = Vertex(5, "E")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)
        graph.addVertex(e)

        graph.addEdge(a, b, 6.0)
        graph.addEdge(a, d, 1.0)
        graph.addEdge(b, c, 5.0)
        graph.addEdge(b, e, 2.0)
        graph.addEdge(d, b, 2.0)
        graph.addEdge(d, e, 1.0)
        graph.addEdge(e, c, 5.0)

        val result = dijkstra.findShortestPath(graph, a, c)

        assertNotNull(result)
        result?.let {
            assertEquals(listOf(a, d, e, c), it.path)

            assertEquals(7.0, it.distance, 0.001)
        }
    }

    /**
     * Тест для нахождения кратчайшего пути в простом взвешенном неориентированном графе
     */
    @Test
    @DisplayName("Нахождение кратчайшего пути в неориентированном взвешенном графе")
    fun DijkstraInNotDirGraph() {
        val graph = GraphImpl(isDirected = false, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(b, c, 2.0)
        graph.addEdge(c, d, 1.0)
        graph.addEdge(a, d, 6.0)

        val result = dijkstra.findShortestPath(graph, a, d)

        assertNotNull(result)
        result?.let {
            assertEquals(4.0, it.distance, 0.001)

            assertEquals(4, it.path.size)

            assertEquals(a, it.path.first())
            assertEquals(d, it.path.last())
        }
    }

    /**
     * Тест для случая, когда начальная и конечная вершины совпадают
     */
    @Test
    fun `find path when start and end are the same`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addEdge(a, b, 5.0)

        val result = dijkstra.findShortestPath(graph, a, a)

        assertNotNull(result)
        result?.let {
            assertEquals(1, it.path.size)
            assertEquals(a, it.path.first())

            assertEquals(0.0, it.distance, 0.001)
        }
    }

    /**
     * Тест для случая, когда путь между вершинами не существует
     */
    @Test
    @DisplayName("Путь между двумя вершинами не существует")
    fun `no path exists between vertices`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)

        graph.addEdge(a, b, 1.0)

        val result = dijkstra.findShortestPath(graph, a, d)

        assertNull(result)
    }

    /**
     * Тест для случая с отрицательными весами рёбер
     */
    @Test
    @DisplayName("Дейкстра не работает с отрицательными весами")
    fun `negative edge weights throw exception`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, -2.0)

        assertFailsWith<IllegalArgumentException> {
            dijkstra.findShortestPath(graph, a, c)
        }
    }

    /**
     * Тест басик
     */
    @Test
    @DisplayName("Тест с пары по дискретке")
    fun `Mokaev's Test`() {
        val graph = GraphImpl(isDirected = false, isWeighted = true)

        val A = Vertex(1, "A")
        val B = Vertex(2, "B")
        val C = Vertex(3, "C")
        val D = Vertex(4, "D")
        val E = Vertex(5, "E")

        graph.addVertex(A)
        graph.addVertex(B)
        graph.addVertex(C)
        graph.addVertex(D)
        graph.addVertex(E)

        graph.addEdge(A, B, 7.0)
        graph.addEdge(A, D, 4.0)
        graph.addEdge(D, B, 6.0)
        graph.addEdge(D, E, 3.0)
        graph.addEdge(B, E, 2.0)
        graph.addEdge(B, C, 4.0)
        graph.addEdge(E, C, 5.0)

        val result = dijkstra.findShortestPath(graph, A, C)

        assertEquals(11.0, result?.distance)
        assertEquals(listOf(A,B,C), result?.path)
    }

    /**
     * Тест для графа, состоящего из одной вершины
     */
    @Test
    @DisplayName("Граф с одной вершиной")
    fun `single vertex graph`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)
        val a = Vertex(1, "A")
        graph.addVertex(a)

        val result = dijkstra.findShortestPath(graph, a, a)
        assertNotNull(result)
        assertEquals(listOf(a), result?.path)
        assertEquals(0.0, result?.distance)
    }

    /**
     * Тест с несколькими путями одинаковой длины
     */
    @Test
    @DisplayName("Несколько путей с одинаковой длиной")
    fun `multiple paths with equal weights`() {
        val graph = GraphImpl(isDirected = false, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 2.0)
        graph.addEdge(b, c, 2.0)
        graph.addEdge(a, c, 4.0)

        val result = dijkstra.findShortestPath(graph, a, c)
        assertNotNull(result)

        assertEquals(4.0, result?.distance)
    }

    /**
     * Тест на обновление расстояний в очереди с приоритетом
     */
    @Test
    @DisplayName("Обновление расстояний в очереди")
    fun `priority queue distance update`() {
        val graph = GraphImpl(isDirected = false, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)


        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, 5.0)
        graph.addEdge(a, c, 15.0)
        graph.addEdge(c, a, 9.0)

        val result = dijkstra.findShortestPath(graph, a, c)
        assertNotNull(result)
        assertEquals(9.0, result?.distance)
    }

    /**
     * Тест с нулевыми весами рёбер
     */
    @Test
    @DisplayName("Обработка нулевых весов")
    fun `zero weight edges`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 0.0)
        graph.addEdge(b, c, 0.0)

        val result = dijkstra.findShortestPath(graph, a, c)
        assertNotNull(result)
        assertEquals(0.0, result?.distance)
        assertEquals(listOf(a, b, c), result?.path)
    }

    /**
     * Тест с циклом (без отрицательных весов)
     */
    @Test
    @DisplayName("Граф с циклом")
    fun `graph with cycle`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(b, c, 1.0)
        graph.addEdge(c, a, 1.0)
        graph.addEdge(c, b, 1.0)

        val result = dijkstra.findShortestPath(graph, a, c)

        assertEquals(2.0, result?.distance)
        assertEquals(listOf(a, b, c), result?.path)
    }
}


