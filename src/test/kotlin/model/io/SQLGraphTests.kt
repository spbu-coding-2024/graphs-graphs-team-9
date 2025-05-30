package model.io

import model.graph.GraphImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLGraphTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var dbPath: String
    private lateinit var sqlGraph: SQLGraph

    open class TestSQLGraph(dbPath: String) : SQLGraph(dbPath) {
        public override fun connect(): Connection {
            return super.connect()
        }
    }

    @BeforeEach
    fun setUp() {
        dbPath = tempDir.resolve("test-graph.db").toString()
        sqlGraph = TestSQLGraph(dbPath)
        sqlGraph.initializeDatabase()
    }

    @AfterEach
    fun tearDown() {
        val file = File(dbPath)
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun `test database initialization`() {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            val tables = getTables(conn)
            assertTrue(tables.contains("graph_metadata"))
            assertTrue(tables.contains("vertices"))
            assertTrue(tables.contains("edges"))
        }
    }

    private fun getTables(connection: Connection): List<String> {
        val tables = mutableListOf<String>()
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'").use { resultSet ->
                while (resultSet.next()) {
                    tables.add(resultSet.getString("name"))
                }
            }
        }
        return tables
    }

    @Test
    fun `test save and load empty graph`() {
        val graph = GraphImpl(isDirected = false, isWeighted = false)
        sqlGraph.saveGraph(graph)

        val loadedGraph = sqlGraph.loadGraph()
        assertNotNull(loadedGraph)
        assertFalse(loadedGraph?.isDirected() ?: throw IllegalStateException())
        assertFalse(loadedGraph.isWeighted())
        assertTrue(loadedGraph.getVertices().isEmpty())
        assertTrue(loadedGraph.getEdges().isEmpty())
    }

    @Test
    fun `test save and load directed weighted graph with vertices and edges`() {
        val graph = GraphImpl(isDirected = true, isWeighted = true)

        graph.addVertex("A")
        graph.addVertex("B")
        graph.addVertex("C")

        graph.addEdge("A", "B", 1.5)
        graph.addEdge("B", "C", 2.0)
        graph.addEdge("C", "A", 3.5)

        sqlGraph.saveGraph(graph)

        val loadedGraph = sqlGraph.loadGraph()

        assertNotNull(loadedGraph)
        assertTrue(loadedGraph?.isDirected() ?: throw IllegalStateException())
        assertTrue(loadedGraph.isWeighted())

        val vertices = loadedGraph.getVertices()
        assertEquals(3, vertices.size)

        val vertexNames = vertices.map { it.name }.toSet()
        assertTrue(vertexNames.contains("A"))
        assertTrue(vertexNames.contains("B"))
        assertTrue(vertexNames.contains("C"))

        val edges = loadedGraph.getEdges()
        assertEquals(3, edges.size)

        val foundEdges = mutableListOf<Pair<String, String>>()
        var edgeABWeight: Double? = null
        var edgeBCWeight: Double? = null
        var edgeCAWeight: Double? = null

        edges.forEach { edge ->
            val sourceDestPair = Pair(edge.source.name, edge.destination.name)
            foundEdges.add(sourceDestPair)

            when (sourceDestPair) {
                Pair("A", "B") -> edgeABWeight = edge.weight
                Pair("B", "C") -> edgeBCWeight = edge.weight
                Pair("C", "A") -> edgeCAWeight = edge.weight
            }
        }

        assertTrue(foundEdges.contains(Pair("A", "B")))
        assertTrue(foundEdges.contains(Pair("B", "C")))
        assertTrue(foundEdges.contains(Pair("C", "A")))

        assertEquals(1.5, edgeABWeight)
        assertEquals(2.0, edgeBCWeight)
        assertEquals(3.5, edgeCAWeight)
    }

    @Test
    fun `test save and load undirected unweighted graph with vertices and edges`() {
        val graph = GraphImpl(isDirected = false, isWeighted = false)

        graph.addVertex("X")
        graph.addVertex("Y")
        graph.addEdge("X", "Y", null)

        sqlGraph.saveGraph(graph)

        val loadedGraph = sqlGraph.loadGraph()

        assertNotNull(loadedGraph)
        assertFalse(loadedGraph?.isDirected() ?: throw IllegalStateException())
        assertFalse(loadedGraph.isWeighted())

        val vertices = loadedGraph.getVertices()
        assertEquals(2, vertices.size)

        val edges = loadedGraph.getEdges()
        assertEquals(1, edges.size)

        val edge = edges.first()
        assertNull(edge.weight)

        val sourceDestPair = setOf(edge.source.name, edge.destination.name)
        assertEquals(setOf("X", "Y"), sourceDestPair)
    }

    @Test
    fun `test overwrite existing graph`() {
        val graph1 = GraphImpl(isDirected = true, isWeighted = true)
        graph1.addVertex("A")
        graph1.addVertex("B")
        graph1.addEdge("A", "B", 1.0)
        sqlGraph.saveGraph(graph1)

        val graph2 = GraphImpl(isDirected = false, isWeighted = false)
        graph2.addVertex("C")
        graph2.addVertex("D")
        graph2.addEdge("C", "D", null)
        sqlGraph.saveGraph(graph2)

        val loadedGraph = sqlGraph.loadGraph()
        assertNotNull(loadedGraph)
        assertFalse(loadedGraph?.isDirected() ?: throw IllegalStateException())
        assertFalse(loadedGraph.isWeighted())

        val vertices = loadedGraph.getVertices()
        assertEquals(2, vertices.size)

        val vertexNames = vertices.map { it.name }.toSet()
        assertEquals(setOf("C", "D"), vertexNames)

        val edges = loadedGraph.getEdges()
        assertEquals(1, edges.size)
    }

    @Test
    fun `test SQL exception handling during save`() {
        val graph = GraphImpl(true, true)
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addEdge("A", "B", 1.0)

        val mockSqlGraph = object : TestSQLGraph(dbPath) {
            override fun connect(): Connection {
                val realConnection = super.connect()

                return object : Connection by realConnection {
                    override fun prepareStatement(sql: String): java.sql.PreparedStatement {
                        if (sql.contains("INSERT INTO graph_metadata")) {
                            throw SQLException("Simulated failure during save")
                        }
                        return realConnection.prepareStatement(sql)
                    }
                }
            }
        }

        val exception = assertThrows(SQLException::class.java) {
            mockSqlGraph.saveGraph(graph)
        }

        assertEquals("Simulated failure during save", exception.message)
    }

    @Test
    fun `test vertex integrity issue handling`() {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("INSERT INTO graph_metadata (is_directed, is_weighted) VALUES (0, 0)")
                stmt.execute("INSERT INTO vertices (id, name) VALUES (1, 'A')")
                stmt.execute("INSERT INTO edges (source_vertex_id, destination_vertex_id, weight) VALUES (1, 2, NULL)")
            }
        }

        val originalErr = System.err
        val errContent = ByteArrayOutputStream()
        System.setErr(PrintStream(errContent))

        try {
            val loadedGraph = sqlGraph.loadGraph()

            assertNotNull(loadedGraph)
            assertEquals(1, loadedGraph?.getVertices()?.size ?: throw IllegalStateException())
            assertEquals(0, loadedGraph.getEdges().size)

            val errorOutput = errContent.toString()
            assertTrue(errorOutput.contains("Data integrity issue: Vertex not found for edge (1 -> 2). Edge skipped."))

        } finally {
            System.setErr(originalErr)
        }
    }

    @Test
    fun `test load empty database returns null`() {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("DROP TABLE IF EXISTS graph_metadata")
                stmt.execute("DROP TABLE IF EXISTS vertices")
                stmt.execute("DROP TABLE IF EXISTS edges")
            }
        }

        val loadedGraph = sqlGraph.loadGraph()
        assertNull(loadedGraph)
    }
}
