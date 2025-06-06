package view

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import model.graph.Graph
import model.graph.GraphFactory
import model.io.sqlite.SQLiteService
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import viewModel.screen.MainScreenViewModel
import viewModel.screen.layouts.ForceAtlas2

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenIntegrationTestDeleteFRunTarjanSaveSQLite {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockSqliteService: SQLiteService
    private lateinit var viewModel: MainScreenViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val initialGraph =
        GraphFactory.createDirectedWeightedGraph().apply {
            addVertex("A")
            addVertex("B")
            addVertex("C")
            addVertex("D")
            addVertex("E")
            addVertex("F")
            addVertex("G")

            addEdge("A", "B", 1.1)
            addEdge("G", "C", 32.3)
            addEdge("B", "C", 44.0)
            addEdge("A", "E", 32.1)
            addEdge("A", "F", .3)
            addEdge("F", "G", 3.2)
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockSqliteService = mockk()
        coEvery {
            mockSqliteService.saveGraphToNewFile(any(), any(), any())
        } returns Result.success("fake/path/integration_test_graph_F_deleted.db")

        viewModel =
            MainScreenViewModel(
                graph =
                    initialGraph.let { originalGraph ->
                        val newGraph = GraphFactory.createDirectedWeightedGraph()
                        originalGraph.getVertices().forEach { newGraph.addVertex(it.name) }
                        originalGraph.getEdges().forEach { newGraph.addEdge(it.source.name, it.destination.name, it.weight) }
                        newGraph
                    },
                representationStrategy = ForceAtlas2(),
                sqliteServiceInstance = mockSqliteService,
            )

        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun userDeletesF_RunsTarjan_SavesToSQLite() =
        runTest(testDispatcher) {
            assertNotNull("Vertex F should be in the graph initially", viewModel.graphViewModel.graph.getVertexByName("F"))
            val initialVertexCount = viewModel.graphViewModel.graph.getVertexCount()
            val initialEdgeCount = viewModel.graphViewModel.graph.getEdgeCount()
            val vertexA = viewModel.graphViewModel.graph.getVertexByName("A") ?: throw IllegalStateException()
            val vertexF = viewModel.graphViewModel.graph.getVertexByName("F") ?: throw IllegalStateException()
            val vertexG = viewModel.graphViewModel.graph.getVertexByName("G") ?: throw IllegalStateException()
            assertTrue("Edge A->F should exist", viewModel.graphViewModel.graph.containsEdge(vertexA, vertexF))
            assertTrue("Edge F->G should exist", viewModel.graphViewModel.graph.containsEdge(vertexF, vertexG))

            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Add/Delete", useUnmergedTree = true).performClick()
            composeTestRule.onNodeWithText("remove Vertex").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("deleteVertexDialogTitle", useUnmergedTree = true).assertIsDisplayed()

            composeTestRule.onNodeWithTag("vertexNameInput").performTextInput("F")
            composeTestRule.onNodeWithText("Delete").performClick()
            composeTestRule.waitForIdle()

            assertNull("Vertex F should be removed from the graph", viewModel.graphViewModel.graph.getVertexByName("F"))
            assertEquals("Vertex count should decrease by 1", initialVertexCount - 1, viewModel.graphViewModel.graph.getVertexCount())
            assertEquals("Edge count should decrease by 2", initialEdgeCount - 2, viewModel.graphViewModel.graph.getEdgeCount())
            assertFalse("Edge A->F should be removed", viewModel.graphViewModel.graph.containsEdge(vertexA, vertexF))
            val currentEdges = viewModel.graphViewModel.graph.getEdges()
            assertFalse("Edges connected to F should not exist", currentEdges.any { it.source.name == "F" || it.destination.name == "F" })

            composeTestRule.onNodeWithText("Algorithms").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Tarjan").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("uploadSaveButton").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("sqliteSectionButton").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("sqliteSaveAsButton").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Save SQLite Database As", substring = true).assertIsDisplayed()

            val testDbFileName = "integration_test_graph_F_deleted.db"
            composeTestRule.onNodeWithTag("fileNameInputTextField", useUnmergedTree = true).performTextClearance()
            composeTestRule.onNodeWithTag("fileNameInputTextField", useUnmergedTree = true).performTextInput(testDbFileName)

            val tempDir = System.getProperty("java.io.tmpdir")
            viewModel.setSaveAsDirectoryPath(tempDir)
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("saveDialogSaveButton").performClick()

            testDispatcher.scheduler.advanceUntilIdle()
            composeTestRule.waitForIdle()

            val graphSlot = slot<Graph>()
            coVerify {
                mockSqliteService.saveGraphToNewFile(
                    capture(graphSlot),
                    tempDir,
                    testDbFileName,
                )
            }

            val savedGraph = graphSlot.captured
            assertNull("The saved graph should not contain vertex F", savedGraph.getVertexByName("F"))
            assertEquals("The saved graph should have 6 vertices", initialVertexCount - 1, savedGraph.getVertexCount())
            assertEquals("The saved graph should have 4 edges", initialEdgeCount - 2, savedGraph.getEdgeCount())

            assertNotNull(savedGraph.getVertexByName("A"))
            assertNotNull(savedGraph.getVertexByName("B"))
            assertNotNull(savedGraph.getVertexByName("C"))
            assertNotNull(savedGraph.getVertexByName("D"))
            assertNotNull(savedGraph.getVertexByName("E"))
            assertNotNull(savedGraph.getVertexByName("G"))

            val savedVertexA = savedGraph.getVertexByName("A")!!
            val savedVertexB = savedGraph.getVertexByName("B")!!

            assertTrue(savedGraph.containsEdge(savedVertexA, savedVertexB))
            assertFalse(
                "Edge F->G should not exist in the saved graph",
                savedGraph.getEdges().any {
                    it.source.name == "F" || it.destination.name == "F"
                },
            )
        }
}
