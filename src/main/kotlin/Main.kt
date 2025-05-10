import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.graph.*
import view.MainScreen
import viewModel.CircularPlacementStrategy
import viewModel.MainScreenViewModel
import java.io.File
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import java.awt.Dimension

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

private val DarkColorPalette = darkColors(
    primary = Color(206, 147, 216),
    primaryVariant = Color(123, 31, 162),
    secondary = Color(128, 203, 196),
//    onPrimary = Color.Black
)

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
    val icon: Painter = BitmapPainter(loadImageBitmap(File("src/main/kotlin/resources/icon.png").inputStream()))
    Window(
        onCloseRequest = ::exitApplication,
        title = "Graph",
        icon = icon,
        state = WindowState(width = 1200.dp, height = 700.dp)
    ) {
        window.minimumSize = Dimension(1000, 600)
        App()
    }
}
