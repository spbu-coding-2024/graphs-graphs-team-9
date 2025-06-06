package integration

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import model.graph.GraphFactory
import model.graph.GraphImpl
import model.io.sqlite.SQLiteService
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import view.MainScreen
import viewModel.screen.MainScreenViewModel
import viewModel.screen.layouts.ForceAtlas2

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenUiWithMockTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockSqliteService: SQLiteService
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var graphToSave: GraphImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockSqliteService = mockk()
        graphToSave =
            GraphFactory.createDirectedWeightedGraph().apply {
                addVertex("TestV1")
                addVertex("TestV2")
                addEdge("TestV1", "TestV2", 1.0)
            } as GraphImpl

        coEvery { mockSqliteService.saveGraphToNewFile(any(), any(), any()) } returns Result.success("fake/path/test_graph.db")
        coEvery { mockSqliteService.loadGraphFromFile(any()) } returns Result.success(Pair(graphToSave, "fake/path/test_graph.db"))
        coEvery { mockSqliteService.saveGraphToCurrentFile(any(), any()) } returns Result.success(Unit)

        viewModel =
            MainScreenViewModel(
                // Initial empty graph
                graph = GraphFactory.createUndirectedUnweightedGraph(),
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
    fun sqliteSaveButton_becomesEnabled_afterSuccessfulSaveAs() =
        runTest(testDispatcher) {
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.onNodeWithTag("uploadSaveButton").performClick()
            composeTestRule.onNodeWithTag("sqliteSectionButton").performClick()
            composeTestRule.waitForIdle()

            // Verify "Save" button is initially disabled
            composeTestRule.onNodeWithTag("sqliteSaveButton").assertIsNotEnabled()

            // Prepare and execute "Save As" operation
            // Set the graph that will be saved
            viewModel.setNewGraph(graphToSave)
            viewModel.setSaveAsDirectoryPath(System.getProperty("java.io.tmpdir"))
            viewModel.setSaveAsFileName("test_graph.db")

            viewModel.confirmSaveAsSQLite()
            testDispatcher.scheduler.runCurrent()
            // Wait for UI to update after state change
            composeTestRule.waitForIdle()

            // Verify "Save" button is now enabled
            composeTestRule.onNodeWithTag("sqliteSaveButton").assertIsEnabled()

            // Verify the mock service was called correctly
            coVerify {
                mockSqliteService.saveGraphToNewFile(
                    // Ensure the correct graph instance was passed
                    match { it === graphToSave },
                    System.getProperty("java.io.tmpdir"),
                    "test_graph.db",
                )
            }
        }

    @Test
    fun clickingSaveAsButton_thenCancellingDialog_saveButtonRemainsDisabled() =
        runTest(testDispatcher) {
            // Open UI panels
            composeTestRule.onNodeWithText("Graph", useUnmergedTree = true).performClick()
            composeTestRule.onNodeWithTag("uploadSaveButton").performClick()
            composeTestRule.onNodeWithTag("sqliteSectionButton").performClick()
            composeTestRule.waitForIdle()

            // Verify "Save" button is initially disabled
            composeTestRule.onNodeWithTag("sqliteSaveButton").assertIsNotEnabled()

            // Click "Save As" to show the dialog
            composeTestRule.onNodeWithTag("sqliteSaveAsButton").performClick()
            composeTestRule.waitForIdle()

            // Verify dialog is shown
            composeTestRule.onNodeWithText("Save SQLite Database As", substring = true).assertIsDisplayed()

            // Click "Cancel" in the dialog
            composeTestRule.onNodeWithText("Cancel", useUnmergedTree = true).performClick()
            composeTestRule.waitForIdle()

            // Verify dialog is dismissed and "Save" button remains disabled
            composeTestRule.onNodeWithText("Save SQLite Database As", substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithTag("sqliteSaveButton").assertIsNotEnabled()

            // Verify that saveGraphToNewFile was not called
            coVerify(exactly = 0) { mockSqliteService.saveGraphToNewFile(any(), any(), any()) }
        }
}
