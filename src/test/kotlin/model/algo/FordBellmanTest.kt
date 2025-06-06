package model.algo

import model.algorithms.FordBellman
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FordBellmanTest {
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        graph = GraphFactory.createDirectedWeightedGraph()
    }

    @Test
    @DisplayName("Нахождение кратчайшего пути в ориентированном взвешенном графе")
    fun fordBellmanInDirectedGraph() {
        val (a, _, c, d, e) = createTestGraph1()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertNotNull(path)
        assertNotNull(distance)
        assertEquals(listOf(a, d, e, c).map { it.name }, path!!.map { it.name })
        assertEquals(7.0, distance)
    }

    @Test
    @DisplayName("Граф с одним узлом")
    fun singleNodeGraph() {
        graph.addVertex("A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val (path, distance) = FordBellman.fordBellman(graph, a, a)
        assertEquals(listOf(a).map { it.name }, path!!.map { it.name })
        assertEquals(0.0, distance)
    }

    @Test
    @DisplayName("Простой граф с одним ребром")
    fun simpleGraphWithOneEdge() {
        graph.addVertex("A")
        graph.addVertex("B")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        graph.addEdge("A", "B", 10.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, b)
        assertEquals(listOf(a, b).map { it.name }, path!!.map { it.name })
        assertEquals(10.0, distance)
    }

    @Test
    @DisplayName("Граф с несколькими ребрами и положительными весами")
    fun graphWithPositiveWeights() {
        val (a, b, c) = createTestGraph2()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertEquals(listOf(a, b, c).map { it.name }, path!!.map { it.name })
        assertEquals(8.0, distance)
    }

    @Test
    @DisplayName("Граф с отрицательными весами (без отрицательных циклов)")
    fun graphWithNegativeWeightsNoNegativeCycles() {
        val (a, b, c) = createTestGraph3()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertEquals(listOf(a, b, c).map { it.name }, path!!.map { it.name })
        assertEquals(1.0, distance)
    }

    @Test
    @DisplayName("Обнаружение отрицательного цикла1")
    fun detectNegativeCycle() {
        val (a, _, c) = createTestGraph4()
        assertThrows(IllegalStateException::class.java) {
            FordBellman.fordBellman(graph, a, c)
        }
    }

    @Test
    @DisplayName("Обнаружение отрицательного цикла2")
    fun detectComplexNegativeCycle() {
        val (_, _, _, _, _) = createTestGraph1()
        graph.removeVertex("E")

        if (graph.getVertexByName("A") == null) graph.addVertex("A")
        if (graph.getVertexByName("B") == null) graph.addVertex("B")
        if (graph.getVertexByName("C") == null) graph.addVertex("C")
        if (graph.getVertexByName("D") == null) graph.addVertex("D")

        val vA = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")
        val vD = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 1.0)
        graph.addEdge("B", "C", -1.0)
        graph.addEdge("C", "D", -1.0)
        graph.addEdge("D", "B", -1.0)

        assertThrows(IllegalStateException::class.java) {
            FordBellman.fordBellman(graph, vA, vD)
        }
    }

    @Test
    @DisplayName("Нет пути между вершинами")
    fun noPathBetweenVertices() {
        val (a, _, c) = createTestGraph2()
        graph.removeEdge("B", "C")
        graph.removeEdge("A", "C")

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertNull(path)
        assertEquals(Double.POSITIVE_INFINITY, distance)
    }

    @Test
    @DisplayName("Граф с несколькими путями к вершине")
    fun multiplePathsToTarget() {
        val (a, b, _) = createTestGraph2()
        graph.addVertex("D")
        val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 2.0)
        graph.addEdge("A", "C", 5.0)
        graph.addEdge("B", "D", 3.0)
        graph.addEdge("C", "D", 1.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, d)
        assertEquals(listOf(a, b, d).map { it.name }, path!!.map { it.name })
        assertEquals(5.0, distance)
    }

    @Test
    @DisplayName("Граф с нулевыми весами")
    fun graphWithZeroWeights() {
        val (a, b, c) = createTestGraph2()

        graph.addEdge("A", "B", 0.0)
        graph.addEdge("B", "C", 0.0)
        graph.addEdge("A", "C", 5.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(listOf(a, b, c).map { it.name }, path!!.map { it.name })
        assertEquals(0.0, distance)
    }

    @Test
    @DisplayName("Граф с петлей")
    fun graphWithSelfLoop() {
        graph.addVertex("A")
        graph.addVertex("B")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "A", 1.0)
        graph.addEdge("A", "B", 2.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, b)
        assertEquals(listOf(a, b).map { it.name }, path!!.map { it.name })
        assertEquals(2.0, distance)
    }

    @Test
    @DisplayName("Большой граф с разными весами")
    fun largeGraphWithVariousWeights() {
        val vertexNames = (1..10).map { "V$it" }
        vertexNames.forEach { graph.addVertex(it) }
        val vertices =
            vertexNames.map {
                graph.getVertexByName(it) ?: throw IllegalStateException("Вершина должна быть найдена")
            }

        graph.addEdge("V1", "V2", 5.0)
        graph.addEdge("V1", "V3", 3.0)
        graph.addEdge("V2", "V4", 2.0)
        graph.addEdge("V3", "V2", 1.0)
        graph.addEdge("V3", "V4", 1.0)
        graph.addEdge("V4", "V5", 7.0)
        graph.addEdge("V5", "V6", -2.0)
        graph.addEdge("V6", "V7", 3.0)
        graph.addEdge("V7", "V8", 4.0)
        graph.addEdge("V8", "V9", -1.0)
        graph.addEdge("V9", "V10", 2.0)
        graph.addEdge("V4", "V10", 10.0)

        val (path, distance) = FordBellman.fordBellman(graph, vertices[0], vertices[9])
        val expectedPathVertices = listOf(vertices[0], vertices[2], vertices[3], vertices[9])
        assertEquals(expectedPathVertices.map { it.name }, path!!.map { it.name })
        assertEquals(14.0, distance)
    }

    private fun createTestGraph1(): List<Vertex> {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addVertex("D")
        graph.addVertex("E")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")
        val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")
        val e = graph.getVertexByName("E") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 6.0)
        graph.addEdge("A", "D", 1.0)
        graph.addEdge("B", "C", 5.0)
        graph.addEdge("B", "E", 2.0)
        graph.addEdge("D", "B", 2.0)
        graph.addEdge("D", "E", 1.0)
        graph.addEdge("E", "C", 5.0)

        return listOf(a, b, c, d, e)
    }

    private fun createTestGraph2(): List<Vertex> {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 5.0)
        graph.addEdge("B", "C", 3.0)
        graph.addEdge("A", "C", 12.0)

        return listOf(a, b, c)
    }

    private fun createTestGraph3(): List<Vertex> {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 5.0)
        graph.addEdge("B", "C", -4.0)
        graph.addEdge("A", "C", 2.0)

        return listOf(a, b, c)
    }

    private fun createTestGraph4(): List<Vertex> {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B", 1.0)
        graph.addEdge("B", "C", -2.0)
        graph.addEdge("C", "A", -1.0)

        return listOf(a, b, c)
    }
}
