import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.graph.Graph

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }

    var graph = Graph()
    graph.addVertex(1, "A")
    graph.addVertex(2, "B")
    graph.addVertex(3, "C")
    graph.addVertex(4, "D")
    graph.addVertex(5, "E")
    graph.addVertex(6, "F")
    graph.addVertex(7, "G")

    graph.addEdge(1, 2, 1.0)
    graph.addEdge(1, 2, 1.0)
    graph.addEdge(1, 2, 1.0)
    graph.addEdge(1, 3, 2.0)
    graph.addEdge(1, 4, 3.0)
    graph.addEdge(1, 5, 4.0)
    graph.addEdge(1, 6, 5.0)
    graph.addEdge(1, 7, 6.0)

    graph.printG()
}
