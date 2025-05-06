package model.graph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
            val graph = GraphImpl(isDirected = false, isWeighted = false)
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
                        assertEquals(vB, edges[0].destination)
                    }
                    vB -> {
                        assertEquals(2, edges.size)
                        assertTrue(edges.any { it.destination == vA })
                        assertTrue(edges.any { it.destination == vC })
                    }
                    vC -> {
                        assertEquals(1, edges.size)
                        assertEquals(vB, edges[0].destination)
                    }
                    else -> fail("Неизвестная вершина: $vertex")
                }
            }

            assertEquals(3, count, "Итератор должен пройти по 3 вершинам")
        }
    }
}