package view.additionalButtons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import view.additionalScreen.diologistAddEdgeScreen
import view.additionalScreen.diologistAddVertexScreen
import view.additionalScreen.diologistDeleteEdgeScreen
import view.additionalScreen.diologistDeleteVertexScreen
import viewModel.screen.MainScreenViewModel

@Composable
fun barButton(
    showGraph: MutableState<Boolean>,
    viewModel: MainScreenViewModel,
) {
    val showAddMenu = remember { mutableStateOf(false) }
    val showSettingsMenu = remember { mutableStateOf(false) }
    val showAddVertex = remember { mutableStateOf(false) }
    val showDeleteVertex = remember { mutableStateOf(false) }
    val showAddEdgeDialog = remember { mutableStateOf(false) }
    val showDeleteEdge = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.DarkGray).height(28.dp)
    ) {
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
                    onClick = { showSettingsMenu.value = true }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create graph", color = Color.White)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.Gray
                )
                DropdownMenu(
                    expanded = showSettingsMenu.value,
                    onDismissRequest = {
                        showSettingsMenu.value = false
                    }
                ) {
                    DropdownMenuItem(onClick = {
                        viewModel.createGraph(true, true)
                        showSettingsMenu.value = false
                    }) {
                        Text("Directed weighted graph")
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.createGraph(false, true)
                        showSettingsMenu.value = false
                    }) {
                        Text("Undirected weighted graph")
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.createGraph(true, false)
                        showSettingsMenu.value = false
                    }) {
                        Text("Directed unweighted graph")
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.createGraph(false, false)
                        showSettingsMenu.value = false
                    }
                    ) {
                        Text("Undirected unweighted graph")
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Min)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { showAddMenu.value = true }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Add/Delete", color = Color.White)
                        }
                    }
                }
                DropdownMenu(
                    expanded = showAddMenu.value,
                    onDismissRequest = { showAddMenu.value = false }
                ) {
                    DropdownMenuItem(onClick = {
                        showAddVertex.value = true
                        showAddMenu.value = false
                    }) {
                        Text("Add Vertex")
                    }
                    DropdownMenuItem(onClick = {
                        showAddEdgeDialog.value = true
                        showAddMenu.value = false
                    }) {
                        Text("Add Edge")
                    }
                    DropdownMenuItem(onClick = {
                        showDeleteVertex.value = true
                        showAddMenu.value = false
                    }) {
                        Text("remove Vertex")
                    }
                    DropdownMenuItem(onClick = {
                        showDeleteEdge.value = true
                        showAddMenu.value = false
                    }) {
                        Text("remove Edge")
                    }
                }
            }
        }
    }
    diologistAddVertexScreen(showAddVertex, viewModel)
    diologistDeleteVertexScreen(showDeleteVertex, viewModel)
    diologistAddEdgeScreen(showAddEdgeDialog, viewModel)
    diologistDeleteEdgeScreen(showDeleteEdge, viewModel)
}
