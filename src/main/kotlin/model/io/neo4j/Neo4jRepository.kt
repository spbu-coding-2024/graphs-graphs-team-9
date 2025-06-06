package model.io.neo4j

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.graph.Graph
import model.graph.GraphImpl
import org.neo4j.driver.*
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.harness.Neo4jBuilders
import javax.naming.AuthenticationException
import javax.naming.ServiceUnavailableException

class Neo4jRepository(
    private val uri: String,
    private val user: String,
    private val password: String,
) {
    private suspend fun <T> withDriver(block: suspend (Driver) -> T): T {
        val driver =
            try {
                GraphDatabase.driver(uri, AuthTokens.basic(user, password))
            } catch (e: ClientException) {
                throw Exception("Failed to create Neo4j session, wrong uri, username or password", e)
            } catch (e: AuthenticationException) {
                throw Exception("Neo4j authentication failed", e)
            } catch (e: ServiceUnavailableException) {
                throw Exception("Could not connect to Neo4j database at $uri", e)
            }

        return try {
            withContext(Dispatchers.IO) {
                block(driver)
            }
        } finally {
            driver.close()
        }
    }

    private suspend fun <T> withSession(
        driver: Driver,
        block: suspend (Session) -> T,
    ): T {
        val session = driver.session()
        return try {
            block(session)
        } finally {
            session.close()
        }
    }

    suspend fun writeDB(graph: Graph) =
        withDriver { driver ->
            try {
                val session = driver.session()

                for (g in graph) {
                    session.executeWrite { tc ->
                        tc.run(
                            "MERGE (v: Vertex {id: \$id, name: \$name})",
                            parameters(
                                "id",
                                g.first.id,
                                "name",
                                g.first.name,
                            ),
                        ).consume()
                    }
                }
                for (edge in graph.getEdges()) {
                    session.executeWrite { tc ->
                        tc.run(
                            "MATCH (v1:Vertex {id: \$id1}), (v2:Vertex {id: \$id2})" +
                                "WHERE NOT (v1)-[:CONNECT]->(v2) CREATE (v1)-[r:CONNECT]->(v2) SET r.weight = \$weight",
                            parameters(
                                "id1",
                                edge.source.id,
                                "id2",
                                edge.destination.id,
                                "weight",
                                edge.weight ?: Values.NULL,
                            ),
                        ).consume()
                    }
                }
                session.close()
                driver.close()
            } catch (e: Exception) {
                throw Exception("Failed to write graph", e)
            }
        }

    suspend fun readFromDB(
        isDirected: Boolean,
        isWeighted: Boolean,
    ): Graph =
        withDriver { driver ->
            val graph = GraphImpl(isDirected, isWeighted)

            try {
                withSession(driver) { session ->
                    session.executeRead { tx ->
                        tx.run("MATCH (v:Vertex) RETURN v.id AS id, v.name AS name").list { record ->
                            graph.addVertex(record["name"].asString())
                        }
                    }

                    session.executeRead { tx ->
                        tx.run(
                            "MATCH (v1:Vertex)-[r:CONNECT]->(v2:Vertex) " +
                                "RETURN v1.id AS sourceId, v2.id AS targetId, r.weight AS weight",
                        ).forEach { record ->
                            val sourceId = record["sourceId"].asInt()
                            val targetId = record["targetId"].asInt()
                            val weight = if (record["weight"].isNull) null else record["weight"].asDouble()

                            val v1 = graph.getVertexByKey(sourceId)!!
                            val v2 = graph.getVertexByKey(targetId)!!

                            graph.addEdge(v1.name, v2.name, weight)
                        }
                    }
                }
            } catch (e: Exception) {
                throw Exception("Failed to read graph", e)
            }

            graph
        }

    suspend fun clearDatabase() =
        withDriver { driver ->
            try {
                withSession(driver) { session ->
                    session.executeWrite { tc ->
                        tc.run("MATCH (n) DETACH DELETE n").consume()
                    }
                }
            } catch (e: Exception) {
                throw Exception("Failed to clear database", e)
            }
        }

    companion object {
        fun createEmbedded(): Neo4jRepository {
            Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .build()

            return Neo4jRepository(
                "bolt://localhost:7687",
                "neo4j",
                "password",
            )
        }
    }
}
