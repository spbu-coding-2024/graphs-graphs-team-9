package model.algo

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
        graph.addVertex("A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(0.0, centrality.centrality[a])
    }

    @Test
    @DisplayName("Гармоническая центральность для двух несвязанных вершин")
    fun twoDisconnectedVertices() {
        graph.addVertex("A")
        graph.addVertex("B")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(0.0, centrality.centrality[a])
        assertEquals(0.0, centrality.centrality[b])
    }

    @Test
    @DisplayName("Гармоническая центральность для двух связанных вершин")
    fun twoConnectedVertices() {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addEdge("A", "B")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0, centrality.centrality[a])
        assertEquals(1.0, centrality.centrality[b])
    }

    @Test
    @DisplayName("Гармоническая центральность для линейного графа из 3 вершин")
    fun threeVerticesLineGraph() {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0 + 1.0/2.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0/2.0 + 1.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для полного графа из 3 вершин")
    fun completeGraphThreeVertices() {
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        graph.addEdge("A", "C")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0 + 1.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[b]?: 0.0, 1e-6)
        assertEquals(1.0 + 1.0, centrality.centrality[c]?: 0.0, 1e-6)
    }

    @Test
    @DisplayName("Гармоническая центральность для звездообразного графа")
    fun starGraph() {
        graph.addVertex("Center")
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addEdge("Center", "A")
        graph.addEdge("Center", "B")
        graph.addEdge("Center", "C")
        val center = graph.getVertexByName("Center") ?: throw IllegalStateException("Вершина должна быть найдена")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

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
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addEdge("A", "B", 2.0)
        graph.addEdge("B", "C", 3.0)
        graph.addEdge("A", "C", 10.0)
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")

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
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")
        graph.addVertex("D")
        graph.addEdge("A", "B")
        graph.addEdge("C", "D")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена")
        val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")

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
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addEdge("A", "B")
        graph.addEdge("A", "A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")

        val centrality = HarmonicCentrality(graph)
        assertEquals(1.0, centrality.centrality[a]?: 0.0, 1e-6)
        assertEquals(1.0, centrality.centrality[b]?: 0.0, 1e-6)
    }
}