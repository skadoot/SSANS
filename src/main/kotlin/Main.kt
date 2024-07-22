import fxml.Window
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import util.SimpleLogger
import java.util.logging.Level
import kotlin.system.exitProcess

class Main : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Main::class.java.getResource("fxml/window.fxml"))
        val root = fxmlLoader.load<VBox>()
        val mainWindowController = fxmlLoader.getController<Window>()

        stage.scene = Scene(root, 1280.0, 720.0)
        stage.title = "SSANS"
        stage.setOnCloseRequest {
            mainWindowController.shutdown()
            Platform.exit()
            exitProcess(0)
        }
        stage.show()

        SimpleLogger.setLevel(Level.FINE)
    }
}

fun main() {
    Application.launch(Main::class.java)
}