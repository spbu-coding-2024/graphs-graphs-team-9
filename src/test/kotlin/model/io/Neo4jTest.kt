package model.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import model.graph.Graph
import model.graph.GraphFactory
import model.io.neo4j.Neo4jRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.neo4j.harness.Neo4j
import org.neo4j.harness.Neo4jBuilders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class Neo4jTest {
    private lateinit var graphDW: Graph
    private lateinit var graphDU: Graph
    private lateinit var graphUW: Graph
    private lateinit var graphUU: Graph
    private lateinit var neo4jServer: Neo4j
    private lateinit var neo4j: Neo4jRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        graphDW = GraphFactory.createDirectedWeightedGraph()
        graphDU = GraphFactory.createDirectedUnweightedGraph()
        graphUW = GraphFactory.createUndirectedWeightedGraph()
        graphUU = GraphFactory.createUndirectedUnweightedGraph()

        neo4jServer = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build()
        neo4j = Neo4jRepository(neo4jServer.boltURI().toString(), "neo4j", "password")
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        neo4jServer.close()
    }

    @Test
    @DisplayName("Пустой граф")
    fun emptyGraph() {
        runTest {
            neo4j.clearDatabase()
            neo4j.writeDB(graphDU)
            assertEquals(mapOf(), neo4j.readFromDB(true, false).getMap())
            neo4j.writeDB(graphDW)
            assertTrue(neo4j.readFromDB(true, false).getMap().isEmpty())
        }
    }

    @Test
    @DisplayName("Проверка корректной отправки")
    fun correctSending() {
        runTest {
            createGraphDU()
            neo4j.writeDB(graphDU)
            val g = neo4j.readFromDB(graphDU.isDirected(), graphDU.isWeighted())
            assertEquals(graphDU.getVertices().sortedBy { it.id }, g.getVertices().sortedBy { it.id })
        }
    }

    @Test
    @DisplayName("Запись и чтение направленного невзвешенного графа")
    fun directedUnweightedGraph() {
        runTest {
            createGraphDU()
            neo4j.writeDB(graphDU)

            val readGraph = neo4j.readFromDB(true, false)
            assertEquals(graphDU.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
            assertEquals(graphDU.getEdges().size, readGraph.getEdges().size)
        }
    }

    @Test
    @DisplayName("Запись и чтение направленного взвешенного графа")
    fun directedWeightedGraph() {
        runTest {
            createGraphDW()
            neo4j.writeDB(graphDW)

            val readGraph = neo4j.readFromDB(true, true)
            assertEquals(graphDW.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
            assertEquals(graphDW.getEdges().size, readGraph.getEdges().size)
        }
    }

    @Test
    @DisplayName("Запись и чтение ненаправленного взвешенного графа")
    fun undirectedWeightedGraph() {
        runTest {
            createGraphUW()
            neo4j.writeDB(graphUW)

            val readGraph = neo4j.readFromDB(false, true)
            assertEquals(graphUW.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
            assertEquals(graphUW.getEdges().size, readGraph.getEdges().size)
        }
    }

    @Test
    @DisplayName("Запись и чтение ненаправленного невзвешенного графа")
    fun undirectedUnweightedGraph() {
        runTest {
            createGraphUU()
            neo4j.writeDB(graphUU)

            val readGraph = neo4j.readFromDB(false, false)
            assertEquals(graphUU.getVertices().sortedBy { it.id }, readGraph.getVertices().sortedBy { it.id })
            assertEquals(graphUU.getEdges().size, readGraph.getEdges().size)
        }
    }

    @Test
    @DisplayName("Очистка базы данных")
    fun clearDatabaseTest() {
        runTest {
            createGraphDU()
            neo4j.writeDB(graphDU)
            neo4j.clearDatabase()

            assertTrue(neo4j.readFromDB(true, false).getMap().isEmpty())
        }
    }

    private fun createGraphDU() {
        val a = "A"
        val b = "B"
        graphDU.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b)
        }
    }

    private fun createGraphDW() {
        val a = "A"
        val b = "B"
        graphDW.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b, 5.0)
        }
    }

    private fun createGraphUW() {
        val a = "A"
        val b = "B"
        graphUW.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b, 3.0)
        }
    }

    private fun createGraphUU() {
        val a = "A"
        val b = "B"
        graphUU.apply {
            addVertex(a)
            addVertex(b)
            addEdge(a, b)
        }
    }
}
