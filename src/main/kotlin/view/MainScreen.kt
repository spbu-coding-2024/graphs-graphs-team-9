package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import view.additionalButtons.DBButtons
import view.additionalButtons.VertexSizeSlider
import view.additionalButtons.algoButton
import view.additionalButtons.barButton
import view.additionalButtons.switch
import view.graph.GraphView
import viewModel.additionalScreen.diologistNeo4j
import viewModel.screen.MainScreenViewModel
import viewModel.toosl.CoolColors
import androidx.compose.ui.unit.IntSize
import model.graph.Graph
import model.graph.GraphFactory
import viewModel.screen.layouts.ForceAtlas2
import view.additionalScreen.SaveAsSQLiteDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag

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
    addEdge("A", "F", .3)
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
fun MainScreen(viewModel: MainScreenViewModel = remember { MainScreenViewModel(sampleGraph, ForceAtlas2()) }) {

    val showGraphPanel = remember { mutableStateOf(false) }
    val showAlgoButtons = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }

    val showUploadSaveButtons = remember { mutableStateOf(false) }
    val showNeo4jDialog = remember { mutableStateOf(false) }
    val showNeo4jSaveClearButtonsPanel = remember { mutableStateOf(false) }
    val showSQLiteSaveClearButtonsPanel = remember { mutableStateOf(false) }
    val showResult = remember { mutableStateOf(false) }


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
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    DividerG()

                    Button(
                        onClick = {
                            showUploadSaveButtons.value = !showUploadSaveButtons.value
                        },
                        modifier = Modifier.fillMaxWidth().testTag("uploadSaveButton")
                    ) { Text("Upload/Save") }

                    DBButtons(
                        viewModel,
                        showNeo4jSaveClearButtonsPanel,
                        showSQLiteSaveClearButtonsPanel,
                        showUploadSaveButtons,
                        showNeo4jDialog,
                    )
                    DividerG()
                    switch(viewModel, remember { mutableStateOf(viewModel.showVerticesLabels) })
                    Spacer(modifier = Modifier.height(8.dp))
                    DividerG()
                    VertexSizeSlider(viewModel = viewModel, modifier = Modifier.padding(vertical = 4.dp))
                    DividerG()
                    Button(
                        onClick = {
                            viewModel.resetGraphView()
                            viewModel.resetColor()
                            showResult.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Reset Graph Layout") }
                    DividerG()
                    Button(
                        onClick = { showAlgoButtons.value = !showAlgoButtons.value },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Algorithms") }
                    algoButton(viewModel, showAlgoButtons, showResult)
                    DividerG()
                    Spacer(modifier = Modifier.padding(4.dp))
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
                            scale.value = (scale.value * (1f + delta / 500f)).coerceAtLeast(0.001f)
                            delta
                        }
                    )
            ) {
                GraphView(
                    viewModel = viewModel.graphViewModel,
                    scale = scale.value,
                )
                this@Row.AnimatedVisibility(
                    visible = showResult.value,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp),
                        elevation = 8.dp,
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(0.35f),
                                text = "Result: ",
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text =  if (viewModel.getFindResult() == "") "No route" else viewModel.getFindResult(),
                            )
                        }
                    }
                }
            }
        }
    }
    diologistNeo4j(showNeo4jDialog, showNeo4jSaveClearButtonsPanel, viewModel)
    SaveAsSQLiteDialog(
        showDialog = viewModel.showSaveAsSQLiteDialog.value,
        viewModel = viewModel,
        onDismissRequest = { viewModel.cancelSaveAsSQLiteDialog() }
    )
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
