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

        val (a, b, c, d, e) = createTestGraph1()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertNotNull(path)
        assertNotNull(distance)
        assertEquals(listOf(a, d, e, c), path)
        assertEquals(7.0, distance)
    }

    @Test
    @DisplayName("Граф с одним узлом")
    fun singleNodeGraph() {
        val a = Vertex(1, "A")
        graph.addVertex(a)
        val (path, distance) = FordBellman.fordBellman(graph, a, a)
        assertEquals(listOf(a), path)
        assertEquals(0.0, distance)
    }

    @Test
    @DisplayName("Простой граф с одним ребром")
    fun simpleGraphWithOneEdge() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addEdge(a, b, 10.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, b)
        assertEquals(listOf(a, b), path)
        assertEquals(10.0, distance)
    }

    @Test
    @DisplayName("Граф с несколькими ребрами и положительными весами")
    fun graphWithPositiveWeights() {
        val (a, b, c) = createTestGraph2()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertEquals(listOf(a, b, c), path)
        assertEquals(8.0, distance)
    }

    @Test
    @DisplayName("Граф с отрицательными весами (без отрицательных циклов)")
    fun graphWithNegativeWeightsNoNegativeCycles() {
        val (a, b, c) = createTestGraph3()
        val (path, distance) = FordBellman.fordBellman(graph, a, c)

        assertEquals(listOf(a, b, c), path)
        assertEquals(1.0, distance)
    }

    @Test
    @DisplayName("Обнаружение отрицательного цикла1")
    fun detectNegativeCycle() {
        val (a, b, c) = createTestGraph4()
        assertThrows(IllegalStateException::class.java) {
            FordBellman.fordBellman(graph, a, c)
        }
    }

    @Test
    @DisplayName("Обнаружение отрицательного цикла2")
    fun detectComplexNegativeCycle() {
        val (a, b, c, d, e) = createTestGraph1()
        graph.removeVertex(e)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(b, c, -1.0)
        graph.addEdge(c, d, -1.0)
        graph.addEdge(d, b, -1.0)

        assertThrows(IllegalStateException::class.java) {
            FordBellman.fordBellman(graph, a, d)
        }
    }

    @Test
    @DisplayName("Нет пути между вершинами")
    fun noPathBetweenVertices() {
        val (a, b, c) = createTestGraph2()
        graph.removeEdge(b, c)
        graph.removeEdge(a, c)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(null, path) // ??
        assertEquals(Double.POSITIVE_INFINITY, distance)
    }

    @Test
    @DisplayName("Граф с несколькими путями к вершине")
    fun multiplePathsToTarget() {
        val (a, b, c) = createTestGraph2()
        val d = Vertex(4, "D")

        graph.addVertex(d)

        graph.addEdge(a, b, 2.0)
        graph.addEdge(a, c, 5.0)
        graph.addEdge(b, d, 3.0)
        graph.addEdge(c, d, 1.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, d)
        assertEquals(listOf(a, b, d), path)
        assertEquals(5.0, distance)
    }

    @Test
    @DisplayName("Граф с нулевыми весами")
    fun graphWithZeroWeights() {
        val (a, b, c) = createTestGraph2()

        graph.addEdge(a, b, 0.0)
        graph.addEdge(b, c, 0.0)
        graph.addEdge(a, c, 5.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(listOf(a, b, c), path)
        assertEquals(0.0, distance)
    }

    @Test
    @DisplayName("Граф с петлей")
    fun graphWithSelfLoop() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")

        graph.addVertex(a)
        graph.addVertex(b)

        graph.addEdge(a, a, 1.0)
        graph.addEdge(a, b, 2.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, b)
        assertEquals(listOf(a, b), path)
        assertEquals(2.0, distance)
    }

    @Test
    @DisplayName("Большой граф с разными весами")
    fun largeGraphWithVariousWeights() {
        val vertices = (1..10).map { Vertex(it, "V$it") }
        vertices.forEach { graph.addVertex(it) }

        graph.addEdge(vertices[0], vertices[1], 5.0)
        graph.addEdge(vertices[0], vertices[2], 3.0)
        graph.addEdge(vertices[1], vertices[3], 2.0)
        graph.addEdge(vertices[2], vertices[1], 1.0)
        graph.addEdge(vertices[2], vertices[3], 1.0)
        graph.addEdge(vertices[3], vertices[4], 7.0)
        graph.addEdge(vertices[4], vertices[5], -2.0)
        graph.addEdge(vertices[5], vertices[6], 3.0)
        graph.addEdge(vertices[6], vertices[7], 4.0)
        graph.addEdge(vertices[7], vertices[8], -1.0)
        graph.addEdge(vertices[8], vertices[9], 2.0)
        graph.addEdge(vertices[3], vertices[9], 10.0)

        val (path, distance) = FordBellman.fordBellman(graph, vertices[0], vertices[9])
        assertEquals(listOf(vertices[0], vertices[2], vertices[3], vertices[9]), path)
        assertEquals(14.0, distance)
    }

    private fun createTestGraph1(): List<Vertex> {
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

        return listOf(a, b, c, d, e)
    }

    private fun createTestGraph2(): List<Vertex> {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, 3.0)
        graph.addEdge(a, c, 12.0)

        return listOf(a, b, c)
    }

    private fun createTestGraph3(): List<Vertex> {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, -4.0)
        graph.addEdge(a, c, 2.0)

        return listOf(a, b, c)
    }

    private fun createTestGraph4(): List<Vertex> {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(b, c, -2.0)
        graph.addEdge(c, a, -1.0)

        return listOf(a, b, c)
    }
}