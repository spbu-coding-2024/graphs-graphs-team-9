package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.additionalButtons.DBButtons
import view.additionalButtons.VertexSizeSlider
import view.additionalButtons.algoButton
import view.additionalButtons.barButton
import view.additionalButtons.switch
import view.additionalScreen.diologistAddVertexScreen
import view.graph.GraphView
import viewModel.additionalScreen.diologistDijkstraScreen
import viewModel.additionalScreen.diologistFordBellman
import viewModel.additionalScreen.diologistNeo4j
import viewModel.screen.MainScreenViewModel

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val showGraph = remember { mutableStateOf(false) }
    val showAddMenu = remember { mutableStateOf(false) }
    val showSettingsMenu = remember { mutableStateOf(false) }
    val showAddVertex = remember { mutableStateOf(false) }
    val showUploadSaveButtons = remember { mutableStateOf(false) }
    val showAlgoButtons = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    val showNeo4jScreen = remember { mutableStateOf(false) }
    val showDijkstraScreen = remember { mutableStateOf(false) }
    val showNeo4jSaveClearButton = remember { mutableStateOf(false) }
    val showSQLiteSaveClearButton = remember { mutableStateOf(false) }
    val showFordBellman = remember { mutableStateOf(false) }
    val showLabels = remember { mutableStateOf(false) }
    var uri = remember { viewModel.uri }
    var username = remember { viewModel.username }
    var password = remember { viewModel.password }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        barButton(showGraph, showAddMenu, showSettingsMenu, showAddVertex)

        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .width(1.dp)
        )
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = showGraph.value,
            ) {
                Column(
                    modifier = Modifier
                        .width(232.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    DividerG()

                    Button(
                        onClick = {
                            showUploadSaveButtons.value = !showUploadSaveButtons.value
                            showSQLiteSaveClearButton.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload/save")
                    }

                    DBButtons(viewModel, showNeo4jSaveClearButton, showSQLiteSaveClearButton, showUploadSaveButtons, showNeo4jScreen)

                    DividerG()

                    switch(viewModel, showLabels)

                    Spacer(modifier = Modifier.height(8.dp))

                    DividerG()
//            Spacer(modifier = Modifier.height(8.dp))

                    VertexSizeSlider(
                            viewModel = viewModel,
                            modifier = Modifier.padding(vertical = 4.dp)
                    )

                    DividerG()

                    Button(
                        onClick = viewModel::resetGraphView,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Reset default settings")
                    }
//                    Spacer(modifier = Modifier.height(8.dp))

                    DividerG()

                    Button(
                        onClick = { showAlgoButtons.value = !showAlgoButtons.value },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Algorithms")
                    }

                    algoButton(viewModel, showAlgoButtons, showFordBellman, showDijkstraScreen)

                    DividerG()

                    diologistAddVertexScreen(showAddVertex, viewModel)
                    diologistDijkstraScreen(showDijkstraScreen, viewModel)
                    diologistNeo4j(showNeo4jScreen,  showNeo4jSaveClearButton)
                    diologistFordBellman(showFordBellman, viewModel)
                }
            }

            Divider(
                color = Color.Black,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Surface(
                modifier = Modifier.weight(1f).scrollable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberScrollableState { delta ->
                            scale.value = (scale.value * (1f + delta / 500)).coerceIn(0.01f, 100f)
                            delta
                        },
                )
            ) {
                GraphView(viewModel.graphViewModel, scale.value)
            }
        }
    }
}

@Composable
fun DividerG(){
    Divider(
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth(230f)
            .width(1.dp)
    )
}

//                Box {
//                    TextButton(
//                        onClick = { expandedFileMenu.value = true },
//                        modifier = Modifier.padding(0.dp)
//                    ) {
//                        Text("File", color = Color.White)
//                    }
//                    DropdownMenu(
//                        expanded = expandedFileMenu.value,
//                        onDismissRequest = { expandedFileMenu.value = false }
//                    ) {
//                        DropdownMenuItem(onClick = { /* SQLite */ }) {
//                            Text("SQLite")
//                        }
//                        DropdownMenuItem(onClick = { showNeo4j.value = true }) {
//                            Text("Neo4j")
//                        }
//                        DropdownMenuItem(onClick = { /* Clear Graph */ }) {
//                            Text("Clear Graph")
//                        }
//                    }
//                }
//            }