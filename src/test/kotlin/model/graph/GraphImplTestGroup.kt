package model.graph

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphImplTestGroup {
    private val vA = "A"
    private val vB = "B"
    private val vC = "C"
    private val vD = "D"

    @Nested
    @DisplayName("Тесты для Неориентированного Невзвешенного Графа")
    inner class UndirectedUnweightedGraphTest {
        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            graph = GraphFactory.createUndirectedUnweightedGraph()
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertFalse(graph.isDirected(), "isDirected должно быть false")
            assertFalse(graph.isWeighted(), "isWeighted должно быть false")
        }

        @Test
        @DisplayName("Добавление вершины")
        fun addVertex() {
            assertEquals(0, graph.getVertexCount(), "Изначально граф должен быть пуст")
            graph.addVertex(vA)
            assertTrue(graph.containsVertex(vA), "Граф должен содержать вершину A")
            assertEquals(1, graph.getVertexCount(), "Количество вершин должно быть 1")
            graph.addVertex(vB)
            assertTrue(graph.containsVertex(vB), "Граф должен содержать вершину B")
            assertEquals(2, graph.getVertexCount(), "Количество вершин должно быть 2")
        }

        @Test
        @DisplayName("Добавление существующей вершины не меняет граф")
        fun addExistingVertex() {
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount())
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount(), "Количество вершин не должно измениться")
        }

        @Test
        @DisplayName("Получение списка вершин")
        fun getVertices() {
            graph.addVertex(vA)
            graph.addVertex(vB)

            val vertices = graph.getVertices()
            assertEquals(2, vertices.size)
            assertTrue(vertices.any { it.name == vA })
            assertTrue(vertices.any { it.name == vB })
        }

        @Test
        @DisplayName("Добавление ребра (неориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertEquals(0, graph.getEdgeCount(), "Изначально ребер быть не должно")

            graph.addEdge(vA, vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")
            assertTrue(graph.containsEdge(vertexA, vertexB), "Граф должен содержать ребро A - B")
            assertTrue(graph.containsEdge(vertexB, vertexA), "Граф должен содержать ребро B - A (т.к. неориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1 (не удваивается)")

            assertTrue(graph.getNeighbors(vertexA).map { it.name }.contains(vB), "B должна быть соседом A")
            assertTrue(graph.getNeighbors(vertexB).map { it.name }.contains(vA), "A должна быть соседом B")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertEquals(0, graph.getVertexCount())
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление ребра (неориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")
            assertEquals(1, graph.getEdgeCount())

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vertexA, vertexB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vertexB, vertexA), "Ребро B - A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertFalse(graph.getNeighbors(vertexA).map { it.name }.contains(vB), "B не должна быть соседом A")
            assertFalse(graph.getNeighbors(vertexB).map { it.name }.contains(vA), "A не должна быть соседом B")

            graph.addEdge(vA, vB)
            assertEquals(1, graph.getEdgeCount())
            graph.removeEdge(vB, vA)
            assertFalse(graph.containsEdge(vertexA, vertexB), "Ребро A - B должно быть удалено после удаления B-A")
            assertFalse(graph.containsEdge(vertexB, vertexA), "Ребро B - A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
        }

        @Test
        @DisplayName("Удаление несуществующего ребра не вызывает ошибок")
        fun removeNonExistentEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertDoesNotThrow { graph.removeEdge(vA, vB) }
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление вершины (удаляет связанные ребра)")
        fun removeVertex() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB) // A - B
            graph.addEdge(vB, vC) // B - C

            assertEquals(3, graph.getVertexCount())
            assertEquals(2, graph.getEdgeCount())

            graph.removeVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexC = graph.getVertexByName(vC) ?: throw IllegalStateException("Вершина должна быть найдена")

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")

            assertTrue(graph.getNeighbors(vertexA).isEmpty(), "У A не должно быть соседей после удаления B")
            assertTrue(graph.getNeighbors(vertexC).isEmpty(), "У C не должно быть соседей после удаления B")
        }

        @Test
        @DisplayName("Получение соседей (неориентированный)")
        fun getNeighbors() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vA, vC)

            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexC = graph.getVertexByName(vC) ?: throw IllegalStateException("Вершина должна быть найдена")

            val neighborsA = graph.getNeighbors(vertexA)
            assertEquals(2, neighborsA.size)
            assertTrue(neighborsA.map { it.name }.containsAll(listOf(vB, vC)), "Соседи A должны быть B и C")

            val neighborsB = graph.getNeighbors(vertexB)
            assertEquals(1, neighborsB.size)
            assertTrue(neighborsB.map { it.name }.contains(vA), "Сосед B должен быть A")

            val neighborsC = graph.getNeighbors(vertexC)
            assertEquals(1, neighborsC.size)
            assertTrue(neighborsC.map { it.name }.contains(vA), "Сосед C должен быть A")
        }

        @Test
        @DisplayName("Получение всех рёбер графа")
        fun getEdges() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vB, vC)

            val edges = graph.getEdges()
            assertEquals(2, edges.size)

            assertTrue(
                edges.any { (it.source.name == vA && it.destination.name == vB) || (it.source.name == vB && it.destination.name == vA) },
            )
            assertTrue(
                edges.any { (it.source.name == vB && it.destination.name == vC) || (it.source.name == vC && it.destination.name == vB) },
            )
        }
    }

    @Nested
    @DisplayName("Тесты для Ориентированного Невзвешенного Графа")
    inner class DirectedUnweightedGraphTest {
        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            graph = GraphFactory.createDirectedUnweightedGraph()
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertTrue(graph.isDirected(), "isDirected должно быть true")
            assertFalse(graph.isWeighted(), "isWeighted должно быть false")
        }

        @Test
        @DisplayName("Добавление вершины")
        fun addVertex() {
            assertEquals(0, graph.getVertexCount(), "Изначально граф должен быть пуст")
            graph.addVertex(vA)
            assertTrue(graph.containsVertex(vA), "Граф должен содержать вершину A")
            assertEquals(1, graph.getVertexCount(), "Количество вершин должно быть 1")
            graph.addVertex(vB)
            assertTrue(graph.containsVertex(vB), "Граф должен содержать вершину B")
            assertEquals(2, graph.getVertexCount(), "Количество вершин должно быть 2")
        }

        @Test
        @DisplayName("Добавление существующей вершины не меняет граф")
        fun addExistingVertex() {
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount())
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount(), "Количество вершин не должно измениться")
        }

        @Test
        @DisplayName("Добавление ребра (ориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertEquals(0, graph.getEdgeCount(), "Изначально ребер быть не должно")

            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB) // Add by name
            assertTrue(graph.containsEdge(vertexA, vertexB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vertexB, vertexA), "Граф не должен содержать ребро B -> A (т.к. ориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")

            assertTrue(graph.getNeighbors(vertexA).map { it.name }.contains(vB), "B должна быть соседом A")
            assertFalse(graph.getNeighbors(vertexB).map { it.name }.contains(vA), "A не должна быть соседом B")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertEquals(0, graph.getVertexCount())
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление ребра")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            assertEquals(1, graph.getEdgeCount())
            assertTrue(graph.containsEdge(vertexA, vertexB))

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vertexA, vertexB), "Ребро A -> B должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertFalse(graph.getNeighbors(vertexA).map { it.name }.contains(vB), "B не должна быть соседом A после удаления ребра")
        }

        @Test
        @DisplayName("Удаление несуществующего ребра не вызывает ошибок")
        fun removeNonExistentEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertDoesNotThrow { graph.removeEdge(vA, vB) }
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление вершины (удаляет связанные ребра)")
        fun removeVertex() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB) // A -> B
            graph.addEdge(vC, vB) // C -> B
            graph.addEdge(vB, vA) // B -> A

            assertEquals(3, graph.getVertexCount())
            assertEquals(3, graph.getEdgeCount())

            graph.removeVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexC = graph.getVertexByName(vC) ?: throw IllegalStateException("Вершина должна быть найдена")

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")

            assertTrue(graph.getNeighbors(vertexA).isEmpty(), "У A не должно быть исходящих соседей после удаления B")
            assertTrue(graph.getNeighbors(vertexC).isEmpty(), "У C не должно быть исходящих соседей после удаления B")
        }

        @Test
        @DisplayName("Получение соседей (ориентированный)")
        fun getNeighbors() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vA, vC)
            graph.addEdge(vB, vC)

            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexC = graph.getVertexByName(vC) ?: throw IllegalStateException("Вершина должна быть найдена")

            val neighborsA = graph.getNeighbors(vertexA)
            assertEquals(2, neighborsA.size)
            assertTrue(neighborsA.map { it.name }.containsAll(listOf(vB, vC)), "Соседи A должны быть B и C")

            val neighborsB = graph.getNeighbors(vertexB)
            assertEquals(1, neighborsB.size)
            assertTrue(neighborsB.map { it.name }.contains(vC), "Сосед B должен быть C")

            assertTrue(graph.getNeighbors(vertexC).isEmpty(), "У C не должно быть исходящих соседей")
        }

        @Test
        @DisplayName("Получение всех рёбер графа")
        fun getEdges() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vB, vC)

            val edges = graph.getEdges()
            assertEquals(2, edges.size)

            assertTrue(edges.any { it.source.name == vA && it.destination.name == vB })
            assertTrue(edges.any { it.source.name == vB && it.destination.name == vC })
        }
    }

    @Nested
    @DisplayName("Тесты для Неориентированного Взвешенного Графа")
    inner class UndirectedWeightedGraphTest {
        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            graph = GraphFactory.createUndirectedWeightedGraph()
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertFalse(graph.isDirected(), "isDirected должно быть false")
            assertTrue(graph.isWeighted(), "isWeighted должно быть true")
        }

        @Test
        @DisplayName("Добавление взвешенного ребра (неориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB, 5.0)
            assertTrue(graph.containsEdge(vertexA, vertexB), "Граф должен содержать ребро A - B")
            assertTrue(graph.containsEdge(vertexB, vertexA), "Граф должен содержать ребро B - A (т.к. неориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1 (не удваивается)")

            assertEquals(5.0, graph.getEdgeWeight(vertexA, vertexB), "Вес ребра A - B должен быть 5.0")
            assertEquals(5.0, graph.getEdgeWeight(vertexB, vertexA), "Вес ребра B - A должен быть 5.0")
        }

        @Test
        @DisplayName("Перезапись веса ребра")
        fun rewriteWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB, 10.0)
            assertEquals(10.0, graph.getEdgeWeight(vertexA, vertexB))
            assertEquals(10.0, graph.getEdgeWeight(vertexB, vertexA))

            graph.addEdge(vA, vB, 11.0) // Re-adding with a new weight
            assertEquals(11.0, graph.getEdgeWeight(vertexA, vertexB))
            assertEquals(11.0, graph.getEdgeWeight(vertexB, vertexA))
        }

        @Test
        @DisplayName("Удаление взвешенного ребра (неориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, 10.0)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vertexA, vertexB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vertexB, vertexA), "Ребро B - A должно быть удалено")
            assertNull(graph.getEdgeWeight(vertexA, vertexB), "Вес ребра A - B должен быть null")
            assertNull(graph.getEdgeWeight(vertexB, vertexA), "Вес ребра B - A должен быть null")
        }

        @Test
        @DisplayName("Добавление ребра без указания веса")
        fun addEdgeWithoutWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB) // Weight defaults to null if not provided
            assertTrue(graph.containsEdge(vertexA, vertexB), "Граф должен содержать ребро A - B")
            assertNull(graph.getEdgeWeight(vertexA, vertexB), "Вес ребра A - B должен быть null")
        }
    }

    @Nested
    @DisplayName("Тесты для Ориентированного Взвешенного Графа")
    inner class DirectedWeightedGraphTest {
        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            graph = GraphFactory.createDirectedWeightedGraph()
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertTrue(graph.isDirected(), "isDirected должно быть true")
            assertTrue(graph.isWeighted(), "isWeighted должно быть true")
        }

        @Test
        @DisplayName("Добавление взвешенного ребра (ориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB, 7.5)
            assertTrue(graph.containsEdge(vertexA, vertexB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vertexB, vertexA), "Граф не должен содержать ребро B -> A")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")

            assertEquals(7.5, graph.getEdgeWeight(vertexA, vertexB), "Вес ребра A -> B должен быть 7.5")
            assertNull(graph.getEdgeWeight(vertexB, vertexA), "Вес ребра B -> A должен быть null")
        }

        @Test
        @DisplayName("Перезапись веса ребра")
        fun rewriteWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.addEdge(vA, vB, 10.0)
            assertEquals(10.0, graph.getEdgeWeight(vertexA, vertexB))

            graph.addEdge(vA, vB, 11.0)
            assertEquals(11.0, graph.getEdgeWeight(vertexA, vertexB))
        }

        @Test
        @DisplayName("Удаление взвешенного ребра (ориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, 3.0)
            val vertexA = graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            val vertexB = graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vertexA, vertexB), "Ребро A -> B должно быть удалено")
            assertNull(graph.getEdgeWeight(vertexA, vertexB), "Вес ребра A -> B должен быть null")
        }

        @Test
        @DisplayName("Получение всех рёбер графа со сложной структурой")
        fun getEdgesComplexGraph() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addVertex(vD)

            graph.addEdge(vA, vB, 1.0)
            graph.addEdge(vB, vC, 2.0)
            graph.addEdge(vC, vD, 3.0)
            graph.addEdge(vD, vA, 4.0)
            graph.addEdge(vA, vC, 5.0)

            val edges = graph.getEdges()
            assertEquals(5, edges.size)

            assertTrue(edges.any { it.source.name == vA && it.destination.name == vB && it.weight == 1.0 })
            assertTrue(edges.any { it.source.name == vB && it.destination.name == vC && it.weight == 2.0 })
            assertTrue(edges.any { it.source.name == vC && it.destination.name == vD && it.weight == 3.0 })
            assertTrue(edges.any { it.source.name == vD && it.destination.name == vA && it.weight == 4.0 })
            assertTrue(edges.any { it.source.name == vA && it.destination.name == vC && it.weight == 5.0 })
        }
    }

    @Nested
    @DisplayName("Тесты для Iterator")
    inner class IteratorTest {
        @Test
        @DisplayName("Итератор для графа")
        fun graphIterator() {
            val graph = GraphFactory.createUndirectedUnweightedGraph()
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)

            graph.addEdge(vA, vB)
            graph.addEdge(vB, vC)

            graph.getVertexByName(vA) ?: throw IllegalStateException("Вершина должна быть найдена")
            graph.getVertexByName(vB) ?: throw IllegalStateException("Вершина должна быть найдена")
            graph.getVertexByName(vC) ?: throw IllegalStateException("Вершина должна быть найдена")

            val iterator = graph.iterator()
            var count = 0

            while (iterator.hasNext()) {
                val (vertex, edges) = iterator.next()
                count++
                when (vertex.name) {
                    vA -> {
                        assertEquals(1, edges.size)
                        assertEquals(vB, edges.first().destination.name)
                    }

                    vB -> {
                        assertEquals(2, edges.size, "Vertex B should have 2 edges")
                        assertTrue(edges.any { it.destination.name == vA }, "B should connect to A")
                        assertTrue(edges.any { it.destination.name == vC }, "B should connect to C")
                    }

                    vC -> {
                        assertEquals(1, edges.size)
                        assertEquals(vB, edges.first().destination.name)
                    }

                    else -> fail("Неизвестная вершина: $vertex")
                }
            }

            assertEquals(3, count, "Итератор должен пройти по 3 вершинам")
        }
    }

    @Nested
    inner class GraphPropertyBasedTestGroup {
        // Генератор случайных вершин (строк)
        private fun generateRandomVertexName(): String {
            return ('A'..'Z').random().toString() + Random.nextInt(1, 1000)
        }

        // Генератор случайного списка вершин
        private fun generateRandomVertexNames(count: Int): List<String> {
            val vertices = mutableSetOf<String>() // Use set to ensure uniqueness of names
            while (vertices.size < count) {
                vertices.add(generateRandomVertexName())
            }
            return vertices.toList()
        }

        // Генератор случайного веса
        private fun generateRandomWeight(): Double {
            return Random.nextDouble(0.1, 100.0)
        }

        @Nested
        @DisplayName("Property-Based тесты для Неориентированного Невзвешенного Графа")
        inner class UndirectedUnweightedGraphPropertyTest {
            @RepeatedTest(10)
            @DisplayName("Добавление множества вершин не приводит к дубликатам")
            fun addingMultipleVerticesDoesNotCreateDuplicates() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val vertexNames = generateRandomVertexNames(Random.nextInt(5, 20))

                // Добавляем все вершины из списка
                vertexNames.forEach { graph.addVertex(it) }

                // Проверяем, что все вершины добавлены
                vertexNames.forEach { assertTrue(graph.containsVertex(it)) }

                // Проверяем, что количество вершин равно количеству уникальных вершин
                assertEquals(vertexNames.distinct().size, graph.getVertexCount())
            }

            @RepeatedTest(10)
            @DisplayName("Удаление вершины удаляет все связанные ребра")
            fun removingVertexRemovesAllConnectedEdges() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val vertexNames = generateRandomVertexNames(Random.nextInt(3, 10))

                // Добавляем все вершины
                vertexNames.forEach { graph.addVertex(it) }

                // Соединяем первую вершину со всеми остальными
                val sourceVertexName = vertexNames[0]
                vertexNames.drop(1).forEach { graph.addEdge(sourceVertexName, it) }

                // Проверяем, что все ребра добавлены
                assertEquals(vertexNames.size - 1, graph.getEdgeCount())

                // Удаляем первую вершину
                graph.removeVertex(sourceVertexName)

                // Проверяем, что все связанные ребра удалены
                assertEquals(0, graph.getEdgeCount())

                // Проверяем, что у остальных вершин нет соседей
                vertexNames.drop(1).forEach {
                    assertTrue(
                        graph.getNeighbors(
                            graph.getVertexByName(it) ?: throw IllegalStateException("Вершина должна быть найдена"),
                        ).isEmpty(),
                    )
                }
            }

            @RepeatedTest(10)
            @DisplayName("Ребра в неориентированном графе симметричны")
            fun edgesInUndirectedGraphAreSymmetric() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1Name = generateRandomVertexName()
                var v2Name = generateRandomVertexName()

                // Убедимся, что вершины разные
                while (v1Name == v2Name) {
                    v2Name = generateRandomVertexName()
                }

                graph.addVertex(v1Name)
                graph.addVertex(v2Name)
                graph.addEdge(v1Name, v2Name)
                val vertex1 =
                    graph.getVertexByName(v1Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                val vertex2 =
                    graph.getVertexByName(v2Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")

                // Проверяем, что ребро существует в обоих направлениях
                assertTrue(graph.containsEdge(vertex1, vertex2))
                assertTrue(graph.containsEdge(vertex2, vertex1))

                // Проверяем, что у обеих вершин есть соответствующий сосед
                assertTrue(graph.getNeighbors(vertex1).map { it.name }.contains(v2Name))
                assertTrue(graph.getNeighbors(vertex2).map { it.name }.contains(v1Name))
            }
        }

        @Nested
        @DisplayName("Property-Based тесты для Ориентированного Невзвешенного Графа")
        inner class DirectedUnweightedGraphPropertyTest {
            @RepeatedTest(10)
            @DisplayName("Ребра в ориентированном графе направленные")
            fun edgesInDirectedGraphAreDirectional() {
                val graph = GraphFactory.createDirectedUnweightedGraph()
                val v1Name = generateRandomVertexName()
                var v2Name = generateRandomVertexName()

                // Убедимся, что вершины разные
                while (v1Name == v2Name) {
                    v2Name = generateRandomVertexName()
                }

                graph.addVertex(v1Name)
                graph.addVertex(v2Name)
                graph.addEdge(v1Name, v2Name)
                val vertex1 =
                    graph.getVertexByName(v1Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                val vertex2 =
                    graph.getVertexByName(v2Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")

                // Проверяем, что ребро существует только в одном направлении
                assertTrue(graph.containsEdge(vertex1, vertex2))
                assertFalse(graph.containsEdge(vertex2, vertex1))

                // Проверяем соседей
                assertTrue(graph.getNeighbors(vertex1).map { it.name }.contains(v2Name))
                assertFalse(graph.getNeighbors(vertex2).map { it.name }.contains(v1Name))
            }

            @RepeatedTest(5)
            @DisplayName("Количество ребер соответствует количеству соседей")
            fun edgeCountMatchesNeighborCount() {
                val graph = GraphFactory.createDirectedUnweightedGraph()
                val vertexNames = generateRandomVertexNames(Random.nextInt(3, 8))
                vertexNames.forEach { graph.addVertex(it) }

                // Создаем случайные ребра между вершинами
                val edgePairs = mutableSetOf<Pair<String, String>>()
                vertexNames.forEach { source ->
                    vertexNames.filter { it != source }.forEach { dest ->
                        if (Random.nextBoolean()) {
                            graph.addEdge(source, dest)
                            edgePairs.add(source to dest)
                        }
                    }
                }

                // Проверяем, что количество ребер равно количеству добавленных пар
                assertEquals(edgePairs.size, graph.getEdgeCount())

                // Проверяем, что количество соседей соответствует
                vertexNames.forEach { sourceName ->
                    val expectedNeighbors = edgePairs.filter { it.first == sourceName }.map { it.second }.toSet()
                    val actualNeighbors =
                        graph.getNeighbors(
                            graph.getVertexByName(sourceName)
                                ?: throw IllegalStateException("Вершина должна быть найдена"),
                        ).map {
                            it.name
                        }.toSet()
                    assertEquals(expectedNeighbors, actualNeighbors)
                }
            }
        }

        @Nested
        @DisplayName("Property-Based тесты для Взвешенных Графов")
        inner class WeightedGraphPropertyTest {
            @RepeatedTest(10)
            @DisplayName("Веса ребер в неориентированном графе одинаковы в обоих направлениях")
            fun edgeWeightsInUndirectedGraphAreSameInBothDirections() {
                val graph = GraphFactory.createUndirectedWeightedGraph()
                val v1Name = generateRandomVertexName()
                var v2Name = generateRandomVertexName()

                // Убедимся, что вершины разные
                while (v1Name == v2Name) {
                    v2Name = generateRandomVertexName()
                }

                val weight = generateRandomWeight()

                graph.addVertex(v1Name)
                graph.addVertex(v2Name)
                graph.addEdge(v1Name, v2Name, weight)
                val vertex1 =
                    graph.getVertexByName(v1Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                val vertex2 =
                    graph.getVertexByName(v2Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")

                // Проверяем, что вес ребра одинаков в обоих направлениях
                assertEquals(weight, graph.getEdgeWeight(vertex1, vertex2))
                assertEquals(weight, graph.getEdgeWeight(vertex2, vertex1))
            }

            @RepeatedTest(10)
            @DisplayName("Веса ребер в ориентированном графе могут быть разными")
            fun edgeWeightsInDirectedGraphCanBeDifferent() {
                val graph = GraphFactory.createDirectedWeightedGraph()
                val v1Name = generateRandomVertexName()
                var v2Name = generateRandomVertexName()

                // Убедимся, что вершины разные
                while (v1Name == v2Name) {
                    v2Name = generateRandomVertexName()
                }

                val weight1 = generateRandomWeight()
                val weight2 = generateRandomWeight()

                graph.addVertex(v1Name)
                graph.addVertex(v2Name)
                graph.addEdge(v1Name, v2Name, weight1)
                graph.addEdge(v2Name, v1Name, weight2)
                val vertex1 =
                    graph.getVertexByName(v1Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                val vertex2 =
                    graph.getVertexByName(v2Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")

                // Проверяем, что веса ребер сохранены правильно
                assertEquals(weight1, graph.getEdgeWeight(vertex1, vertex2))
                assertEquals(weight2, graph.getEdgeWeight(vertex2, vertex1))
            }
        }

        @Nested
        @DisplayName("Сложные свойства графов")
        inner class ComplexGraphPropertiesTest {
            @RepeatedTest(5)
            @DisplayName("Количество ребер не превышает N*(N-1)/2 для неориентированного и N*(N-1) для ориентированного")
            fun edgeCountDoesNotExceedMaximumPossible() {
                val vertexNames = generateRandomVertexNames(Random.nextInt(5, 10))
                val undirectedGraph = GraphFactory.createUndirectedUnweightedGraph()
                val directedGraph = GraphFactory.createDirectedUnweightedGraph()

                vertexNames.forEach {
                    undirectedGraph.addVertex(it)
                    directedGraph.addVertex(it)
                }

                // Добавляем случайные ребра между вершинами
                vertexNames.forEach { source ->
                    vertexNames.filter { it != source }.forEach { dest ->
                        if (Random.nextBoolean()) {
                            undirectedGraph.addEdge(source, dest)
                            directedGraph.addEdge(source, dest)
                        }
                    }
                }

                val n = vertexNames.size
                val maxUndirectedEdges = n * (n - 1) / 2
                val maxDirectedEdges = n * (n - 1)

                // Проверяем, что количество ребер не превышает максимально возможное
                assertTrue(undirectedGraph.getEdgeCount() <= maxUndirectedEdges)
                assertTrue(directedGraph.getEdgeCount() <= maxDirectedEdges)
            }

            @RepeatedTest(5)
            @DisplayName("Удаление всех вершин приводит к пустому графу")
            fun removingAllVerticesResultsInEmptyGraph() {
                val vertexNames = generateRandomVertexNames(Random.nextInt(2, 8))
                val graphs =
                    listOf(
                        GraphFactory.createUndirectedUnweightedGraph(),
                        GraphFactory.createDirectedUnweightedGraph(),
                        GraphFactory.createUndirectedWeightedGraph(),
                        GraphFactory.createDirectedWeightedGraph(),
                    )

                graphs.forEach { graph ->
                    // Добавляем вершины и ребра
                    vertexNames.forEach { graph.addVertex(it) }

                    for (i in vertexNames.indices) {
                        for (j in i + 1 until vertexNames.size) {
                            if (Random.nextBoolean()) {
                                if (graph.isWeighted()) {
                                    graph.addEdge(vertexNames[i], vertexNames[j], generateRandomWeight())
                                } else {
                                    graph.addEdge(vertexNames[i], vertexNames[j])
                                }
                            }
                        }
                    }

                    // Удаляем все вершины
                    vertexNames.forEach { graph.removeVertex(it) }

                    // Проверяем, что граф пуст
                    assertEquals(0, graph.getVertexCount())
                    assertEquals(0, graph.getEdgeCount())
                }
            }

            @RepeatedTest(5)
            @DisplayName("Итератор обходит все вершины графа ровно один раз")
            fun iteratorVisitsEachVertexExactlyOnce() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val vertexNames = generateRandomVertexNames(Random.nextInt(3, 8))

                vertexNames.forEach { graph.addVertex(it) }

                // Добавляем случайные ребра
                for (i in vertexNames.indices) {
                    for (j in i + 1 until vertexNames.size) {
                        if (Random.nextBoolean()) {
                            graph.addEdge(vertexNames[i], vertexNames[j])
                        }
                    }
                }

                // Используем итератор для обхода графа
                val visitedVertexNames = mutableSetOf<String>()
                val iterator = graph.iterator()

                while (iterator.hasNext()) {
                    val (vertex, _) = iterator.next()
                    visitedVertexNames.add(vertex.name) // Store name
                }

                // Проверяем, что все вершины были посещены ровно один раз
                assertEquals(vertexNames.toSet(), visitedVertexNames)
            }
        }

        @Nested
        @DisplayName("Дополнительные свойства графов")
        inner class AdditionalGraphPropertiesTest {
            @RepeatedTest(5)
            @DisplayName("В графе без циклов количество ребер не превышает N-1")
            fun graphWithoutCyclesHasAtMostNMinusOneEdges() {
                val vertexNames = generateRandomVertexNames(Random.nextInt(5, 10))
                val n = vertexNames.size

                val graph = GraphFactory.createUndirectedUnweightedGraph()
                vertexNames.forEach { graph.addVertex(it) }

                // Создаем дерево (граф без циклов)
                // Соединяем каждую вершину с одной случайной предыдущей
                for (i in 1 until n) {
                    val randomPrevIndex = Random.nextInt(0, i)
                    graph.addEdge(vertexNames[i], vertexNames[randomPrevIndex])
                }

                // Проверяем, что количество ребер равно N-1 (или меньше, если были дубликаты имен, но generateRandomVertexNames этого избегает)
                // Для дерева из n вершин всегда n-1 ребро
                if (n > 0) { // Avoid -1 for n=0 case, though test setup ensures n >= 5
                    assertEquals(n - 1, graph.getEdgeCount())
                } else {
                    assertEquals(0, graph.getEdgeCount())
                }
            }

            @RepeatedTest(10)
            @DisplayName("Ребра с одинаковыми конечными вершинами считаются одним ребром")
            fun duplicateEdgesAreCountedAsOne() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1Name = generateRandomVertexName()
                val v2Name = generateRandomVertexName()

                graph.addVertex(v1Name)
                graph.addVertex(v2Name)

                // Добавляем одно и то же ребро несколько раз
                graph.addEdge(v1Name, v2Name)
                assertEquals(1, graph.getEdgeCount())

                graph.addEdge(v1Name, v2Name)
                assertEquals(1, graph.getEdgeCount(), "Повторное добавление ребра не должно увеличивать счетчик")

                graph.addEdge(v2Name, v1Name)
                assertEquals(
                    1,
                    graph.getEdgeCount(),
                    "Добавление ребра в обратном направлении не должно увеличивать счетчик в неориентированном графе",
                )
            }

            @RepeatedTest(5)
            @DisplayName("Операции с неинициализированным графом не вызывают ошибок")
            fun operationsOnUninitializedGraphDoNotThrowErrors() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1Name = generateRandomVertexName()
                val v2Name = generateRandomVertexName()
                val tempV1 = Vertex(-1, v1Name) // For methods expecting Vertex objects
                val tempV2 = Vertex(-2, v2Name)

                // Операции с пустым графом
                assertFalse(graph.containsVertex(v1Name))
                assertFalse(graph.containsEdge(tempV1, tempV2))
                assertEquals(0, graph.getEdgeCount())
                assertEquals(0, graph.getVertexCount())
                assertTrue(graph.getVertices().isEmpty())
                assertTrue(graph.getEdges().isEmpty())
                assertTrue(graph.getNeighbors(tempV1).isEmpty())
            }

            @RepeatedTest(5)
            @DisplayName("Свойство транзитивности для связности графа (проверка через соседей)")
            fun transitivityPropertyForConnectivity() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1Name = generateRandomVertexName()
                val v2Name = generateRandomVertexName()
                val v3Name = generateRandomVertexName()

                graph.addVertex(v1Name)
                val vertex1 =
                    graph.getVertexByName(v1Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                graph.addVertex(v2Name)
                val vertex2 =
                    graph.getVertexByName(v2Name)
                        ?: throw IllegalStateException("Вершина должна быть найдена")
                graph.addVertex(v3Name)
                graph.getVertexByName(v3Name)
                    ?: throw IllegalStateException("Вершина должна быть найдена")

                // Создаем ребра v1 -- v2 и v2 -- v3
                graph.addEdge(v1Name, v2Name)
                graph.addEdge(v2Name, v3Name)

                // Проверяем транзитивность через соседей
                val neighborsOfV1 = graph.getNeighbors(vertex1)
                val neighborsOfV2 = graph.getNeighbors(vertex2)

                assertTrue(neighborsOfV1.map { it.name }.contains(v2Name))
                assertTrue(neighborsOfV2.map { it.name }.contains(v3Name))

                // v3 не должна быть прямым соседом v1 (если только v1=v3, что маловероятно)
                if (v1Name != v3Name) {
                    assertFalse(neighborsOfV1.map { it.name }.contains(v3Name))
                }
            }
        }
    }
}
