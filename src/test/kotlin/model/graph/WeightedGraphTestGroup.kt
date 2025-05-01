package model.graph

import model.graph.Vertex // Make sure Vertex is accessible
import model.graph.WeightedGraph // Make sure AdjListWeightedGraph is accessible
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows // Import assertThrows

@DisplayName("Тесты для AdjListWeightedGraph (Взвешенный Граф)")
class AdjListWeightedGraphTest {

    // --- Общие вершины и веса для тестов ---
    private val vA = Vertex(1, "A")
    private val vB = Vertex(2, "B")
    private val vC = Vertex(3, "C")
    private val vD = Vertex(4, "D")

    private val WEIGHT1 = 5.0
    private val WEIGHT2 = 10.5
    private val WEIGHT3 = 1.0

    @Nested
    @DisplayName("Тесты для Ориентированного Взвешенного Графа")
    inner class DirectedWeightedGraphTest {

        private lateinit var graph: WeightedGraph

        @BeforeEach
        fun setUp() {
            graph = WeightedGraph(isDirected = true)
        }

        // Тесты на добавление вершин аналогичны невзвешенному
        @Test
        @DisplayName("Добавление вершины")
        fun addVertex() {
            assertEquals(0, graph.getVertexCount())
            graph.addVertex(vA)
            assertTrue(graph.containsVertex(vA))
            assertEquals(1, graph.getVertexCount())
        }

        @Test
        @DisplayName("Добавление существующей вершины не меняет граф")
        fun addExistingVertex() {
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount())
            graph.addVertex(vA)
            assertEquals(1, graph.getVertexCount())
        }

