package integration

import androidx.compose.material.MaterialTheme
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
import model.io.neo4j.Neo4jRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.neo4j.harness.Neo4j
import org.neo4j.harness.Neo4jBuilders
import view.MainScreen
import viewModel.screen.MainScreenViewModel
import viewModel.screen.layouts.ForceAtlas2
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IntegrationTestNeo4j {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MainScreenViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var neo4jServer: Neo4j
    private lateinit var neo4j: Neo4jRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        neo4jServer =
            Neo4jBuilders
                .newInProcessBuilder()
                .withDisabledServer()
                .build()
        neo4j = Neo4jRepository(neo4jServer.boltURI().toString(), "neo4j", "password")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        neo4jServer.close()
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
    fun neo4jConnectionTest() =
        runTest(testDispatcher) {
            val graph =
                GraphFactory
                    .createUndirectedUnweightedGraph()
            setupViewModel(graph)

            composeTestRule.onNodeWithText("Graph").performClick()
            composeTestRule.onNodeWithText("Upload/Save").performClick()
            composeTestRule.onNodeWithText("Neo4j").performClick()

            composeTestRule.onNodeWithText("URI").performTextInput(neo4jServer.boltURI().toString())
            composeTestRule.onNodeWithText("Username").performTextInput("neo4j")
            composeTestRule.onNodeWithText("Password").performTextInput("password")
            composeTestRule.onNodeWithText("Connect").performClick()

            // Даем больше времени для соединения
            testDispatcher.scheduler.advanceTimeBy(2000)
            composeTestRule.waitForIdle()

            assertTrue(
                viewModel.errorMessage.value.isNullOrEmpty(),
                "Connection failed: ${viewModel.errorMessage.value}",
            )
        }

    @Test
    fun uploadGraphFromNeo4jTest() =
        runTest(testDispatcher) {
            val testGraph =
                GraphFactory.createUndirectedUnweightedGraph().apply {
                    addVertex("1")
                    addVertex("2")
                    addEdge("1", "2")
                }

            // Явно записываем тестовый граф в Neo4j
            neo4j.clearDatabase()
            neo4j.writeDB(testGraph)

            setupViewModel(GraphFactory.createUndirectedUnweightedGraph())

            composeTestRule.onNodeWithText("Graph").performClick()
            composeTestRule.onNodeWithText("Upload/Save").performClick()
            composeTestRule.onNodeWithText("Neo4j").performClick()

            composeTestRule.onNodeWithText("URI").performTextInput(neo4jServer.boltURI().toString())
            composeTestRule.onNodeWithText("Username").performTextInput("neo4j")
            composeTestRule.onNodeWithText("Password").performTextInput("password")

            // Убедимся, что параметры графа установлены правильно
            viewModel.setIsDirect(false)
            viewModel.setIsWeight(false)

            composeTestRule.onNodeWithText("Upload Graph").performClick()

            // Даем больше времени для загрузки
            testDispatcher.scheduler.advanceTimeBy(5000)
            composeTestRule.waitForIdle()

            val loadedGraph = neo4j.readFromDB(false, false)
            val loadedVertices = loadedGraph.getVertices().map { it.name }

            assertTrue(loadedVertices.contains("1"))
            assertTrue(loadedVertices.contains("2"))
            assertEquals(1, loadedGraph.getEdges().size)
        }
}
