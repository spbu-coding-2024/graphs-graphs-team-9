package integration

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import viewModel.toosl.Colors
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DijkstraAlgorithmUiTest {
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
        viewModel.graphViewModel.vertices.forEach { it.color = Colors.vertexBasic }
        viewModel.graphViewModel.edges.forEach { it.color = Colors.edgeBasic }
        composeTestRule.waitForIdle()
    }

    @Test
    fun dijkstraFindShortestPath_SimpleGraph_PathExists_And_ColorsUpdated() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addVertex("B")
                    addVertex("T")
                    addEdge("S", "A", 1.0)
                    addEdge("S", "B", 4.0)
                    addEdge("A", "B", 2.0)
                    addEdge("A", "T", 6.0)
                    addEdge("B", "T", 3.0)
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Dijkstra").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start vertex").performTextInput("S")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("End Vertex").performTextInput("T")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            assertEquals("6.0", viewModel.getFindResult())

            val graphVM = viewModel.graphViewModel
            val vertexS = graphVM.verticesMap[graphVM.graph.getVertexByName("S")]
            val vertexA = graphVM.verticesMap[graphVM.graph.getVertexByName("A")]
            val vertexB = graphVM.verticesMap[graphVM.graph.getVertexByName("B")]
            val vertexT = graphVM.verticesMap[graphVM.graph.getVertexByName("T")]

            assertNotNull(vertexS)
            assertNotNull(vertexA)
            assertNotNull(vertexB)
            assertNotNull(vertexT)

            assertEquals(Colors.pathHighlightVertex, vertexS.color, "Vertex S should be highlighted")
            assertEquals(Colors.pathHighlightVertex, vertexA.color, "Vertex A should be highlighted")
            assertEquals(Colors.pathHighlightVertex, vertexB.color, "Vertex B should be highlighted")
            assertEquals(Colors.pathHighlightVertex, vertexT.color, "Vertex T should be highlighted")

            val edgeSA =
                graphVM.edgesMap[
                    graphVM.graph.getEdgeByVertex(
                        graphVM.graph.getVertexByName("S") ?: throw IllegalStateException("Vertex must be in graph"),
                        graphVM.graph.getVertexByName("A") ?: throw IllegalStateException("Vertex must be in graph"),
                    ),
                ]
            val edgeAB =
                graphVM.edgesMap[
                    graphVM.graph.getEdgeByVertex(
                        graphVM.graph.getVertexByName("A") ?: throw IllegalStateException("Vertex must be in graph"),
                        graphVM.graph.getVertexByName("B") ?: throw IllegalStateException("Vertex must be in graph"),
                    ),
                ]
            val edgeBT =
                graphVM.edgesMap[
                    graphVM.graph.getEdgeByVertex(
                        graphVM.graph.getVertexByName("B") ?: throw IllegalStateException("Vertex must be in graph"),
                        graphVM.graph.getVertexByName("T") ?: throw IllegalStateException("Vertex must be in graph"),
                    ),
                ]
            val edgeST =
                graphVM.edgesMap[
                    graphVM.graph.getEdgeByVertex(
                        graphVM.graph.getVertexByName("S") ?: throw IllegalStateException("Vertex must be in graph"),
                        graphVM.graph.getVertexByName("T") ?: throw IllegalStateException("Vertex must be in graph"),
                    ),
                ]
            val edgeatNotinpath =
                graphVM.edgesMap[
                    graphVM.graph.getEdgeByVertex(
                        graphVM.graph.getVertexByName("A") ?: throw IllegalStateException("Vertex must be in graph"),
                        graphVM.graph.getVertexByName("T") ?: throw IllegalStateException("Vertex must be in graph"),
                    ),
                ]

            assertNotNull(edgeSA)
            assertNotNull(edgeAB)
            assertNotNull(edgeBT)

            assertEquals(Colors.pathHighlightEdge, edgeSA.color, "Edge S-A should be highlighted")
            assertEquals(Colors.pathHighlightEdge, edgeAB.color, "Edge A-B should be highlighted")
            assertEquals(Colors.pathHighlightEdge, edgeBT.color, "Edge B-T should be highlighted")

            if (edgeST != null) {
                assertEquals(Colors.edgeBasic, edgeST.color, "Edge S-T (not in path) should be basic color")
            }
            if (edgeatNotinpath != null) {
                assertEquals(Colors.edgeBasic, edgeatNotinpath.color, "Edge A-T (not in shortest path S->A->B->T) should be basic color")
            }
        }

    @Test
    fun dijkstraFindShortestPath_NoPathExists_ColorsRemainBasic() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addVertex("B")
                    addVertex("T")
                    addEdge("S", "A", 1.0)
                    addEdge("B", "T", 1.0)
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Dijkstra").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start vertex").performTextInput("S")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("End Vertex").performTextInput("T")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            assertEquals("", viewModel.getFindResult())

            val graphVM = viewModel.graphViewModel
            graphVM.vertices.forEach {
                assertEquals(Colors.vertexBasic, it.color, "Vertex ${it.vertex.name} color should be basic when no path found")
            }
            graphVM.edges.forEach {
                assertEquals(Colors.edgeBasic, it.color, "Edge color should be basic when no path found")
            }
        }

    @Test
    fun dijkstraButton_NotVisible_ForGraphWithNegativeWeights() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addVertex("T")
                    addEdge("S", "A", 1.0)
                    addEdge("A", "T", -2.0)
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Dijkstra").assertDoesNotExist()
        }

    @Test
    fun dijkstraButton_Visible_ForGraphWithOnlyPositiveWeights() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addVertex("T")
                    addEdge("S", "A", 1.0)
                    addEdge("A", "T", 2.0)
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Dijkstra").assertIsDisplayed()
        }

    @Test
    fun dijkstraButton_NotVisible_ForUnweightedGraph() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedUnweightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addVertex("T")
                    addEdge("S", "A")
                    addEdge("A", "T")
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Dijkstra").assertDoesNotExist()
        }

    @Test
    fun dijkstraFindShortestPath_SameStartAndEndVertex_ColorUpdated() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedWeightedGraph().apply {
                    addVertex("S")
                    addVertex("A")
                    addEdge("S", "A", 5.0)
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Dijkstra").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Start vertex").performTextInput("S")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("End Vertex").performTextInput("S")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            assertEquals("0.0", viewModel.getFindResult())

            val graphVM = viewModel.graphViewModel
            val vertexS = graphVM.verticesMap[graphVM.graph.getVertexByName("S")]
            val vertexaNotinpath = graphVM.verticesMap[graphVM.graph.getVertexByName("A")]

            assertNotNull(vertexS)
            assertEquals(Colors.pathHighlightVertex, vertexS.color, "Vertex S (start and end) should be highlighted")

            assertNotNull(vertexaNotinpath)
            assertEquals(Colors.vertexBasic, vertexaNotinpath.color, "Vertex A (not in path) should be basic color")

            graphVM.edges.forEach {
                assertEquals(Colors.edgeBasic, it.color, "Edge colors should remain basic")
            }
        }
}
