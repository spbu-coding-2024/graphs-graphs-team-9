package algo

import model.algorithms.HarmonicCentrality
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class HarmonicCentralityTest {
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        graph = GraphFactory.createUndirectedUnweightedGraph()
    }

    @Test
    @DisplayName("Гармоническая центральность для графа с одной вершиной")
    fun singleVertexGraph() {
        val a = Vertex(1, "A")
        graph.addVertex(a)

        val centrality = HarmonicCentrality(graph)
        assertEquals(0.0, centrality.centrality[a])
    }

    @Test
    @DisplayName("Гармоническая центральность для двух несвязанных вершин")
    fun twoDisconnectedVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graph.addVertex(a)
        graph.addVertex(b)

        val centrality = HarmonicCentrality(graph)
        assertEquals(0.0, centrality.centrality[a])
        assertEquals(0.0, centrality.centrality[b])
    }

    @Test
    @DisplayName("Гармоническая центральность для двух связанных вершин")
    fun twoConnectedVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addEdge(a, b)

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0, centrality.centrality[a])
        assertEquals(1.0, centrality.centrality[b])
    }

    @Test
    @DisplayName("Гармоническая центральность для линейного графа из 3 вершин")
    fun threeVerticesLineGraph() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addEdge(a, b)
        graph.addEdge(b, c)

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0 + 1.0/2.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0/2.0 + 1.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для полного графа из 3 вершин")
    fun completeGraphThreeVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addEdge(a, b)
        graph.addEdge(b, c)
        graph.addEdge(a, c)

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0 + 1.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для звездообразного графа")
    fun starGraph() {
        val center = Vertex(1, "Center")
        val a = Vertex(2, "A")
        val b = Vertex(3, "B")
        val c = Vertex(4, "C")
        graph.addVertex(center)
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addEdge(center, a)
        graph.addEdge(center, b)
        graph.addEdge(center, c)

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0 + 1.0 + 1.0, centrality.centrality[center]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0/2.0 + 1.0/2.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0/2.0 + 1.0/2.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0/2.0 + 1.0/2.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для взвешенного графа")
    fun weightedGraph() {
        graph = GraphFactory.createDirectedWeightedGraph()
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addEdge(a, b, 2.0)
        graph.addEdge(b, c, 3.0)
        graph.addEdge(a, c, 10.0)

        val centrality = HarmonicCentrality(graph)

        // Для вершины A:
        // - путь до B: 2 (A→B)
        // - путь до C: min(10 (A→C), 5 (A→B→C) → 5
        // гармоническая центральность: 1/2 + 1/5 = 0.5 + 0.2 = 0.7
        assertEquals(0.7, centrality.centrality[a]?: 0.0, 1e-6)

        // Для вершины B:
        // - путь до A: нет пути (в ориентированном графе)
        // - путь до C: 3 (B→C)
        // гармоническая центральность: 1/3 ≈ 0.333...
        assertEquals(1.0/3.0, centrality.centrality[b]?: 0.0, 1e-6)

        // Для вершины C:
        // - путь до A: нет пути
        // - путь до B: нет пути
        // гармоническая центральность: 0
        assertEquals(0.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для графа с несколькими компонентами связности")
    fun disconnectedComponents() {
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

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0, centrality.centrality[c]?: 0.0, 1e-6)
        assertEquals(1.0, centrality.centrality[d]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для пустого графа")
    fun emptyGraph() {
        val centrality = HarmonicCentrality(graph)
        assertTrue(centrality.centrality.isEmpty())
    }

    @Test
    @DisplayName("Гармоническая центральность для графа с петлей")
    fun graphWithSelfLoop() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graph.addVertex(a)
        graph.addVertex(b)
        graph.addEdge(a, b)
        graph.addEdge(a, a)

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0, centrality.centrality[b]?: 0.0, 1e-6)
    }
}