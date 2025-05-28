package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.additionalButtons.DBButtons
import view.additionalButtons.VertexSizeSlider
import view.additionalButtons.algoButton
import view.additionalButtons.barButton
import view.additionalButtons.switch
import view.additionalScreen.diologistAddEdgeScreen
import view.additionalScreen.diologistAddVertexScreen
import view.additionalScreen.diologistDeleteEdgeScreen
import view.additionalScreen.diologistDeleteVertexScreen
import view.graph.GraphView
import viewModel.additionalScreen.diologistDijkstraScreen
import viewModel.additionalScreen.diologistFordBellman
import viewModel.additionalScreen.diologistNeo4j
import viewModel.screen.MainScreenViewModel
import viewModel.toosl.CoolColors
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import model.graph.EmptyGraph
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex

val sampleGraph: Graph = GraphFactory.createDirectedUnweightedGraph().apply {
    addVertex(Vertex(1, "A"))
    addVertex(Vertex(2, "B"))
    addVertex(Vertex(3, "C"))
    addVertex(Vertex(4, "D"))
    addVertex(Vertex(5, "E"))
    addVertex(Vertex(6, "F"))
    addVertex(Vertex(7, "G"))

    addEdge(Vertex(1, "A"), Vertex(2, "B"))
    addEdge(Vertex(1, "A"), Vertex(3, "C"))
    addEdge(Vertex(1, "A"), Vertex(4, "D"))
    addEdge(Vertex(1, "A"), Vertex(5, "E"))
    addEdge(Vertex(1, "A"), Vertex(6, "F"))
    addEdge(Vertex(1, "A"), Vertex(7, "G"))
}

@Composable
fun MainScreen() {
    val viewModel = remember { MainScreenViewModel(GraphFactory.createUndirectedUnweightedGraph()) }
//    val viewModel = remember { MainScreenViewModel(sampleGraph) }

    println(viewModel.getVertexs())

    val showGraphPanel = remember { mutableStateOf(false) }
    val showAlgoButtons = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }

    val showUploadSaveButtons = remember { mutableStateOf(false) }
    val showNeo4jDialog = remember { mutableStateOf(false) }
    val showNeo4jSaveClearButtonsPanel = remember { mutableStateOf(false) }
    val showSQLiteSaveClearButtonsPanel = remember { mutableStateOf(false) }

    val uri = remember { viewModel.uri }
    val username = remember { viewModel.username }
    val password = remember { viewModel.password }

//    DBButtons(
//        showNeo4jSaveClearButtonsPanel,
//        showSQLiteSaveClearButtonsPanel,
//        showUploadSaveButtons,
//        showNeo4jDialog
//    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        barButton(showGraphPanel, viewModel)

        Divider(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth().height(1.dp)
        )
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = showGraphPanel.value,
            ) {
                Column(
                    modifier = Modifier
                        .width(232.dp)
                        .padding(horizontal = 8.dp)
                        .scrollable(rememberScrollableState { 0f }, orientation = Orientation.Vertical)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    DividerG()

                    Button(
                        onClick = {
                            showUploadSaveButtons.value = !showUploadSaveButtons.value
                            showSQLiteSaveClearButtonsPanel.value = false
                            showNeo4jSaveClearButtonsPanel.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Upload/Save") }

                    DBButtons(
                        viewModel,
                        showNeo4jSaveClearButtonsPanel,
                        showSQLiteSaveClearButtonsPanel,
                        showUploadSaveButtons,
                        showNeo4jDialog
                    )
                    DividerG()
                    switch(viewModel, remember { mutableStateOf(viewModel.showVerticesLabels) })
                    Spacer(modifier = Modifier.height(8.dp))
                    DividerG()
                    VertexSizeSlider(viewModel = viewModel, modifier = Modifier.padding(vertical = 4.dp))
                    DividerG()
                    Button(
                        onClick = { viewModel.resetGraphView() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Reset Graph Layout") }
                    DividerG()
                    Button(
                        onClick = { showAlgoButtons.value = !showAlgoButtons.value },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Algorithms") }
                    algoButton(viewModel, showAlgoButtons)
                    DividerG()


                }
            }

            Divider(
                color = Color.Black,
                modifier = Modifier.fillMaxHeight().width(1.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .background(CoolColors.backgroundBasic)
                    .clipToBounds()
                    .onSizeChanged { newSize ->
                        surfaceSize = newSize
                    }
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta ->
                            scale.value = (scale.value * (1f + delta / 500f)).coerceIn(0.1f, 5f)
                            delta
                        }
                    )
            ) {
                val canvasWidthDp = maxWidth
                val canvasHeightDp = maxHeight

                LaunchedEffect(canvasWidthDp, canvasHeightDp, viewModel.graphViewModel.graph) {
                    if (canvasWidthDp > 0.dp && canvasHeightDp > 0.dp) {
                        viewModel.initializeOrUpdatePlacement(canvasWidthDp, canvasHeightDp)
                    }
                }

                GraphView(
                    viewModel = viewModel.graphViewModel,
                    scale = scale.value,
                    onVertexDrag = viewModel::processVertexDrag
                )
            }
        }
    }
    diologistNeo4j(showNeo4jDialog, showNeo4jSaveClearButtonsPanel, viewModel, viewModel.uri, viewModel.username, viewModel.password)
}

@Composable
fun DividerG() {
    Divider(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().height(1.dp)
    )
}