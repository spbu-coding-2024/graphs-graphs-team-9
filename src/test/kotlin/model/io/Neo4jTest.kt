package model.io

import model.algorithms.FindBridges
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.GraphImpl
import model.graph.Vertex
import model.io.Neo4j.Neo4j
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.neo4j.driver.exceptions.ClientException
import kotlin.test.*

@Tag("Wrong")
class Neo4jTest {

    private lateinit var graphDW: Graph
    private lateinit var graphDU: Graph
    private lateinit var graphUW: Graph
    private lateinit var graphUU: Graph
    private lateinit var neo4j: Neo4j

    @BeforeEach
    fun setup() {
        graphDW = GraphFactory.createDirectedWeightedGraph()
        graphDU = GraphFactory.createDirectedUnweightedGraph()
        graphUW = GraphFactory.createUndirectedWeightedGraph()
        graphUU = GraphFactory.createUndirectedUnweightedGraph()
        neo4j = Neo4j("bolt://localhost:7687", "TestBD", "81726354")
        neo4j.clearDatabase()
    }

    @Test
    @DisplayName("Пустой граф")
    fun emptyGraph() {
        neo4j.clearDatabase()
        neo4j.writeDB(graphDU)
        assertEquals(mapOf(), neo4j.readFromDB(true, false).getMap())
        neo4j.writeDB(graphDW)
        assertTrue(neo4j.readFromDB(true, false).getMap().isEmpty())
    }

    @Test
    @DisplayName("Проверка корректной отправки")
    fun correctSending() {
        createGraphDU()
        neo4j.writeDB(graphDU)

        val g = neo4j.readFromDB(graphDU.isDirected(), graphDU.isWeighted())
        assertEquals(graphDU.getVertices().sortedBy { it.id }, g.getVertices().sortedBy { it.id })
    }

    @Test
    @DisplayName("Запись и чтение направленного невзвешенного графа")
    fun directedUnweightedGraph() {
        createGraphDU()
        neo4j.writeDB(graphDU)

        val readGraph = neo4j.readFromDB(true, false)
        assertEquals(graphDU.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
        assertEquals(graphDU.getEdges().size, readGraph.getEdges().size)
    }

    @Test
    @DisplayName("Запись и чтение направленного взвешенного графа")
    fun directedWeightedGraph() {
        createGraphDW()
        neo4j.writeDB(graphDW)

        val readGraph = neo4j.readFromDB(true, true)
        assertEquals(graphDW.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
        assertEquals(graphDW.getEdges().size, readGraph.getEdges().size)
    }

    @Test
    @DisplayName("Запись и чтение ненаправленного взвешенного графа")
    fun undirectedWeightedGraph() {
        createGraphUW()
        neo4j.writeDB(graphUW)

        val readGraph = neo4j.readFromDB(false, true)
        assertEquals(graphUW.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
        assertEquals(graphUW.getEdges().size, readGraph.getEdges().size)
    }

    @Test
    @DisplayName("Запись и чтение ненаправленного невзвешенного графа")
    fun undirectedUnweightedGraph() {
        createGraphUU()
        neo4j.writeDB(graphUU)

        val readGraph = neo4j.readFromDB(false, false)
        assertEquals(graphUU.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
        assertEquals(graphUU.getEdges().size, readGraph.getEdges().size)
    }

    @Test
    @DisplayName("Очистка базы данных")
    fun clearDatabaseTest() {
        createGraphDU()
        neo4j.writeDB(graphDU)
        neo4j.clearDatabase()

        assertTrue(neo4j.readFromDB(true, false).getMap().isEmpty())
    }

// добавить тесты для ошибок

    private fun createGraphDU() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graphDU.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b)
        }
    }

    private fun createGraphDW() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graphDW.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b, 5.0)
        }
    }

    private fun createGraphUW() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graphUW.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b, 3.0)
        }
    }

    private fun createGraphUU() {
        val a = Vertex(1, "A")
        val b = Vertex(2, "B")
        graphUU.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b)
        }
    }
}