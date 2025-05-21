package model.algo

import model.graph.GraphImpl
import model.graph.Vertex
import model.algorithms.TarjanAlgorithm
import model.graph.Graph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TarjanAlgorithmTest {

    private lateinit var tarjan: TarjanAlgorithm
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        tarjan = TarjanAlgorithm()
        graph = GraphImpl(isDirected = true, isWeighted = false)
    }

    /**
     * Тест для пустого графа
     */
    @Test
    @DisplayName("Пустой граф")
    fun testEmptyGraph() {
        val components = tarjan.findStronglyConnectedComponents(graph)

        assertTrue(components.isEmpty())
    }

    /**
     * Тест для графа с одной вершиной
     */
    @Test
    @DisplayName("Граф с одной вершиной")
    fun testSingleVertex() {
        val a = Vertex(1, "A")
        graph.addVertex(a)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(1, components.size)
        assertEquals(setOf(a), components[0])
    }

    /**
     * Тест для графа с петлей
     */
    @Test
    @DisplayName("Граф с петлей")
    fun testSelfLoop() {
        val a = Vertex(1, "A")
        graph.addVertex(a)
        graph.addEdge(a, a)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(1, components.size)
        assertEquals(setOf(a), components[0])
    }

    /**
     * Тест для графа с двумя взаимосвязанными вершинами
     */
    @Test
    @DisplayName("Граф с двумя взаимосвязанными вершинами")
    fun testTwoConnectedVertices() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addEdge(a, b)
        graph.addEdge(b, a)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(1, components.size)
        assertEquals(setOf(a, b), components[0])
    }

    /**
     * Тест для графа с несколькими компонентами сильной связности
     */
    @Test
    @DisplayName("Граф с несколькими компонентами сильной связности")
    fun testMultipleComponents() {
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

        graph.addEdge(a, b)
        graph.addEdge(b, c)
        graph.addEdge(c, a)

        graph.addEdge(e, e)

        graph.addEdge(c, d)
        graph.addEdge(c, e)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(3, components.size)

        val componentSets = components.map { it.toSet() }
        assertTrue(componentSets.contains(setOf(a, b, c)))
        assertTrue(componentSets.contains(setOf(d)))
        assertTrue(componentSets.contains(setOf(e)))
    }

    /**
     * Тест для сложного направленного графа
     */
    @Test
    @DisplayName("Сложный направленный граф")
    fun testComplexDirectedGraph() {

        val vertices = (0..7).map { Vertex(it, it.toString()) }
        vertices.forEach { graph.addVertex(it) }

        // Добавляем рёбра
        graph.addEdge(vertices[0], vertices[1])
        graph.addEdge(vertices[1], vertices[2])
        graph.addEdge(vertices[2], vertices[0])

        graph.addEdge(vertices[3], vertices[2])
        graph.addEdge(vertices[3], vertices[4])

        graph.addEdge(vertices[4], vertices[5])
        graph.addEdge(vertices[5], vertices[6])
        graph.addEdge(vertices[6], vertices[4])

        graph.addEdge(vertices[7], vertices[6])
        graph.addEdge(vertices[7], vertices[3])

        val components = tarjan.findStronglyConnectedComponents(graph)
        print(components)

        assertEquals(4, components.size)

        val componentSets = components.map { it.toSet() }
        assertTrue(componentSets.contains(setOf(vertices[0], vertices[1], vertices[2])))
        assertTrue(componentSets.contains(setOf(vertices[4], vertices[5], vertices[6])))
        assertTrue(componentSets.contains(setOf(vertices[3])))

        assertTrue(componentSets.contains(setOf(vertices[7])))
    }

    /**
     * Тест для линейного графа без циклов
     */
    @Test
    @DisplayName("Линейный граф без циклов")
    fun testLinearGraph() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)

        graph.addEdge(a, b)
        graph.addEdge(b, c)
        graph.addEdge(c, d)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(4, components.size)

        components.forEach { component ->
            assertEquals(1, component.size)
        }

        val verticesInComponents = components.flatten().toSet()
        assertEquals(setOf(a, b, c, d), verticesInComponents)
    }

    /**
     * Тест для графа с изолированными компонентами
     */
    @Test
    @DisplayName("Граф с изолированными компонентами")
    fun testDisconnectedComponents() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        val c = Vertex(3, "C")
        val d = Vertex(4, "D")

        graph.addVertex(a)
        graph.addVertex(b)
        graph.addVertex(c)
        graph.addVertex(d)

        graph.addEdge(a, b)
        graph.addEdge(b, a)

        graph.addEdge(c, d)
        graph.addEdge(d, c)

        val components = tarjan.findStronglyConnectedComponents(graph)

        assertEquals(2, components.size)

        val componentSets = components.map { it.toSet() }
        assertTrue(componentSets.contains(setOf(a, b)))
        assertTrue(componentSets.contains(setOf(c, d)))
    }
}