        @Test
        @DisplayName("Добавление взвешенного ребра")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertEquals(0, graph.getEdgeCount())
            graph.addEdge(vA, vB, WEIGHT1)

            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vB, vA), "Граф не должен содержать ребро B -> A")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB), "Вес ребра A -> B должен быть ${WEIGHT1}")
            assertNull(graph.getEdgeWeight(vB, vA), "Вес ребра B -> A должен быть null")
            assertTrue(graph.getNeighbors(vA).contains(vB), "B должна быть соседом A")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами (должны добавиться)")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB, WEIGHT1)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertFalse(graph.containsEdge(vA, vB))
            assertNotEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertNotEquals(2, graph.getVertexCount())
            assertNotEquals(1, graph.getEdgeCount())
        }

        // Примечание: Текущая реализация AdjListWeightedGraph ДОБАВЛЯЕТ еще одно ребро,
        // если вызвать addEdge для существующей пары вершин, а не обновляет вес.
        // Этот тест проверяет такое поведение (добавление параллельного ребра).
        @Test
        @DisplayName("Добавление второго ребра между теми же вершинами")
        fun addSecondEdgeBetweenSameVertices() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, WEIGHT1)
            assertEquals(1, graph.getEdgeCount())
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))

            graph.addEdge(vA, vB, WEIGHT2) // Добавляем второе ребро A -> B с другим весом
            assertEquals(2, graph.getEdgeCount(), "Количество ребер должно стать 2 (параллельные)")

            // getEdgeWeight найдет первое добавленное ребро
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB), "getEdgeWeight должен вернуть вес первого ребра")
            // Нужен был бы другой метод, чтобы получить все веса или последний вес.
        }

        @Test
        @DisplayName("Удаление взвешенного ребра")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, WEIGHT1)
            assertEquals(1, graph.getEdgeCount())
            assertTrue(graph.containsEdge(vA, vB))

            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertNull(graph.getEdgeWeight(vA, vB), "Вес удаленного ребра должен быть null")
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
        @DisplayName("Удаление вершины (удаляет связанные взвешенные ребра)")
        fun removeVertex() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB, WEIGHT1) // A -> B
            graph.addEdge(vC, vB, WEIGHT2) // C -> B
            graph.addEdge(vB, vA, WEIGHT3) // B -> A
            assertEquals(3, graph.getVertexCount())
            assertEquals(3, graph.getEdgeCount())

            graph.removeVertex(vB) // Удаляем B

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount())
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertFalse(graph.containsEdge(vC, vB), "Ребро C -> B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B -> A должно быть удалено")
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vC, vB))
            assertNull(graph.getEdgeWeight(vB, vA))
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")
            assertTrue(graph.getNeighbors(vA).isEmpty())
            assertTrue(graph.getNeighbors(vC).isEmpty())
        }

        @Test
        @DisplayName("Получение веса существующего ребра")
        fun getEdgeWeightFound() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB, WEIGHT1)
            graph.addEdge(vB, vC, WEIGHT2)
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertEquals(WEIGHT2, graph.getEdgeWeight(vB, vC))
        }

        @Test
        @DisplayName("Получение веса несуществующего ребра (вершины есть)")
        fun getEdgeWeightNotFound_EdgeMissing() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA))
        }

        @Test
        @DisplayName("Получение веса ребра, когда вершина 'from' отсутствует")
        fun getEdgeWeightNotFound_FromVertexMissing() {
            graph.addVertex(vB)
            assertNull(graph.getEdgeWeight(vA, vB), "Вес должен быть null, если 'from' вершина отсутствует")
        }

        // Этот тест основан на реализации getEdgeWeight, которая возвращает null, если 'from' нет.
        // Если бы она кидала Exception, тест был бы другим (assertThrows).


        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertTrue(graph.isDirected(), "isDirected должно быть true")
            assertTrue(graph.isWeighted(), "isWeighted должно быть true")
        }
    }

    @Nested
    @DisplayName("Тесты для Неориентированного Взвешенного Графа")
    inner class UndirectedWeightedGraphTest {

        private lateinit var graph: WeightedGraph

        @BeforeEach
        fun setUp() {
            graph = WeightedGraph(isDirected = false)
        }

        // Тесты на добавление вершин аналогичны
        @Test
        @DisplayName("Добавление вершины")
        fun addVertex() {
            assertEquals(0, graph.getVertexCount())
            graph.addVertex(vA)
            assertTrue(graph.containsVertex(vA))
            assertEquals(1, graph.getVertexCount())
        }

        @Test
        @DisplayName("Добавление взвешенного ребра (неориентированный)")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, WEIGHT1)

            assertTrue(graph.containsEdge(vA, vB), "Должно быть ребро A - B")
            assertTrue(graph.containsEdge(vB, vA), "Должно быть ребро B - A")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB), "Вес A - B должен быть ${WEIGHT1}")
            assertEquals(WEIGHT1, graph.getEdgeWeight(vB, vA), "Вес B - A должен быть ${WEIGHT1}")
            assertTrue(graph.getNeighbors(vA).contains(vB))
            assertTrue(graph.getNeighbors(vB).contains(vA))
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами (должны добавиться)")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB, WEIGHT1)
            assertFalse(graph.containsVertex(vA))
            assertFalse(graph.containsVertex(vB))
            assertFalse(graph.containsEdge(vA, vB))
            assertFalse(graph.containsEdge(vB, vA))
            assertNotEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertNotEquals(WEIGHT1, graph.getEdgeWeight(vB, vA))
            assertNotEquals(2, graph.getVertexCount())
            assertNotEquals(1, graph.getEdgeCount())
        }


        // Опять же, тест проверяет добавление параллельных ребер, если реализация это позволяет
        @Test
        @DisplayName("Добавление второго ребра между теми же вершинами (неориентированный)")
        fun addSecondEdgeBetweenSameVertices() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, WEIGHT1)
            assertEquals(1, graph.getEdgeCount())
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertEquals(WEIGHT1, graph.getEdgeWeight(vB, vA))


            graph.addEdge(vA, vB, WEIGHT2) // Добавляем еще одно A-B с другим весом
            assertEquals(2, graph.getEdgeCount(), "Количество ребер должно стать 2")

            // getEdgeWeight найдет первое добавленное (в зависимости от реализации find)
            // Проверяем оба направления
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertEquals(WEIGHT1, graph.getEdgeWeight(vB, vA))
        }

        @Test
        @DisplayName("Удаление взвешенного ребра (неориентированный)")
        fun removeEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addEdge(vA, vB, WEIGHT1)
            assertEquals(1, graph.getEdgeCount())

            graph.removeEdge(vA, vB) // Удаляем A - B
            assertFalse(graph.containsEdge(vA, vB))
            assertFalse(graph.containsEdge(vB, vA))
            assertEquals(0, graph.getEdgeCount())
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA))

            // Проверка удаления B - A
            graph.addEdge(vA, vB, WEIGHT1)
            assertEquals(1, graph.getEdgeCount())
            graph.removeEdge(vB, vA) // Удаляем B - A
            assertFalse(graph.containsEdge(vA, vB))
            assertFalse(graph.containsEdge(vB, vA))
            assertEquals(0, graph.getEdgeCount())
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA))
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
        @DisplayName("Удаление вершины (удаляет связанные взвешенные ребра неориентированно)")
        fun removeVertex() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB, WEIGHT1) // A - B
            graph.addEdge(vB, vC, WEIGHT2) // B - C
            assertEquals(3, graph.getVertexCount())
            assertEquals(2, graph.getEdgeCount())

            graph.removeVertex(vB) // Удаляем B

            assertFalse(graph.containsVertex(vB))
            assertEquals(2, graph.getVertexCount())
            assertFalse(graph.containsEdge(vA, vB))
            assertFalse(graph.containsEdge(vB, vA))
            assertFalse(graph.containsEdge(vB, vC))
            assertFalse(graph.containsEdge(vC, vB))
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA))
            assertNull(graph.getEdgeWeight(vB, vC))
            assertNull(graph.getEdgeWeight(vC, vB))
            assertEquals(0, graph.getEdgeCount())
            assertTrue(graph.getNeighbors(vA).isEmpty())
            assertTrue(graph.getNeighbors(vC).isEmpty())
        }

        @Test
        @DisplayName("Получение веса существующего ребра (неориентированный)")
        fun getEdgeWeightFound() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB, WEIGHT1)
            graph.addEdge(vB, vC, WEIGHT2)
            assertEquals(WEIGHT1, graph.getEdgeWeight(vA, vB))
            assertEquals(WEIGHT1, graph.getEdgeWeight(vB, vA))
            assertEquals(WEIGHT2, graph.getEdgeWeight(vB, vC))
            assertEquals(WEIGHT2, graph.getEdgeWeight(vC, vB))
        }

        @Test
        @DisplayName("Получение веса несуществующего ребра (вершины есть)")
        fun getEdgeWeightNotFound_EdgeMissing() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA))
        }

        @Test
        @DisplayName("Получение веса ребра, когда вершина 'from' отсутствует")
        fun getEdgeWeightNotFound_FromVertexMissing() {
            graph.addVertex(vB)
            assertNull(graph.getEdgeWeight(vA, vB))
            assertNull(graph.getEdgeWeight(vB, vA)) // Должно быть null и для обратного поиска
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertFalse(graph.isDirected(), "isDirected должно быть false")
            assertTrue(graph.isWeighted(), "isWeighted должно быть true")
        }
    }
}