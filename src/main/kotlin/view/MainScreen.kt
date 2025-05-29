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
import androidx.compose.ui.platform.LocalDensity
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
import model.graph.Graph
import model.graph.GraphFactory
import model.graph.Vertex
import viewModel.screen.layouts.ForceAtlas2

val sampleGraph: Graph = GraphFactory.createDirectedWeightedGraph().apply {
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
    addEdge("A", "F",.3)
    addEdge("F", "G", 3.2)
//    addVertex(Vertex(1, "A"))
//    addVertex(Vertex(2, "B"))
//    addVertex(Vertex(3, "C"))
//    addVertex(Vertex(4, "D"))
//    addVertex(Vertex(5, "E"))
//    addVertex(Vertex(6, "F"))
//    addVertex(Vertex(7, "G"))
//
//    addEdge(Vertex(1, "A"), Vertex(2, "B"), 1.2)
//    addEdge(Vertex(1, "A"), Vertex(3, "C"), 3.2)
//    addEdge(Vertex(1, "A"), Vertex(4, "D"), 3.4)
//    addEdge(Vertex(1, "A"), Vertex(5, "E"), 5.2)
//    addEdge(Vertex(1, "A"), Vertex(6, "F"), 43.1)
//    addEdge(Vertex(1, "A"), Vertex(7, "G"), .43)
}

@Composable
fun MainScreen() {
    val viewModel = remember { MainScreenViewModel(sampleGraph, ForceAtlas2()) }
//    val viewModel = remember { MainScreenViewModel(sampleGraph) }


    val showGraphPanel = remember { mutableStateOf(false) }
    val showAlgoButtons = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }

    val showUploadSaveButtons = remember { mutableStateOf(false) }
    val showNeo4jDialog = remember { mutableStateOf(false) }
    val showNeo4jSaveClearButtonsPanel = remember { mutableStateOf(false) }
    val showSQLiteSaveClearButtonsPanel = remember { mutableStateOf(false) }

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
//                            showSQLiteSaveClearButtonsPanel.value = false
//                            showNeo4jSaveClearButtonsPanel.value = false
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
                        onClick = { viewModel.resetGraphView() }, //viewModel.resetGraphView()
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

                GraphView(
                    viewModel = viewModel.graphViewModel,
                    scale = scale.value,
                    // onVertexDrag = viewModel.vertex.dra
                )
            }
        }
    }
    diologistNeo4j(showNeo4jDialog, showNeo4jSaveClearButtonsPanel, viewModel)
    ErrorDialog(viewModel.showErrorDialog.value, viewModel.errorMessage.value, { viewModel.clearError() })
}

@Composable
fun DividerG() {
    Divider(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().height(1.dp)
    )
}
@Composable
fun ErrorDialog(
    showDialog: Boolean,
    message: String?,
    onDismiss: () -> Unit
) {
    if (showDialog && message != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Ok")
                }
            }
        )
    }
}