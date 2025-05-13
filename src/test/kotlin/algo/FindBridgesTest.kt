package algo

import model.algorithms.FindBridges
import model.graph.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertTrue

class FindBridgesTest {

    private lateinit var findBridges: FindBridges

    @BeforeEach
    fun setup() {
        val emptyGraph = GraphFactory.createUndirectedUnweightedGraph()
        findBridges = FindBridges(emptyGraph)
    }

    @Test
    @DisplayName("Граф без ребер")
    fun noEdgesGraph() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graph.addVertex(a)
        graph.addVertex(b)

        findBridges = FindBridges(graph)
        findBridges.findBridges()
        assertTrue(findBridges.bridges.isEmpty())
    }

    @Test
    @DisplayName("Простой граф с одним мостом")
    fun simpleGraphWithOneBridge() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b)
        graph.addEdge(b, c)

        findBridges = FindBridges(graph)
        findBridges.findBridges()

        assertEquals(2, findBridges.bridges.size)
        assertTrue(findBridges.bridges.contains(a to b) || findBridges.bridges.contains(b to a))
        assertTrue(findBridges.bridges.contains(b to c) || findBridges.bridges.contains(c to b))
    }

    @Test
    @DisplayName("Граф без мостов (цикл)")
    fun graphWithNoBridgesCycle() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)

        graph.addEdge(a, b)
        graph.addEdge(b, c)
        graph.addEdge(c, a)

        findBridges = FindBridges(graph)
        findBridges.findBridges()

        assertTrue(findBridges.bridges.isEmpty())
    }

    @Test
    @DisplayName("Граф с несколькими компонентами связности")
    fun graphWithMultipleConnectedComponents() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)

        graph.addEdge(a, b)
        graph.addEdge(c, d)

        findBridges = FindBridges(graph)
        findBridges.findBridges()

        assertEquals(2, findBridges.bridges.size)
        assertTrue(findBridges.bridges.contains(a to b) || findBridges.bridges.contains(b to a))
        assertTrue(findBridges.bridges.contains(c to d) || findBridges.bridges.contains(d to c))
    }

    @Test
    @DisplayName("Граф с несколькими мостами и компонентами связности")
    fun graphWithMultipleBridgesAndComponents() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")
        val e = Vertex(5, "E")
        val f = Vertex(6, "F")
        val g = Vertex(7, "G")


        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)
        graph.addVertex(e)
        graph.addVertex(f)
        graph.addVertex(g)


        graph.addEdge(a, b)
        graph.addEdge(b, c)
        graph.addEdge(c, a) // цикл A-B-C
        graph.addEdge(c, d) // мост C-D
        graph.addEdge(e, f) // мост E-F
        graph.addEdge(f, g) // мост F-G

        findBridges = FindBridges(graph)
        findBridges.findBridges()

        assertEquals(3, findBridges.bridges.size)
        assertTrue(findBridges.bridges.any { it == (c to d) || it == (d to c) })
        assertTrue(findBridges.bridges.any { it == (e to f) || it == (f to e) })
        assertTrue(findBridges.bridges.any { it == (f to g) || it == (g to f) })

        val cycleEdges = listOf(a to b, b to c, c to a)
        for (edge in cycleEdges) {
            assertTrue(!findBridges.bridges.any { it == edge || it == Pair(edge.second, edge.first) })
        }
    }

    @Test
    @DisplayName("Пустой граф")
    fun emptyGraphHandling() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        findBridges = FindBridges(graph)
        assertDoesNotThrow { findBridges.findBridges() }
        assertTrue(findBridges.bridges.isEmpty())
    }

    @Test
    @DisplayName("Граф с петлями")
    fun graphWithSelfLoops() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val a = Vertex(1, "A")
        graph.addVertex(a)
        graph.addEdge(a, a)

        findBridges = FindBridges(graph)
        findBridges.findBridges()

        assertTrue(findBridges.bridges.isEmpty())
    }

    @Test
    @DisplayName("Большой граф без мостов")
    fun largeGraphNoBridges() {
        val graph = GraphFactory.createUndirectedUnweightedGraph()
        val vertices = (1..1000).map { Vertex(it, "V$it") }
        vertices.forEach { graph.addVertex(it) }

        for (i in 0 until vertices.size - 1) {
            graph.addEdge(vertices[i], vertices[i + 1])
        }
        graph.addEdge(vertices.last(), vertices.first())

        findBridges = FindBridges(graph)
        findBridges.findBridges()
        assertTrue(findBridges.bridges.isEmpty())
    }
}