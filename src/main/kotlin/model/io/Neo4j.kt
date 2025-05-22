package model.io.Neo4j

import model.graph.Graph
import model.graph.GraphImpl
import model.graph.Vertex
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.exceptions.ClientException
import javax.naming.AuthenticationException
import javax.naming.ServiceUnavailableException


class Neo4j(private val uri: String, private val user: String, private val password: String) {

    private fun createDriver() = try {
        GraphDatabase.driver(uri, AuthTokens.basic(user, password))
    } catch (e: ClientException) {
        throw Exception("Failed to create Neo4j session, wrong uri, username or password", e)
    } catch (e: AuthenticationException) {
        throw Exception("Neo4j authentication failed", e)
    } catch (e: ServiceUnavailableException) {
        throw Exception("Could not connect to Neo4j database at $uri", e)
    }

    fun writeDB(graph: Graph) {
        val driver = createDriver()
        try {
            val session = driver.session()

            for (g in graph) {
                session.executeWrite { tc ->
                    tc.run(
                        "MERGE (v: Vertex {id: \$id, name: \$name})",
                        parameters(
                            "id", g.first.id,
                            "name", g.first.name
                        )
                    ).consume()
                }
            }
            for (edge in graph.getEdges()) {
                session.executeWrite { tc ->
                    tc.run(
                        "MATCH (v1:Vertex {id: \$id1}), (v2:Vertex {id: \$id2})" +
                                "WHERE NOT (v1)-[:CONNECT]->(v2) CREATE (v1)-[r:CONNECT]->(v2) SET r.weight = \$weight",
                        parameters(
                            "id1", edge.source.id,
                            "id2", edge.destination.id,
                            "weight", edge.weight ?: Values.NULL
                        )
                    ).consume()
                }

            }
            session.close()
            driver.close()
        } catch (e: Exception) {
            throw Exception("Failed to write graph", e)
        }
    }

    fun readFromDB(isDirected: Boolean, isWeighted: Boolean): Graph {
        val graph = GraphImpl(isDirected, isWeighted)
        val driver = createDriver()
        try {
            val session = driver.session()
            session.executeRead { tx ->
                tx.run("MATCH (v:Vertex) RETURN v.id AS id, v.name AS name").list { record ->
                    graph.addVertex(Vertex(record["id"].asInt(), record["name"].asString()))
                }
            }

            session.executeRead { tx ->
                tx.run(
                    "MATCH (v1:Vertex)-[r:CONNECT]->(v2:Vertex)" +
                            "RETURN v1.id AS sourceId, v2.id AS targetId, r.weight AS weight"
                ).forEach { record ->
                    val sourceId = record["sourceId"].asInt()
                    val targetId = record["targetId"].asInt()
                    val weight = if (record["weight"].isNull) null else record["weight"].asDouble()

                    val v1 = graph.getVertexByKey(sourceId)!!
                    val v2 = graph.getVertexByKey(targetId)!!

                    graph.addEdge(v1, v2, weight)
                }
            }
            session.close()
            driver.close()
        } catch (e: Exception) {
            throw Exception("Failed to read graph", e)
        }
        return graph
    }

    fun clearDatabase() {
        val driver = createDriver()
        try {
            val session = driver.session()
            session.executeWrite { tc ->
                tc.run("MATCH (n) DETACH DELETE n").consume()
            }
            session.close()
            driver.close()
        } catch (e: Exception) {
            throw Exception("Failed to clear database", e)
        }
    }
}





//class Neo4j(private val uri: String, private val user: String, private val password: String) {
//
//    private fun createDriver() = try {
//        GraphDatabase.driver(uri, AuthTokens.basic(user, password))
//    } catch (e: ClientException) {
//        throw Exception("Failed to create Neo4j session, wrong uri, username or password", e)
//    } catch (e: AuthenticationException) {
//        throw Exception("Neo4j authentication failed", e)
//    } catch (e: ServiceUnavailableException) {
//        throw Exception("Could not connect to Neo4j database at $uri", e)
//    }
//
//    fun writeDB(graph: Graph) {
//        val driver = createDriver()
//        try {
//            val session = driver.session()
//            for (g in graph) {
//                session.executeWrite { tc ->
//                    tc.run(
//                        "MERGE (v: Vertex {id: \$id, name: \$name})",
//                        parameters(
//                            "id", g.first.id,
//                            "name", g.first.name
//                        )
//                    ).consume()
//                }
//            }
//            for (edge in graph.getEdges()) {
//                session.executeWrite { tc ->
//                    tc.run(
//                        "MATCH (v1:Vertex {id: \$id1}), (v2:Vertex {id: \$id2})" +
//                                "WHERE NOT (v1)-[:CONNECT]->(v2) CREATE (v1)-[r:CONNECT]->(v2) SET r.weight = \$weight",
//                        parameters(
//                            "id1", edge.source.id,
//                            "id2", edge.destination.id,
//                            "weight", edge.weight ?: Values.NULL
//                        )
//                    ).consume()
//                }
//
//            }
//            session.close()
//            driver.close()
//        } catch (e: Exception) {
//            Exception("Failed to write graph", e)
//        }
//    }
//
//    fun readFromDB(isDirected: Boolean, isWeighted: Boolean): Graph {
//        val graph = GraphImpl(isDirected, isWeighted)
//        val driver = createDriver()
//        val session = driver.session()
//        try {
//            session.executeRead { tx ->
//                tx.run(
//                    "MATCH (v1:Vertex)-[r:CONNECT]->(v2:Vertex)" +
//                            "RETURN v1.id AS sourceId, v2.id AS targetId, r.weight AS weight"
//                ).forEach { record ->
//                    val sourceId = record["sourceId"].asInt()
//                    val targetId = record["targetId"].asInt()
//                    val weight = if (record["weight"].isNull) null else record["weight"].asDouble()
//
//                    val v1 = graph.getVertexByKey(sourceId)!!
//                    val v2 = graph.getVertexByKey(targetId)!!
//
//                    graph.addEdge(v1, v2, weight)
//                }
//            }
//            session.close()
//            driver.close()
//        } catch (e: Exception) {
//            Exception("Failed to read graph", e)
//        }
//        return graph
//    }
//
//    fun clearDatabase() {
//        createDriver().use { driver ->
//            driver.session().use { session ->
//                session.executeWrite { tx ->
//                    try {
//                        tx.run("MATCH (n) DETACH DELETE n").consume()
//                        session.close()
//                        driver.close()
//                    } catch (e: Exception) {
//                        Exception("Failed to read graph", e)
//                    }
//                }
//            }
//        }
//    }
//}