package model.graph

import model.graph.Vertex // Make sure Vertex is accessible
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Тесты для AdjListUnweightedGraph (Невзвешенный Граф)")
class UnweightedGraphTest {

    // Общие вершины для тестов
    private val vA = Vertex(1, "A")
    private val vB = Vertex(2, "B")
    private val vC = Vertex(3, "C")
    private val vD = Vertex(4, "D")

    @Nested
    @DisplayName("Тесты для Ориентированного Невзвешенного Графа")
    inner class DirectedUnweightedGraphTest {

        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            graph = GraphFactory.createDirectedUnweightedGraph()
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
            graph.addVertex(vA) // Повторное добавление
            assertEquals(1, graph.getVertexCount(), "Количество вершин не должно измениться при повторном добавлении")
        }

        @Test
        @DisplayName("Добавление ребра")
        fun addEdge() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            assertEquals(0, graph.getEdgeCount(), "Изначально ребер быть не должно")
            graph.addEdge(vA, vB)
            assertTrue(graph.containsEdge(vA, vB), "Граф должен содержать ребро A -> B")
            assertFalse(graph.containsEdge(vB, vA), "Граф не должен содержать ребро B -> A (т.к. ориентированный)")
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1")
            assertTrue(graph.getNeighbors(vA).contains(vB), "B должна быть соседом A")
            assertTrue(graph.getNeighbors(vB).isEmpty(), "У B не должно быть исходящих соседей")
        }

        @Test
        @DisplayName("Добавление ребра с несуществующими вершинами")
        fun addEdgeWithMissingVertices() {
            graph.addEdge(vA, vB)
            assertFalse(graph.containsVertex(vA), "Вершина A должна быть добавлена явно")
            assertFalse(graph.containsVertex(vB), "Вершина B должна быть добавлена явно")
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B не может существовать")
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
            assertEquals(0, graph.getEdgeCount())
            assertDoesNotThrow {
                graph.removeEdge(vA, vB) // Ребра нет
            }
            assertEquals(0, graph.getEdgeCount())
            assertDoesNotThrow {
                graph.removeEdge(vC, vD) // Вершин нет
            }
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
            graph.addEdge(vB, vA) // B -> A (петля)
            assertEquals(3, graph.getVertexCount())
            assertEquals(3, graph.getEdgeCount())

            graph.removeVertex(vB) // Удаляем B

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertFalse(graph.containsEdge(vA, vB), "Ребро A -> B должно быть удалено")
            assertFalse(graph.containsEdge(vC, vB), "Ребро C -> B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B -> A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены") // Если нет других ребер

            // Проверяем соседей оставшихся вершин
            assertTrue(graph.getNeighbors(vA).isEmpty(), "У A не должно быть исходящих соседей после удаления B")
            assertTrue(graph.getNeighbors(vC).isEmpty(), "У C не должно быть исходящих соседей после удаления B")
        }

        @Test
        @DisplayName("Получение соседей")
        fun getNeighbors() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB)
            graph.addEdge(vA, vC)

            val neighborsA = graph.getNeighbors(vA)
            assertEquals(2, neighborsA.size)
            assertTrue(neighborsA.containsAll(listOf(vB, vC)), "Соседи A должны быть B и C")

            assertTrue(graph.getNeighbors(vB).isEmpty(), "У B не должно быть исходящих соседей")
            assertTrue(graph.getNeighbors(vC).isEmpty(), "У C не должно быть исходящих соседей")
        }

        @Test
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertTrue(graph.isDirected(), "isDirected должно быть true")
            assertFalse(graph.isWeighted(), "isWeighted должно быть false")
        }
    }

    @Nested
    @DisplayName("Тесты для Неориентированного Невзвешенного Графа")
    inner class UndirectedUnweightedGraphTest {

        private lateinit var graph: Graph

        @BeforeEach
        fun setUp() {
            // Создаем неориентированный невзвешенный граф перед каждым тестом
            graph = GraphFactory.createUndirectedUnweightedGraph()
        }

        // Тесты на добавление/удаление вершин аналогичны ориентированному
        @Test
        @DisplayName("Добавление вершины")
        fun addVertex() {
            assertEquals(0, graph.getVertexCount(), "Изначально граф должен быть пуст")
            graph.addVertex(vA)
            assertTrue(graph.containsVertex(vA), "Граф должен содержать вершину A")
            assertEquals(1, graph.getVertexCount(), "Количество вершин должно быть 1")
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
            assertEquals(1, graph.getEdgeCount(), "Количество ребер должно быть 1 (не удваивается)") // Важно!

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

            // Удаляем A-B
            graph.removeEdge(vA, vB)
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Количество ребер должно быть 0")
            assertFalse(graph.getNeighbors(vA).contains(vB), "B не должна быть соседом A")
            assertFalse(graph.getNeighbors(vB).contains(vA), "A не должна быть соседом B")

            // Проверка удаления в обратном направлении
            graph.addEdge(vA, vB) // Добавляем снова
            assertEquals(1, graph.getEdgeCount())
            graph.removeEdge(vB, vA) // Удаляем B-A
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено после удаления B-A")
            assertFalse(graph.containsEdge(vB, vA), "Реbro B - A должно быть удалено")
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
        @DisplayName("Удаление вершины (удаляет связанные ребра неориентированно)")
        fun removeVertex() {
            graph.addVertex(vA)
            graph.addVertex(vB)
            graph.addVertex(vC)
            graph.addEdge(vA, vB) // A - B
            graph.addEdge(vB, vC) // B - C
            assertEquals(3, graph.getVertexCount())
            assertEquals(2, graph.getEdgeCount()) // A-B, B-C

            graph.removeVertex(vB) // Удаляем B

            assertFalse(graph.containsVertex(vB), "Вершина B должна быть удалена")
            assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины")
            assertFalse(graph.containsEdge(vA, vB), "Ребро A - B должно быть удалено")
            assertFalse(graph.containsEdge(vB, vA), "Ребро B - A должно быть удалено")
            assertFalse(graph.containsEdge(vB, vC), "Ребро B - C должно быть удалено")
            assertFalse(graph.containsEdge(vC, vB), "Ребро C - B должно быть удалено")
            assertEquals(0, graph.getEdgeCount(), "Все ребра, связанные с B, должны быть удалены")

            // Проверяем соседей оставшихся вершин
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
        @DisplayName("Проверка флагов isDirected и isWeighted")
        fun checkFlags() {
            assertFalse(graph.isDirected(), "isDirected должно быть false")
            assertFalse(graph.isWeighted(), "isWeighted должно быть false")
        }
    }
}