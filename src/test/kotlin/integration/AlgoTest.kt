package integration

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import model.graph.Graph
import model.graph.GraphFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import view.MainScreen
import viewModel.screen.MainScreenViewModel
import viewModel.screen.layouts.ForceAtlas2
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AlgoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MainScreenViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupViewModel(graph: Graph) {
        viewModel = MainScreenViewModel(graph, ForceAtlas2())
        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun findKeyVertexTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createUndirectedUnweightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")
                    addVertex("E")
                    addEdge("A", "B")
                    addEdge("B", "C")
                    addEdge("C", "D")
                    addEdge("D", "E")
                    addEdge("E", "A")
                    addEdge("A", "D")
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find key vertex").performClick()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(100)
            composeTestRule.waitForIdle()

            // Ожидаем выполнения и проверяем
            val vertices = viewModel.graphViewModel.vertices.toList()
            val sizes = vertices.associate { it.vertex.name to it.radius }
            assertTrue((sizes["A"] ?: 0.dp) > 25.dp, "Key vertex A > 25dp")
            assertTrue((sizes["D"] ?: 0.dp) > 25.dp, "Key vertex D > 25dp")
        }

    @Test
    fun fordBellmanNegativeWeightsTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")

                    addEdge("A", "B", 1.0)
                    addEdge("B", "C", -2.0)
                    addEdge("C", "D", 3.0)
                    addEdge("A", "D", 5.0)
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()

            // Взаимодействие с диалогом Ford-Bellman
            composeTestRule.onNodeWithText("Ford Bellman").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start vertex").performTextInput("A")
            composeTestRule.onNodeWithText("End Vertex").performTextInput("D")
            composeTestRule.onNodeWithText("Find").performClick()

            // Ожидаем выполнения и проверяем
            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            assertEquals("2.0", viewModel.getFindResult())
        }

    @Test
    fun fordBellmanNoPathTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")

                    addEdge("A", "B", 1.0)
                    addEdge("C", "D", 2.0)
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()

            // Взаимодействие с диалогом Ford-Bellman
            composeTestRule.onNodeWithText("Ford Bellman").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start vertex").performTextInput("A")
            composeTestRule.onNodeWithText("End Vertex").performTextInput("D")
            composeTestRule.onNodeWithText("Find").performClick()

            // Ожидаем выполнения и проверяем
            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            assertEquals("", viewModel.getFindResult())
        }

    @Test
    fun findBridgesInSimpleGraphTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createUndirectedUnweightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addEdge("A", "B")
                    addEdge("B", "C")
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find Bridges").performClick()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(100)
            composeTestRule.waitForIdle()

            // Проверяем, что мост A-B выделен
            val edges = viewModel.graphViewModel.edges.toList()
            val bridgeEdges = edges.filter { it.color != Color.Gray }
            assertTrue(bridgeEdges.size == 2, "Should find 2 bridge")
        }

    @Test
    fun findBridgesInGraphWithCycleTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createUndirectedUnweightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")
                    addEdge("A", "B")
                    addEdge("B", "C")
                    addEdge("C", "D")
                    addEdge("D", "A") // Создаем цикл
                    addEdge("A", "C")
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find Bridges").performClick()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(100)
            composeTestRule.waitForIdle()

            // В этом графе не должно быть мостов, так как все вершины в цикле
            val edges = viewModel.graphViewModel.edges.toList()
            val bridgeEdges = edges.filter { it.color != Color.Gray }
            assertTrue(bridgeEdges.isEmpty(), "Should find no bridges")
        }

    @Test
    fun findBridgesInComplexGraphTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createUndirectedUnweightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")
                    addVertex("E")
                    addVertex("F")
                    addEdge("A", "B")
                    addEdge("B", "C")
                    addEdge("C", "D")
                    addEdge("D", "A") // Цикл A-B-C-D
                    addEdge("C", "E") // Мост C-E
                    addEdge("E", "F") // Мост E-F
                    addEdge("F", "E") // Обратное ребро
                }

            setupViewModel(graph)

            // Открываем UI элементы
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find Bridges").performClick()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()
            testDispatcher.scheduler.advanceTimeBy(100)
            composeTestRule.waitForIdle()

            // Проверяем, что только мосты C-E и E-F выделены
            val edges = viewModel.graphViewModel.edges.toList()
            val bridgeEdges = edges.filter { it.color != Color.Gray }
            assertTrue(bridgeEdges.size == 2)
        }
}
