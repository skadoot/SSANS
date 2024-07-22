package fxml

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.shape.Arc
import javafx.scene.shape.Line
import util.SimpleLogger

class Window {
    lateinit var resumeBtn: Button
    lateinit var stepBtn: Button
    lateinit var statusLabel: Label
    lateinit var pauseBtn: Button

    @FXML
    lateinit var networkPane: Pane

    // List of all Arduino elements in the network
    private var arduinoMap = mutableMapOf<String, Arduino>()

    // For connecting devices
    private var selectedArduino: Arduino? = null

    @FXML
    fun initialize() {
        networkPane.setOnContextMenuRequested { event ->
            val addDeviceItem = MenuItem("Add Device").apply { setOnAction { addDevice(event.x, event.y) } }
            ContextMenu(addDeviceItem).show(networkPane, event.screenX, event.screenY)
        }
        addConfigListeners()
    }

    private fun addDevice(x: Double, y: Double) {
        val loader = FXMLLoader(javaClass.getResource("/fxml/arduino.fxml"))
        val node = loader.load<GridPane>()
        val arduinoController = loader.getController<Arduino>()

        arduinoController.gridPane.layoutX = x
        arduinoController.gridPane.layoutY = y

        arduinoMap[arduinoController.name.text] = arduinoController

        node.setOnContextMenuRequested { event ->
            val removeDeviceItem = MenuItem("Remove Device").apply {
                setOnAction {
                    arduinoController.connectedLines.forEach {
                        networkPane.children.remove(it)
                    }
                    arduinoController.connectedLines.clear()
                    arduinoMap[arduinoController.name.text]?.shutdown()
                    networkPane.children.remove(node)
                }
            }
            ContextMenu(removeDeviceItem).show(node, event.screenX, event.screenY)
            event.consume() // Stops the event from propagating to the parent (networkPane)
        }

        // Set the selectedArduino property when a pin is clicked
        arduinoController.gridPane.childrenUnmodifiable.filterIsInstance<Arc>().forEach {
            it.setOnMouseClicked {
                if (selectedArduino == null) {
                    selectedArduino = arduinoController
                } else if (selectedArduino != arduinoController && selectedArduino?.selectedPin != null && arduinoController.selectedPin != null) {
                    connectDevices(
                        selectedArduino!!,
                        selectedArduino!!.selectedPin!!,
                        arduinoController,
                        arduinoController.selectedPin!!
                    )
                    selectedArduino = null
                }
            }
        }

        networkPane.children.add(node)
    }

    /**
     * Connects two Arduino devices.
     *
     * @param arduinoOne The first Arduino device
     * @param pinOne The pin on the first Arduino device
     * @param arduinoTwo The second Arduino device
     * @param pinTwo The pin on the second Arduino device
     */
    private fun connectDevices(arduinoOne: Arduino, pinOne: Int, arduinoTwo: Arduino, pinTwo: Int) {
        arduinoOne.arduino.connectSerial(pinOne, arduinoTwo.arduino.serialHandler.getSoftwareSerial(pinTwo))
        SimpleLogger.info("[WINDOW] Connected [${arduinoOne.name.text}] pin $pinOne to [${arduinoTwo.name.text}] pin $pinTwo")
        drawLine(arduinoOne, pinOne, arduinoTwo, pinTwo)
    }

    /**
     * Draws a line between two pins on two Arduino devices, and updates the line's position when the devices are moved.
     *
     * @param arduinoOne The first Arduino device
     * @param pinOne The pin on the first Arduino device
     * @param arduinoTwo The second Arduino device
     * @param pinTwo The pin on the second Arduino device
     */
    private fun drawLine(arduinoOne: Arduino, pinOne: Int, arduinoTwo: Arduino, pinTwo: Int) {
        val line = Line()
        val startPin = arduinoOne.getPinByNumber(pinOne)
        val endPin = arduinoTwo.getPinByNumber(pinTwo)
        line.startX = arduinoOne.gridPane.layoutX + startPin.layoutX
        line.startY = arduinoOne.gridPane.layoutY + startPin.layoutY
        line.endX = arduinoTwo.gridPane.layoutX + endPin.layoutX
        line.endY = arduinoTwo.gridPane.layoutY + endPin.layoutY

        networkPane.children.add(line)

        arduinoOne.connectedLines.add(line)
        arduinoTwo.connectedLines.add(line)

        arduinoOne.gridPane.layoutXProperty().addListener { _, _, _ ->
            line.startX = arduinoOne.gridPane.layoutX + startPin.layoutX
            line.startY = arduinoOne.gridPane.layoutY + startPin.layoutY
        }
        arduinoOne.gridPane.layoutYProperty().addListener { _, _, _ ->
            line.startX = arduinoOne.gridPane.layoutX + startPin.layoutX
            line.startY = arduinoOne.gridPane.layoutY + startPin.layoutY
        }
        arduinoTwo.gridPane.layoutXProperty().addListener { _, _, _ ->
            line.endX = arduinoTwo.gridPane.layoutX + endPin.layoutX
            line.endY = arduinoTwo.gridPane.layoutY + endPin.layoutY
        }
        arduinoTwo.gridPane.layoutYProperty().addListener { _, _, _ ->
            line.endX = arduinoTwo.gridPane.layoutX + endPin.layoutX
            line.endY = arduinoTwo.gridPane.layoutY + endPin.layoutY
        }
    }

    /**
     * Adds listeners to the control buttons in the configuration pane.
     */
    fun addConfigListeners() {
        resumeBtn.setOnAction {
            arduinoMap.values.forEach { it.resume() }
        }

        stepBtn.setOnAction {
            arduinoMap.values.forEach { it.step() }
        }

        pauseBtn.setOnAction {
            arduinoMap.values.forEach { it.pause() }
        }
    }

    fun shutdown() {
        arduinoMap.values.forEach { it.shutdown() }
    }
}