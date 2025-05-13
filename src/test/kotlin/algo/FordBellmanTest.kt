package algo

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

        val result = FordBellman.fordBellman(graph, a, c)

        assertNotNull(result)
        assertEquals(listOf(a, d, e, c), result.first)

        assertEquals(7.0, result.second, 0.001)

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
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, 3.0)
        graph.addEdge(a, c, 12.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(listOf(a, b, c), path)
        assertEquals(8.0, distance)
    }

    @Test
    @DisplayName("Граф с отрицательными весами (без отрицательных циклов)")
    fun graphWithNegativeWeightsNoNegativeCycles() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 5.0)
        graph.addEdge(b, c, -4.0)
        graph.addEdge(a, c, 2.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(listOf(a, b, c), path)
        assertEquals(1.0, distance)
    }

    @Test
    @DisplayName("Обнаружение отрицательного цикла")
    fun detectNegativeCycle() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(b, c, -2.0)
        graph.addEdge(c, a, -1.0)

        assertThrows(java.lang.IllegalStateException::class.java) { // ?
            FordBellman.fordBellman(graph, a, c)
        }
    }

    @Test
    @DisplayName("Нет пути между вершинами")
    fun noPathBetweenVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 1.0)

        val (path, distance) = FordBellman.fordBellman(graph, a, c)
        assertEquals(listOf(c), path) // ??
        assertEquals(Double.POSITIVE_INFINITY, distance)
    }

    @Test
    @DisplayName("Вычисление суммы расстояний до всех вершин")
    fun calculateSumOfDistancesToAllVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b, 1.0)
        graph.addEdge(a, c, 2.0)
        graph.addEdge(b, c, 1.0)

        val (vertices, totalDistance) = FordBellman.fordBellman(graph, a)

        val expectedDistance = 0.0 + 1.0 + 2.0
        assertEquals(expectedDistance, totalDistance)
        assertEquals(graph.getVertices().size, vertices.size)
    }
}