package view.additionalButtons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun barButton(
    showGraph: MutableState<Boolean>,
    showSettingsMenu: MutableState<Boolean>,
    showAddMenu: MutableState<Boolean>,
    showAddVertex: MutableState<Boolean>,
    showDeleteVertex: MutableState<Boolean>,
    showAddEdgeDialog: MutableState<Boolean>,
    showDeleteEdge: MutableState<Boolean>,
) {
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
                        Text("Settings", color = Color.White)
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
                    onDismissRequest = { showSettingsMenu.value = false }
                ) {
                    DropdownMenuItem(onClick = {showSettingsMenu.value = false}) {
                        Text("light topic")
                    }
                    DropdownMenuItem(onClick = {showSettingsMenu.value = false}) {
                        Text("light topic")
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
                            Text("Add", color = Color.White)
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
                        showDeleteVertex.value =true
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
}

//@Composable
//fun barButton(
//    showGraph: MutableState<Boolean>,
//    expandedSettingsMenu: MutableState<Boolean>,
//) {
//    Box(
//        modifier = Modifier.fillMaxWidth().background(Color.DarkGray).height(28.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(vertical = 2.dp).background(Color.DarkGray),
//            horizontalArrangement = Arrangement.Start,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Graph button
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(IntrinsicSize.Min)
//            ) {
//                DropdownMenuItem(
//                    modifier = Modifier.fillMaxSize(),
//                    onClick = { showGraph.value = !showGraph.value }
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .background(Color.Transparent),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Graph", color = Color.White)
//                    }
//                }
//            }
//
//            Divider(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(1.dp),
//                color = Color.Gray
//            )
//
//            // Settings button
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(IntrinsicSize.Min)
//            ) {
//                DropdownMenuItem(
//                    modifier = Modifier.fillMaxSize(),
//                    onClick = { expandedSettingsMenu.value = true }
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Settings", color = Color.White)
//                    }
//                }
//            }
//
//            Divider(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(1.dp),
//                color = Color.Gray
//            )
//
//            // Add button
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(IntrinsicSize.Min)
//            ) {
//                DropdownMenuItem(
//                    modifier = Modifier.fillMaxSize(),
//                    onClick = { expandedSettingsMenu.value = true }
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Add", color = Color.White)
//                    }
//                }
//            }
//        }
//
//        // Dropdown menu (should be outside the Row)
//        DropdownMenu(
//            expanded = expandedSettingsMenu.value,
//            onDismissRequest = { expandedSettingsMenu.value = false }
//        ) {
//            DropdownMenuItem(onClick = {}) {
//                Text("Option 1")
//            }
//            DropdownMenuItem(onClick = {}) {
//                Text("Option 2")
//            }
//        }
//    }
//}