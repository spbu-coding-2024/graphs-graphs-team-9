import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.graph.*
import view.MainScreen
import viewModel.CircularPlacementStrategy
import viewModel.MainScreenViewModel

val sampleGraph: Graph = GraphFactory.createUndirectedUnweightedGraph().apply {
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
@Preview
fun App() {

    MaterialTheme {
        MainScreen(MainScreenViewModel(sampleGraph, CircularPlacementStrategy()))
    }
//    var text by remember { mutableStateOf("Hello, World!") }
//
//    MaterialTheme {
//        Button(onClick = {
//            text = "Hello, Desktop!"
//        }) {
//            Text(text)
//        }
//    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
