package model.algo

import model.algorithms.TarjanAlgorithm
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.GraphImpl
import model.graph.Vertex
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
        graph.addVertex("A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
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
        graph.addVertex("A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        graph.addEdge("A", "A")

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
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addEdge("A", "B")
        graph.addEdge("B", "A")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена")
        val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")

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
        graph.addVertex("A"); graph.addVertex("B"); graph.addVertex("C")
        graph.addVertex("D"); graph.addVertex("E")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена"); val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена"); val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")
        val e = graph.getVertexByName("E") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        graph.addEdge("C", "A")

        graph.addEdge("E", "E")

        graph.addEdge("C", "D")
        graph.addEdge("C", "E")

        val components = tarjan.findStronglyConnectedComponents(graph)
        assertEquals(3, components.size, "Expected 3 strongly connected components")

        val componentSets = components.map { it.toSet() }
        assertTrue(componentSets.contains(setOf(a, b, c)), "Component {A, B, C} not found")
        assertTrue(componentSets.contains(setOf(d)), "Component {D} not found")
        assertTrue(componentSets.contains(setOf(e)), "Component {E} not found")
    }

    /**
     * Тест для сложного направленного графа
     */
    @Test
    @DisplayName("Сложный направленный граф")
    fun testComplexDirectedGraph() {
        val vertexNames = (0..7).map { it.toString() }
        vertexNames.forEach { graph.addVertex(it) }
        val vertices = vertexNames.map { graph.getVertexByName(it) ?: throw IllegalStateException("Вершина должна быть найдена") }

        graph.addEdge("0", "1")
        graph.addEdge("1", "2")
        graph.addEdge("2", "0")

        graph.addEdge("3", "2")
        graph.addEdge("3", "4")

        graph.addEdge("4", "5")
        graph.addEdge("5", "6")
        graph.addEdge("6", "4")

        graph.addEdge("7", "6")
        graph.addEdge("7", "3")

        val components = tarjan.findStronglyConnectedComponents(graph)
        assertEquals(4, components.size, "Expected 4 strongly connected components")

        val componentSets = components.map { it.toSet() }
        val componentNameSets = components.map { comp -> comp.map { it.name }.toSet() }

        assertTrue(componentNameSets.contains(setOf("0", "1", "2")), "Component {0,1,2} not found")
        assertTrue(componentNameSets.contains(setOf("4", "5", "6")), "Component {4,5,6} not found")
        assertTrue(componentNameSets.contains(setOf("3")), "Component {3} not found")
        assertTrue(componentNameSets.contains(setOf("7")), "Component {7} not found")
    }

    /**
     * Тест для линейного графа без циклов
     */
    @Test
    @DisplayName("Линейный граф без циклов")
    fun testLinearGraph() {
        graph.addVertex("A"); graph.addVertex("B"); graph.addVertex("C"); graph.addVertex("D")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена"); val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена"); val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        graph.addEdge("C", "D")

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
        graph.addVertex("A"); graph.addVertex("B"); graph.addVertex("C"); graph.addVertex("D")
        val a = graph.getVertexByName("A") ?: throw IllegalStateException("Вершина должна быть найдена"); val b = graph.getVertexByName("B") ?: throw IllegalStateException("Вершина должна быть найдена")
        val c = graph.getVertexByName("C") ?: throw IllegalStateException("Вершина должна быть найдена"); val d = graph.getVertexByName("D") ?: throw IllegalStateException("Вершина должна быть найдена")

        graph.addEdge("A", "B")
        graph.addEdge("B", "A")

        graph.addEdge("C", "D")
        graph.addEdge("D", "C")

        val components = tarjan.findStronglyConnectedComponents(graph)
        assertEquals(2, components.size)

        val componentSets = components.map { it.toSet() }
        assertTrue(componentSets.contains(setOf(a, b)))
        assertTrue(componentSets.contains(setOf(c, d)))
    }
}
