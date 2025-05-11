package model.graph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphImplTestGroup {

    private val vA = Vertex(1, "A")
    private val vB = Vertex(2, "B")
    private val vC = Vertex(3, "C")
    private val vD = Vertex(4, "D")

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
            assertTrue(vertices.contains(vA))
            assertTrue(vertices.contains(vB))
        }

        @Test
        @DisplayName("Добавление ребра (неориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertEquals(0, graph.getEdgeCount(), "Изначально ребер быть не должно")

            graph.addEdge(vA, vB)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A - B")
            assertTrue(graph.containsEdge(vB, vA), "Граф должен содержать ребро B - A (т.к. неориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1 (не удваивается)")

            assertTrue(graph.getNeighbors(vA).contains(vB), "B должна быть соседом A")
            assertTrue(graph.getNeighbors(vB).contains(vA), "A должна быть соседом B")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertFalse(graph.containsEdge(vA, vB))
            assertFalse(graph.containsEdge(vB, vA))
            assertEquals(0, graph.getVertexCount())
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление ребра (неориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB)
            assertEquals(1, graph.getEdgeCount())

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertFalse(graph.getNeighbors(vA).contains(vB), "B не должна быть соседом A")
            assertFalse(graph.getNeighbors(vB).contains(vA), "A не должна быть соседом B")

            graph.addEdge(vA, vB)
            assertEquals(1, graph.getEdgeCount())
            graph.removeEdge(vB, vA)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено после удаления B-A")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
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

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
            assertFalse(graph.containsEdge(vB, vC), "Ребро B - C должно быть удалено")
            assertFalse(graph.containsEdge(vC, vB), "Ребро C - B должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")

            assertTrue(graph.getNeighbors(vA).isEmpty(), "У A не должно быть соседей после удаления B")
            assertTrue(graph.getNeighbors(vC).isEmpty(), "У C не должно быть соседей после удаления B")
        }

        @Test
        @DisplayName("Получение соседей (неориентированный)")
        fun getNeighbors() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vA, vC)

            val neighborsA = graph.getNeighbors(vA)
            assertEquals(2, neighborsA.size)
            assertTrue(neighborsA.containsAll(listOf(vB, vC)), "Соседи A должны быть B и C")

            val neighborsB = graph.getNeighbors(vB)
            assertEquals(1, neighborsB.size)
            assertTrue(neighborsB.contains(vA), "Сосед B должен быть A")

            val neighborsC = graph.getNeighbors(vC)
            assertEquals(1, neighborsC.size)
            assertTrue(neighborsC.contains(vA), "Сосед C должен быть A")
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

            assertTrue(edges.any { it.source == vA && it.destination == vB || it.source == vB && it.destination == vA })
            assertTrue(edges.any { it.source == vB && it.destination == vC || it.source == vC && it.destination == vB })
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

            graph.addEdge(vA, vB)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vB, vA), "Граф не должен содержать ребро B -> A (т.к. ориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")

            assertTrue(graph.getNeighbors(vA).contains(vB), "B должна быть соседом A")
            assertFalse(graph.getNeighbors(vB).contains(vA), "A не должна быть соседом B")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertFalse(graph.containsEdge(vA, vB))
            assertEquals(0, graph.getVertexCount())
            assertEquals(0, graph.getEdgeCount())
        }

        @Test
        @DisplayName("Удаление ребра")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB)
            assertEquals(1, graph.getEdgeCount())
            assertTrue(graph.containsEdge(vA, vB))

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertFalse(graph.getNeighbors(vA).contains(vB), "B не должна быть соседом A после удаления ребра")
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

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertFalse(graph.containsEdge(vC, vB), "Ребро C -> B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B -> A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")

            assertTrue(graph.getNeighbors(vA).isEmpty(), "У A не должно быть исходящих соседей после удаления B")
            assertTrue(graph.getNeighbors(vC).isEmpty(), "У C не должно быть исходящих соседей после удаления B")
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

            val neighborsA = graph.getNeighbors(vA)
            assertEquals(2, neighborsA.size)
            assertTrue(neighborsA.containsAll(listOf(vB, vC)), "Соседи A должны быть B и C")

            val neighborsB = graph.getNeighbors(vB)
            assertEquals(1, neighborsB.size)
            assertTrue(neighborsB.contains(vC), "Сосед B должен быть C")

            assertTrue(graph.getNeighbors(vC).isEmpty(), "У C не должно быть исходящих соседей")
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

            assertTrue(edges.any { it.source == vA && it.destination == vB })
            assertTrue(edges.any { it.source == vB && it.destination == vC })
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

            graph.addEdge(vA, vB, 5.0)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A - B")
            assertTrue(graph.containsEdge(vB, vA), "Граф должен содержать ребро B - A (т.к. неориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1 (не удваивается)")

            assertEquals(5.0, graph.getEdgeWeight(vA, vB), "Вес ребра A - B должен быть 5.0")
            assertEquals(5.0, graph.getEdgeWeight(vB, vA), "Вес ребра B - A должен быть 5.0")
        }

        @Test
        @DisplayName("Перезапись веса ребра")
        fun rewriteWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)

            graph.addEdge(vA, vB, 10.0)
            assertEquals(10.0, graph.getEdgeWeight(vA, vB))
            assertEquals(10.0, graph.getEdgeWeight(vB, vA))

            graph.addEdge(vA, vB, 11.0)
            assertEquals(11.0, graph.getEdgeWeight(vA, vB))
            assertEquals(11.0, graph.getEdgeWeight(vB, vA))
        }

        @Test
        @DisplayName("Удаление взвешенного ребра (неориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, 10.0)

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
            assertNull(graph.getEdgeWeight(vA, vB), "Вес ребра A - B должен быть null")
            assertNull(graph.getEdgeWeight(vB, vA), "Вес ребра B - A должен быть null")
        }

        @Test
        @DisplayName("Добавление ребра без указания веса")
        fun addEdgeWithoutWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)

            graph.addEdge(vA, vB)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A - B")
            assertNull(graph.getEdgeWeight(vA, vB), "Вес ребра A - B должен быть null")
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

            graph.addEdge(vA, vB, 7.5)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vB, vA), "Граф не должен содержать ребро B -> A")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")

            assertEquals(7.5, graph.getEdgeWeight(vA, vB), "Вес ребра A -> B должен быть 7.5")
            assertNull(graph.getEdgeWeight(vB, vA), "Вес ребра B -> A должен быть null")
        }

        @Test
        @DisplayName("Перезапись веса ребра")
        fun rewriteWeight() {
            graph.addVertex(vA)
            graph.addVertex(vB)

            graph.addEdge(vA, vB, 10.0)
            assertEquals(10.0, graph.getEdgeWeight(vA, vB))

            graph.addEdge(vA, vB, 11.0)
            assertEquals(11.0, graph.getEdgeWeight(vA, vB))
        }

        @Test
        @DisplayName("Удаление взвешенного ребра (ориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, 3.0)

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertNull(graph.getEdgeWeight(vA, vB), "Вес ребра A -> B должен быть null")
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

            assertTrue(edges.any { it.source == vA && it.destination == vB && it.weight == 1.0 })
            assertTrue(edges.any { it.source == vB && it.destination == vC && it.weight == 2.0 })
            assertTrue(edges.any { it.source == vC && it.destination == vD && it.weight == 3.0 })
            assertTrue(edges.any { it.source == vD && it.destination == vA && it.weight == 4.0 })
            assertTrue(edges.any { it.source == vA && it.destination == vC && it.weight == 5.0 })
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

            val iterator = graph.iterator()
            var count = 0

            while (iterator.hasNext()) {
                val (vertex, edges) = iterator.next()
                count++

                when (vertex) {
                    vA -> {
                        assertEquals(1, edges.size)
                        assertEquals(vB, edges.first().destination)
                    }

                    vB -> {
                        assertEquals(2, edges.size)
                        assertTrue(edges.any { it.destination == vA })
                        assertTrue(edges.any { it.destination == vC })
                    }

                    vC -> {
                        assertEquals(1, edges.size)
                        assertEquals(vB, edges.first().destination)
                    }

                    else -> fail("Неизвестная вершина: $vertex")
                }
            }

            assertEquals(3, count, "Итератор должен пройти по 3 вершинам")
        }
    }

    @Nested
    inner class GraphPropertyBasedTestGroup {

        // Генератор случайных вершин с уникальными идентификаторами
        private fun generateRandomVertex(): Vertex {
            val id = Random.nextInt(1, 1000)
            val name = ('A'..'Z').random().toString()
            return Vertex(id, name)
        }

        // Генератор случайного списка вершин
        private fun generateRandomVertices(count: Int): List<Vertex> {
            val vertices = mutableListOf<Vertex>()
            repeat(count) {
                vertices.add(generateRandomVertex())
            }
            return vertices
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
                val vertices = generateRandomVertices(Random.nextInt(5, 20))

                // Добавляем все вершины из списка
                vertices.forEach { graph.addVertex(it) }

                // Проверяем, что все вершины добавлены
                vertices.forEach { assertTrue(graph.containsVertex(it)) }

                // Проверяем, что количество вершин равно количеству уникальных вершин
                assertEquals(vertices.distinct().size, graph.getVertexCount())
            }

            @RepeatedTest(10)
            @DisplayName("Удаление вершины удаляет все связанные ребра")
            fun removingVertexRemovesAllConnectedEdges() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val vertices = generateRandomVertices(Random.nextInt(3, 10))

                // Добавляем все вершины
                vertices.forEach { graph.addVertex(it) }

                // Соединяем первую вершину со всеми остальными
                val sourceVertex = vertices[0]
                vertices.drop(1).forEach { graph.addEdge(sourceVertex, it) }

                // Проверяем, что все ребра добавлены
                assertEquals(vertices.size - 1, graph.getEdgeCount())

                // Удаляем первую вершину
                graph.removeVertex(sourceVertex)

                // Проверяем, что все связанные ребра удалены
                assertEquals(0, graph.getEdgeCount())

                // Проверяем, что у остальных вершин нет соседей
                vertices.drop(1).forEach {
                    assertTrue(graph.getNeighbors(it).isEmpty())
                }
            }

            @RepeatedTest(10)
            @DisplayName("Ребра в неориентированном графе симметричны")
            fun edgesInUndirectedGraphAreSymmetric() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1 = generateRandomVertex()
                var v2 = generateRandomVertex()

                // Убедимся, что вершины разные
                while (v1.id == v2.id) {
                    v2 = generateRandomVertex()
                }

                graph.addVertex(v1)
                graph.addVertex(v2)
                graph.addEdge(v1, v2)

                // Проверяем, что ребро существует в обоих направлениях
                assertTrue(graph.containsEdge(v1, v2))
                assertTrue(graph.containsEdge(v2, v1))

                // Проверяем, что у обеих вершин есть соответствующий сосед
                assertTrue(graph.getNeighbors(v1).contains(v2))
                assertTrue(graph.getNeighbors(v2).contains(v1))
            }
        }

        @Nested
        @DisplayName("Property-Based тесты для Ориентированного Невзвешенного Графа")
        inner class DirectedUnweightedGraphPropertyTest {

            @RepeatedTest(10)
            @DisplayName("Ребра в ориентированном графе направленные")
            fun edgesInDirectedGraphAreDirectional() {
                val graph = GraphFactory.createDirectedUnweightedGraph()
                val v1 = generateRandomVertex()
                var v2 = generateRandomVertex()

                // Убедимся, что вершины разные
                while (v1.id == v2.id) {
                    v2 = generateRandomVertex()
                }

                graph.addVertex(v1)
                graph.addVertex(v2)
                graph.addEdge(v1, v2)

                // Проверяем, что ребро существует только в одном направлении
                assertTrue(graph.containsEdge(v1, v2))
                assertFalse(graph.containsEdge(v2, v1))

                // Проверяем соседей
                assertTrue(graph.getNeighbors(v1).contains(v2))
                assertFalse(graph.getNeighbors(v2).contains(v1))
            }

            @RepeatedTest(5)
            @DisplayName("Количество ребер соответствует количеству соседей")
            fun edgeCountMatchesNeighborCount() {
                val graph = GraphFactory.createDirectedUnweightedGraph()
                val vertices = generateRandomVertices(Random.nextInt(3, 8))
                vertices.forEach { graph.addVertex(it) }

                // Создаем случайные ребра между вершинами
                val edgePairs = mutableSetOf<Pair<Vertex, Vertex>>()
                vertices.forEach { source ->
                    vertices.filter { it != source }.forEach { dest ->
                        if (Random.nextBoolean()) {
                            graph.addEdge(source, dest)
                            edgePairs.add(source to dest)
                        }
                    }
                }

                // Проверяем, что количество ребер равно количеству добавленных пар
                assertEquals(edgePairs.size, graph.getEdgeCount())

                // Проверяем, что количество соседей соответствует
                vertices.forEach { source ->
                    val expectedNeighbors = edgePairs.filter { it.first == source }.map { it.second }.toSet()
                    assertEquals(expectedNeighbors, graph.getNeighbors(source).toSet())
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
                val v1 = generateRandomVertex()
                var v2 = generateRandomVertex()

                // Убедимся, что вершины разные
                while (v1.id == v2.id) {
                    v2 = generateRandomVertex()
                }

                val weight = generateRandomWeight()

                graph.addVertex(v1)
                graph.addVertex(v2)
                graph.addEdge(v1, v2, weight)

                // Проверяем, что вес ребра одинаков в обоих направлениях
                assertEquals(weight, graph.getEdgeWeight(v1, v2))
                assertEquals(weight, graph.getEdgeWeight(v2, v1))
            }

            @RepeatedTest(10)
            @DisplayName("Веса ребер в ориентированном графе могут быть разными")
            fun edgeWeightsInDirectedGraphCanBeDifferent() {
                val graph = GraphFactory.createDirectedWeightedGraph()
                val v1 = generateRandomVertex()
                var v2 = generateRandomVertex()

                // Убедимся, что вершины разные
                while (v1.id == v2.id) {
                    v2 = generateRandomVertex()
                }

                val weight1 = generateRandomWeight()
                val weight2 = generateRandomWeight()

                graph.addVertex(v1)
                graph.addVertex(v2)
                graph.addEdge(v1, v2, weight1)
                graph.addEdge(v2, v1, weight2)

                // Проверяем, что веса ребер сохранены правильно
                assertEquals(weight1, graph.getEdgeWeight(v1, v2))
                assertEquals(weight2, graph.getEdgeWeight(v2, v1))
            }
        }

        @Nested
        @DisplayName("Сложные свойства графов")
        inner class ComplexGraphPropertiesTest {

            @RepeatedTest(5)
            @DisplayName("Количество ребер не превышает N*(N-1)/2 для неориентированного и N*(N-1) для ориентированного")
            fun edgeCountDoesNotExceedMaximumPossible() {
                val vertices = generateRandomVertices(Random.nextInt(5, 10))
                val undirectedGraph = GraphFactory.createUndirectedUnweightedGraph()
                val directedGraph = GraphFactory.createDirectedUnweightedGraph()

                vertices.forEach {
                    undirectedGraph.addVertex(it)
                    directedGraph.addVertex(it)
                }

                // Добавляем случайные ребра между вершинами
                vertices.forEach { source ->
                    vertices.filter { it != source }.forEach { dest ->
                        if (Random.nextBoolean()) {
                            undirectedGraph.addEdge(source, dest)
                            directedGraph.addEdge(source, dest)
                        }
                    }
                }

                val n = vertices.size
                val maxUndirectedEdges = n * (n - 1) / 2
                val maxDirectedEdges = n * (n - 1)

                // Проверяем, что количество ребер не превышает максимально возможное
                assertTrue(undirectedGraph.getEdgeCount() <= maxUndirectedEdges)
                assertTrue(directedGraph.getEdgeCount() <= maxDirectedEdges)
            }

            @RepeatedTest(5)
            @DisplayName("Удаление всех вершин приводит к пустому графу")
            fun removingAllVerticesResultsInEmptyGraph() {
                val vertices = generateRandomVertices(Random.nextInt(2, 8))
                val graphs = listOf(
                        GraphFactory.createUndirectedUnweightedGraph(),
                        GraphFactory.createDirectedUnweightedGraph(),
                        GraphFactory.createUndirectedWeightedGraph(),
                        GraphFactory.createDirectedWeightedGraph()
                )

                graphs.forEach { graph ->
                    // Добавляем вершины и ребра
                    vertices.forEach { graph.addVertex(it) }

                    for (i in vertices.indices) {
                        for (j in i + 1 until vertices.size) {
                            if (Random.nextBoolean()) {
                                if (graph.isWeighted()) {
                                    graph.addEdge(vertices[i], vertices[j], generateRandomWeight())
                                } else {
                                    graph.addEdge(vertices[i], vertices[j])
                                }
                            }
                        }
                    }

                    // Удаляем все вершины
                    vertices.forEach { graph.removeVertex(it) }

                    // Проверяем, что граф пуст
                    assertEquals(0, graph.getVertexCount())
                    assertEquals(0, graph.getEdgeCount())
                }
            }

            @RepeatedTest(5)
            @DisplayName("Итератор обходит все вершины графа ровно один раз")
            fun iteratorVisitsEachVertexExactlyOnce() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val vertices = generateRandomVertices(Random.nextInt(3, 8))

                vertices.forEach { graph.addVertex(it) }

                // Добавляем случайные ребра
                for (i in vertices.indices) {
                    for (j in i + 1 until vertices.size) {
                        if (Random.nextBoolean()) {
                            graph.addEdge(vertices[i], vertices[j])
                        }
                    }
                }

                // Используем итератор для обхода графа
                val visitedVertices = mutableSetOf<Vertex>()
                val iterator = graph.iterator()

                while (iterator.hasNext()) {
                    val (vertex, _) = iterator.next()
                    visitedVertices.add(vertex)
                }

                // Проверяем, что все вершины были посещены ровно один раз
                assertEquals(vertices.toSet(), visitedVertices)
            }
        }

        @Nested
        @DisplayName("Дополнительные свойства графов")
        inner class AdditionalGraphPropertiesTest {

            @RepeatedTest(5)
            @DisplayName("В графе без циклов количество ребер не превышает N-1")
            fun graphWithoutCyclesHasAtMostNMinusOneEdges() {
                val vertices = generateRandomVertices(Random.nextInt(5, 10))
                val n = vertices.size

                val graph = GraphFactory.createUndirectedUnweightedGraph()
                vertices.forEach { graph.addVertex(it) }

                // Создаем дерево (граф без циклов)
                // Соединяем каждую вершину с одной случайной предыдущей
                for (i in 1 until n) {
                    val randomPrevIndex = Random.nextInt(0, i)
                    graph.addEdge(vertices[i], vertices[randomPrevIndex])
                }

                // Проверяем, что количество ребер равно N-1
                assertEquals(n - 1, graph.getEdgeCount())
            }

            @RepeatedTest(10)
            @DisplayName("Ребра с одинаковыми конечными вершинами считаются одним ребром")
            fun duplicateEdgesAreCountedAsOne() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1 = generateRandomVertex()
                val v2 = generateRandomVertex()

                graph.addVertex(v1)
                graph.addVertex(v2)

                // Добавляем одно и то же ребро несколько раз
                graph.addEdge(v1, v2)
                assertEquals(1, graph.getEdgeCount())

                graph.addEdge(v1, v2)
                assertEquals(1, graph.getEdgeCount(), "Повторное добавление ребра не должно увеличивать счетчик")

                graph.addEdge(v2, v1)
                assertEquals(1, graph.getEdgeCount(), "Добавление ребра в обратном направлении не должно увеличивать счетчик в неориентированном графе")
            }

            @RepeatedTest(5)
            @DisplayName("Операции с неинициализированным графом не вызывают ошибок")
            fun operationsOnUninitializedGraphDoNotThrowErrors() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1 = generateRandomVertex()
                val v2 = generateRandomVertex()

                // Операции с пустым графом
                assertFalse(graph.containsVertex(v1))
                assertFalse(graph.containsEdge(v1, v2))
                assertEquals(0, graph.getEdgeCount())
                assertEquals(0, graph.getVertexCount())
                assertTrue(graph.getVertices().isEmpty())
                assertTrue(graph.getEdges().isEmpty())
                assertTrue(graph.getNeighbors(v1).isEmpty())
            }

            @RepeatedTest(5)
            @DisplayName("Свойство транзитивности для связности графа")
            fun transitivityPropertyForConnectivity() {
                val graph = GraphFactory.createUndirectedUnweightedGraph()
                val v1 = generateRandomVertex()
                val v2 = generateRandomVertex()
                val v3 = generateRandomVertex()

                graph.addVertex(v1)
                graph.addVertex(v2)
                graph.addVertex(v3)

                // Создаем ребра v1 -- v2 и v2 -- v3
                graph.addEdge(v1, v2)
                graph.addEdge(v2, v3)

                // Проверяем транзитивность через соседей
                val neighborsOfV1 = graph.getNeighbors(v1)
                val neighborsOfV2 = graph.getNeighbors(v2)

                assertTrue(neighborsOfV1.contains(v2))
                assertTrue(neighborsOfV2.contains(v3))

                // v3 не должна быть прямым соседом v1
                assertFalse(neighborsOfV1.contains(v3))
            }
        }
    }
}