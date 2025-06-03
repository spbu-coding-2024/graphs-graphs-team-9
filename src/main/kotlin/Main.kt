import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import view.MainScreen
import java.awt.Dimension



@Composable
@Preview
fun App() {

}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 1200.dp, height = 800.dp),
        title = "Bobr",
    ) {
        window.minimumSize = Dimension(900, 600)
        MaterialTheme {
            MainScreen()
        }
    }
}
