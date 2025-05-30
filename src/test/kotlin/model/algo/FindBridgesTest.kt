package model.algo

import model.algorithms.FindBridges
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertTrue

class FindBridgesTest {

    private lateinit var findBridges: FindBridges
    private lateinit var graphUU: Graph
    private lateinit var graphU: Graph

    @BeforeEach
    fun setup() {
        graphUU = GraphFactory.createUndirectedUnweightedGraph()
    }

    @Test
    @DisplayName("Граф без ребер")
    fun noEdgesGraph() {
        graphUU.addVertex("A")
        graphUU.addVertex("B")
        findBridges = FindBridges(graphUU)
        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Простой граф с одним мостом")
    fun simpleGraphWithOneBridge() {
        graphUU.addVertex("A")
        graphUU.addVertex("B")
        graphUU.addVertex("C")
        graphUU.addEdge("A", "B")
        graphUU.addEdge("B", "C")

        findBridges = FindBridges(graphUU)
        val bridges = findBridges.findBridges()
        val a = graphUU.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graphUU.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graphUU.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        assertEquals(2, bridges.size)
        assertTrue(bridges.contains(a to b) || bridges.contains(b to a))
        assertTrue(bridges.contains(b to c) || bridges.contains(c to b))
    }

    @Test
    @DisplayName("Граф без мостов (цикл)")
    fun graphUUWithNoBridgesCycle() {
        graphUU.addVertex("A")
        graphUU.addVertex("B")
        graphUU.addVertex("C")
        graphUU.addEdge("A", "B")
        graphUU.addEdge("B", "C")
        graphUU.addEdge("C", "A")

        findBridges = FindBridges(graphUU)
        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Граф с несколькими компонентами связности")
    fun graphUUWithMultipleConnectedComponents() {
        graphUU.addVertex("A")
        graphUU.addVertex("B")
        graphUU.addVertex("C")
        graphUU.addVertex("D")
        graphUU.addEdge("A", "B")
        graphUU.addEdge("C", "D")

        findBridges = FindBridges(graphUU)
        val bridges = findBridges.findBridges()
        val a = graphUU.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graphUU.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graphUU.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")
        val d = graphUU.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")


        assertEquals(2, bridges.size)
        assertTrue(bridges.contains(a to b) || bridges.contains(b to a))
        assertTrue(bridges.contains(c to d) || bridges.contains(d to c))
    }

    @Test
    @DisplayName("Граф с несколькими мостами и компонентами связности")
    fun graphUUWithMultipleBridgesAndComponents() {
        graphUU.addVertex("A")
        graphUU.addVertex("B")
        graphUU.addVertex("C")
        graphUU.addVertex("D")
        graphUU.addVertex("E")
        graphUU.addVertex("F")
        graphUU.addVertex("G")

        graphUU.addEdge("A", "B")
        graphUU.addEdge("B", "C")
        graphUU.addEdge("C", "A") // цикл A-B-C
        graphUU.addEdge("C", "D") // мост C-D
        graphUU.addEdge("E", "F") // мост E-F
        graphUU.addEdge("F", "G") // мост F-G

        findBridges = FindBridges(graphUU)
        val bridges = findBridges.findBridges()

        val a = graphUU.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена"); val b = graphUU.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graphUU.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена"); val d = graphUU.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")
        val e = graphUU.getVertexByName("E") ?: throw IllegalStateException("Вершина должна быть найдена"); val f = graphUU.getVertexByName("F") ?: throw IllegalStateException("Вершина должна быть найдена")
        val g = graphUU.getVertexByName("G") ?: throw IllegalStateException("Вершина должна быть найдена")

        assertEquals(3, bridges.size)
        // Сравниваем пары Vertex объектов
        assertTrue(bridges.any { (it.first == c && it.second == d) || (it.first == d && it.second == c) })
        assertTrue(bridges.any { (it.first == e && it.second == f) || (it.first == f && it.second == e) })
        assertTrue(bridges.any { (it.first == f && it.second == g) || (it.first == g && it.second == f) })


        val cycleEdges = listOf(a to b, b to c, c to a)
        for (edge in cycleEdges) {
            assertTrue(!bridges.any { (it.first == edge.first && it.second == edge.second) || (it.first == edge.second && it.second == edge.first) })
        }
    }

    @Test
    @DisplayName("Пустой граф")
    fun emptyGraphHandling() {
        findBridges = FindBridges(graphUU)
        assertDoesNotThrow { findBridges.findBridges() }
        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Граф с петлями")
    fun graphUUWithSelfLoops() {
        graphUU.addVertex("A")
        graphUU.addEdge("A", "A")

        findBridges = FindBridges(graphUU)
        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Большой граф без мостов")
    fun largeGraphNoBridges() {
        val vertexNames = (1..1000).map { "V$it" }
        vertexNames.forEach { graphUU.addVertex(it) }

        for (i in 0 until vertexNames.size - 1) {
            graphUU.addEdge(vertexNames[i], vertexNames[i + 1])
        }
        graphUU.addEdge(vertexNames.last(), vertexNames.first())

        findBridges = FindBridges(graphUU)
        assertTrue(findBridges.findBridges().isEmpty())
    }
}