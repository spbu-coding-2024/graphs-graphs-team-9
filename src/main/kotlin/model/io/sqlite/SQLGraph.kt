package model.io.sqlite

import model.graph.GraphImpl
import model.graph.Vertex
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Types

open class SQLGraph(private val dbPath: String) {
    protected open fun connect(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun initializeDatabase() {
        connect().use { conn ->
            conn.createStatement().use { statement ->

                val createGraphMetadataTable =
                    """
                    CREATE TABLE IF NOT EXISTS graph_metadata (
                        id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
                        is_directed INTEGER NOT NULL,
                        is_weighted INTEGER NOT NULL
                    )
                    """.trimIndent()
                statement.execute(createGraphMetadataTable)

                val createVerticesTable =
                    """
                    CREATE TABLE IF NOT EXISTS vertices (
                        id INTEGER PRIMARY KEY,
                        name TEXT
                    )
                    """.trimIndent()
                statement.execute(createVerticesTable)

                val createEdgesTable =
                    """
                    CREATE TABLE IF NOT EXISTS edges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        source_vertex_id INTEGER NOT NULL,
                        destination_vertex_id INTEGER NOT NULL,
                        weight REAL,
                        FOREIGN KEY (source_vertex_id) REFERENCES vertices(id) ON DELETE CASCADE,
                        FOREIGN KEY (destination_vertex_id) REFERENCES vertices(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                statement.execute(createEdgesTable)
            }
        }
    }

    fun saveGraph(graph: GraphImpl) {
        connect().use { conn ->
            conn.autoCommit = false
            try {
                conn.createStatement().use { stmt ->
                    stmt.execute("DELETE FROM graph_metadata")
                    stmt.execute("DELETE FROM edges")
                    stmt.execute("DELETE FROM vertices")
                }

                val insertMetadataSql = "INSERT INTO graph_metadata (is_directed, is_weighted) VALUES (?, ?)"
                conn.prepareStatement(insertMetadataSql).use { pstmt ->
                    pstmt.setInt(1, if (graph.isDirected()) 1 else 0)
                    pstmt.setInt(2, if (graph.isWeighted()) 1 else 0)
                    pstmt.executeUpdate()
                }

                if (graph.getVertices().isNotEmpty()) {
                    val insertVertexSql = "INSERT INTO vertices (id, name) VALUES (?, ?)"
                    conn.prepareStatement(insertVertexSql).use { pstmt ->
                        graph.getVertices().forEach { vertex ->
                            pstmt.setInt(1, vertex.id)
                            pstmt.setString(2, vertex.name)
                            pstmt.addBatch()
                        }
                        pstmt.executeBatch()
                    }
                }

                val edgesToSave = graph.getEdges()
                if (edgesToSave.isNotEmpty()) {
                    val insertEdgeSql =
                        """
                        INSERT INTO edges (source_vertex_id, destination_vertex_id, weight)
                        VALUES (?, ?, ?)
                        """.trimIndent()
                    conn.prepareStatement(insertEdgeSql).use { pstmt ->
                        edgesToSave.forEach { edge ->
                            pstmt.setInt(1, edge.source.id)
                            pstmt.setInt(2, edge.destination.id)
                            if (graph.isWeighted() && edge.weight != null) {
                                pstmt.setDouble(3, edge.weight ?: throw IllegalArgumentException())
                            } else {
                                pstmt.setNull(3, Types.REAL)
                            }
                            pstmt.addBatch()
                        }
                        pstmt.executeBatch()
                    }
                }
                conn.commit()
            } catch (e: SQLException) {
                conn.rollback()
                throw e
            }
        }
    }

    fun loadGraph(): GraphImpl? {
        connect().use { conn ->
            if (!tableExists(conn, "graph_metadata") ||
                !tableExists(conn, "vertices") ||
                !tableExists(conn, "edges")
            ) {
                return null
            }
            var isDirectedGraph = false
            var isWeightedGraph = false

            conn.createStatement().use { stmt ->
                val rsMetadata = stmt.executeQuery("SELECT is_directed, is_weighted FROM graph_metadata WHERE id = 1")
                if (rsMetadata.next()) {
                    isDirectedGraph = rsMetadata.getInt("is_directed") == 1
                    isWeightedGraph = rsMetadata.getInt("is_weighted") == 1
                }
            }

            val graph = GraphImpl(isDirectedGraph, isWeightedGraph)
            val vertices = mutableMapOf<Int, Vertex>()

            conn.createStatement().use { stmt ->
                val rsVertices = stmt.executeQuery("SELECT id, name FROM vertices")
                while (rsVertices.next()) {
                    val id = rsVertices.getInt("id")
                    val name = rsVertices.getString("name")
                    val vertex = Vertex(id, name)
                    vertices[id] = vertex
                    graph.addVertex(name)
                }
            }

            val sqlQueryEdges = "SELECT source_vertex_id, destination_vertex_id, weight FROM edges"
            conn.createStatement().use { stmt ->
                val rsEdges = stmt.executeQuery(sqlQueryEdges)
                while (rsEdges.next()) {
                    val sourceId = rsEdges.getInt("source_vertex_id")
                    val destinationId = rsEdges.getInt("destination_vertex_id")

                    val weight: Double? =
                        if (isWeightedGraph) {
                            val weightObj = rsEdges.getObject("weight")
                            (weightObj as? Number)?.toDouble()
                        } else {
                            null
                        }

                    val sourceVertex = vertices[sourceId]
                    val destinationVertex = vertices[destinationId]

                    if (sourceVertex != null && destinationVertex != null) {
                        graph.addEdge(sourceVertex.name, destinationVertex.name, weight)
                    } else {
                        System.err.println("Data integrity issue: Vertex not found for edge ($sourceId -> $destinationId). Edge skipped.")
                    }
                }
            }
            return graph
        }
    }
}

@Throws(SQLException::class)
private fun tableExists(
    connection: Connection,
    tableName: String?,
): Boolean {
    val meta = connection.metaData
    val resultSet = meta.getTables(null, null, tableName, arrayOf<String>("TABLE"))

    return resultSet.next()
}
