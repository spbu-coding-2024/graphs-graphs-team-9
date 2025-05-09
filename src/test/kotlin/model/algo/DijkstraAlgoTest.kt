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

        // Добавляем вершины
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
}
