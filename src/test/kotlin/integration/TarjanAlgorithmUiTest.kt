package integration

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TarjanAlgorithmUiTest {
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
    fun tarjanFindSCC_And_ColorsUpdatedCorrectly() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedUnweightedGraph().apply {
                    addVertex("A")
                    addVertex("B")
                    addVertex("C")
                    addVertex("D")
                    addVertex("E")
                    addVertex("F")
                    addVertex("G")
                    addVertex("H")

                    // SCC 1: A, B, C
                    addEdge("A", "B")
                    addEdge("B", "C")
                    addEdge("C", "A")

                    // SCC 2: D, E, F
                    addEdge("D", "E")
                    addEdge("E", "F")
                    addEdge("F", "D")

                    // SCC 3: G
                    // SCC 4: H
                    addEdge("C", "D")
                    addEdge("F", "G")
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Tarjan").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            val graphVM = viewModel.graphViewModel

            val vmA =
                graphVM.verticesMap[graphVM.graph.getVertexByName("A")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmB =
                graphVM.verticesMap[graphVM.graph.getVertexByName("B")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmC =
                graphVM.verticesMap[graphVM.graph.getVertexByName("C")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmD =
                graphVM.verticesMap[graphVM.graph.getVertexByName("D")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmE =
                graphVM.verticesMap[graphVM.graph.getVertexByName("E")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmF =
                graphVM.verticesMap[graphVM.graph.getVertexByName("F")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmG =
                graphVM.verticesMap[graphVM.graph.getVertexByName("G")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmH =
                graphVM.verticesMap[graphVM.graph.getVertexByName("H")]
                    ?: throw IllegalStateException("Vertex must be in graph")

            assertNotEquals(Colors.vertexBasic, vmA.color, "Color of A should have changed")
            assertNotEquals(Colors.vertexBasic, vmB.color, "Color of B should have changed")
            assertNotEquals(Colors.vertexBasic, vmC.color, "Color of C should have changed")
            assertNotEquals(Colors.vertexBasic, vmD.color, "Color of D should have changed")
            assertNotEquals(Colors.vertexBasic, vmE.color, "Color of E should have changed")
            assertNotEquals(Colors.vertexBasic, vmF.color, "Color of F should have changed")
            assertNotEquals(Colors.vertexBasic, vmG.color, "Color of G should have changed")
            assertNotEquals(Colors.vertexBasic, vmH.color, "Color of H should have changed")

            val colorSCC1 = vmA.color
            assertEquals(colorSCC1, vmB.color, "B should have same color as A (SCC1)")
            assertEquals(colorSCC1, vmC.color, "C should have same color as A (SCC1)")

            val colorSCC2 = vmD.color
            assertEquals(colorSCC2, vmE.color, "E should have same color as D (SCC2)")
            assertEquals(colorSCC2, vmF.color, "F should have same color as D (SCC2)")

            val colorSCC3 = vmG.color
            val colorSCC4 = vmH.color

            assertNotEquals(colorSCC1, colorSCC2, "SCC1 and SCC2 should have different colors")
            assertNotEquals(colorSCC1, colorSCC3, "SCC1 and SCC3 should have different colors")
            assertNotEquals(colorSCC1, colorSCC4, "SCC1 and SCC4 should have different colors")
            assertNotEquals(colorSCC2, colorSCC3, "SCC2 and SCC3 should have different colors")
            assertNotEquals(colorSCC2, colorSCC4, "SCC2 and SCC4 should have different colors")
            assertNotEquals(colorSCC3, colorSCC4, "SCC3 and SCC4 should have different colors")

            graphVM.edges.forEach { edgeVM ->
                assertEquals(Colors.edgeBasic, edgeVM.color, "Edge colors should remain basic")
            }
        }

    @Test
    fun tarjan_SingleSCC_AllVerticesSameColor() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedUnweightedGraph().apply {
                    addVertex("X")
                    addVertex("Y")
                    addVertex("Z")
                    addEdge("X", "Y")
                    addEdge("Y", "Z")
                    addEdge("Z", "X")
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Tarjan").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            val graphVM = viewModel.graphViewModel
            val vmX =
                graphVM.verticesMap[graphVM.graph.getVertexByName("X")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmY =
                graphVM.verticesMap[graphVM.graph.getVertexByName("Y")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmZ =
                graphVM.verticesMap[graphVM.graph.getVertexByName("Z")]
                    ?: throw IllegalStateException("Vertex must be in graph")

            val componentColor = vmX.color
            assertNotEquals(Colors.vertexBasic, componentColor, "Color should have changed from basic")
            assertEquals(componentColor, vmY.color, "Y should have same color as X")
            assertEquals(componentColor, vmZ.color, "Z should have same color as X")

            graphVM.edges.forEach { assertEquals(Colors.edgeBasic, it.color) }
        }

    @Test
    fun tarjan_NoEdges_EachVertexIsAnSCC_DifferentColors() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory.createDirectedUnweightedGraph().apply {
                    addVertex("V1")
                    addVertex("V2")
                    addVertex("V3")
                }
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Tarjan").performClick()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            testDispatcher.scheduler.advanceTimeBy(300)
            composeTestRule.waitForIdle()

            val graphVM = viewModel.graphViewModel
            val vmV1 =
                graphVM.verticesMap[graphVM.graph.getVertexByName("V1")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmV2 =
                graphVM.verticesMap[graphVM.graph.getVertexByName("V2")]
                    ?: throw IllegalStateException("Vertex must be in graph")
            val vmV3 =
                graphVM.verticesMap[graphVM.graph.getVertexByName("V3")]
                    ?: throw IllegalStateException("Vertex must be in graph")

            val colorV1 = vmV1.color
            val colorV2 = vmV2.color
            val colorV3 = vmV3.color

            assertNotEquals(Colors.vertexBasic, colorV1)
            assertNotEquals(Colors.vertexBasic, colorV2)
            assertNotEquals(Colors.vertexBasic, colorV3)

            assertTrue(
                colorV1 != colorV2 || colorV1 != colorV3 || colorV2 != colorV3,
                "At least two vertices should have different colors if they are distinct SCCs. " +
                    "If all are same, random generator might have produced same color multiple times.",
            )
            if (graphVM.vertices.size <= 5) {
                assertNotEquals(colorV1, colorV2, "Colors of V1 and V2 should be different")
                assertNotEquals(colorV1, colorV3, "Colors of V1 and V3 should be different")
                assertNotEquals(colorV2, colorV3, "Colors of V2 and V3 should be different")
            }

            graphVM.edges.forEach { assertEquals(Colors.edgeBasic, it.color) }
        }
}
