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
import view.graph.GraphView
import viewModel.additionalScreen.diologistFordBellman
import viewModel.additionalScreen.diologistNeo4j
import viewModel.screen.MainScreenViewModel

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val showGraph = remember { mutableStateOf(false) }
    val expandedSettingsMenu = remember { mutableStateOf(false) }
    val showUploadSaveButtons = remember { mutableStateOf(false) }
    val showAlgoButtons = remember { mutableStateOf(false) }
    val scale = remember { mutableStateOf(1f) }
    val showNeo4j = remember { mutableStateOf(false) }
    val showFordBellman = remember { mutableStateOf(false) }
    var uri = remember { viewModel.uri }
    var username = remember { viewModel.username }
    var password = remember { viewModel.password }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).fillMaxHeight(0.04f)) {
            Row(
                modifier = Modifier.padding(vertical = 2.dp).background(Color.DarkGray),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Min)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { showGraph.value = !showGraph.value }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Graph", color = Color.White)
                        }
                    }
                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Min)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { expandedSettingsMenu.value = true }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Settings", color = Color.White)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedSettingsMenu.value,
                        onDismissRequest = { expandedSettingsMenu.value = false }
                    ) {
                        DropdownMenuItem(onClick = {}) {}
                    }
                }
            }
        }
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

                    Button(
                        onClick = { showUploadSaveButtons.value = !showUploadSaveButtons.value },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload/save")
                    }
                    AnimatedVisibility(
                        visible = showUploadSaveButtons.value,
                    ) {
                        Column(
                            modifier = Modifier
                                .absolutePadding(left = 8.dp, right = 8.dp)
                        ) {

                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("SQLite")
                            }
                            Button(
                                onClick = { showNeo4j.value = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Neo4j")
                            }
                            Button(
                                onClick = {viewModel::clearGraph},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear Graph")
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Graph")
                            }
                        }
                    }
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth(230f)
                            .width(1.dp)
                    )

                    Row {
                        Switch(
                            checked = viewModel.showVerticesLabels,
                            onCheckedChange = { viewModel.showVerticesLabels = it }
                        )
                        Text(
                            text = "Show vertices labels",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Row {
                        Switch(
                            checked = viewModel.showEdgesLabels,
                            onCheckedChange = { viewModel.showEdgesLabels = it }
                        )
                        Text(
                            text = "Show edges labels",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth(230f)
                            .width(1.dp)
                    )
//            Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::resetGraphView,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Reset default settings")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth(230f)
                            .width(1.dp)
                    )
                    Button(
                        onClick = { showAlgoButtons.value = !showAlgoButtons.value },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Algorithms")
                    }
                    AnimatedVisibility(
                        visible = showAlgoButtons.value,
                    ) {
                        Column(
                            modifier = Modifier
                                .absolutePadding(left = 8.dp, right = 8.dp)
                        ) {

                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dijkstra")
                            }
                            Button(
                                onClick = viewModel::runFindBridge,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Find Bridges")
                            }
                            Button(
                                onClick = {showFordBellman.value = !showFordBellman.value},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ford Bellman")
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tarjan")
                            }
                        }
                    }
                    Divider(
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth(230f)
                            .width(1.dp)
                    )

                    diologistNeo4j(showNeo4j)
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