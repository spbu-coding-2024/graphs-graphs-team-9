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
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graphUU.addVertex(a)
        graphUU.addVertex(b)

        findBridges = FindBridges(graphUU)
        findBridges.findBridges()
        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Простой граф с одним мостом")
    fun simpleGraphWithOneBridge() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graphUU.addVertex(a)
        graphUU.addVertex(b)
        graphUU.addVertex(c)

        graphUU.addEdge(a, b)
        graphUU.addEdge(b, c)

        findBridges = FindBridges(graphUU)

        assertEquals(2, findBridges.findBridges().size )
        assertTrue(findBridges.findBridges().contains(a to b) || findBridges.findBridges().contains(b to a))
        assertTrue(findBridges.findBridges().contains(b to c) || findBridges.findBridges().contains(c to b))
    }

    @Test
    @DisplayName("Граф без мостов (цикл)")
    fun graphUUWithNoBridgesCycle() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graphUU.addVertex(a)
        graphUU.addVertex(b)
        graphUU.addVertex(c)

        graphUU.addEdge(a, b)
        graphUU.addEdge(b, c)
        graphUU.addEdge(c, a)

        findBridges = FindBridges(graphUU)
        findBridges.findBridges()

        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Граф с несколькими компонентами связности")
    fun graphUUWithMultipleConnectedComponents() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graphUU.addVertex(a)
        graphUU.addVertex(b)
        graphUU.addVertex(c)
        graphUU.addVertex(d)

        graphUU.addEdge(a, b)
        graphUU.addEdge(c, d)

        findBridges = FindBridges(graphUU)

        assertEquals(2, findBridges.findBridges().size)
        assertTrue(findBridges.findBridges().contains(a to b) || findBridges.findBridges().contains(b to a))
        assertTrue(findBridges.findBridges().contains(c to d) || findBridges.findBridges().contains(d to c))
    }

    @Test
    @DisplayName("Граф с несколькими мостами и компонентами связности")
    fun graphUUWithMultipleBridgesAndComponents() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")
        val e = Vertex(5, "E")
        val f = Vertex(6, "F")
        val g = Vertex(7, "G")


        graphUU.addVertex(a)
        graphUU.addVertex(b)
        graphUU.addVertex(c)
        graphUU.addVertex(d)
        graphUU.addVertex(e)
        graphUU.addVertex(f)
        graphUU.addVertex(g)


        graphUU.addEdge(a, b)
        graphUU.addEdge(b, c)
        graphUU.addEdge(c, a) // цикл A-B-C
        graphUU.addEdge(c, d) // мост C-D
        graphUU.addEdge(e, f) // мост E-F
        graphUU.addEdge(f, g) // мост F-G

        findBridges = FindBridges(graphUU)

        assertEquals(3, findBridges.findBridges().size)
        assertTrue(findBridges.findBridges().any { it == (c to d) || it == (d to c) })
        assertTrue(findBridges.findBridges().any { it == (e to f) || it == (f to e) })
        assertTrue(findBridges.findBridges().any { it == (f to g) || it == (g to f) })

        val cycleEdges = listOf(a to b, b to c, c to a)
        for (edge in cycleEdges) {
            assertTrue(!findBridges.findBridges().any { it == edge || it == Pair(edge.second, edge.first) })
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
        val a = Vertex(1, "A")
        graphUU.addVertex(a)
        graphUU.addEdge(a, a)

        findBridges = FindBridges(graphUU)
        findBridges.findBridges()

        assertTrue(findBridges.findBridges().isEmpty())
    }

    @Test
    @DisplayName("Большой граф без мостов")
    fun largeGraphNoBridges() {
        val vertices = (1..1000).map { Vertex(it, "V$it") }
        vertices.forEach { graphUU.addVertex(it) }

        for (i in 0 until vertices.size - 1) {
            graphUU.addEdge(vertices[i], vertices[i + 1])
        }
        graphUU.addEdge(vertices.last(), vertices.first())

        findBridges = FindBridges(graphUU)
        findBridges.findBridges()
        assertTrue(findBridges.findBridges().isEmpty())
    }
}