package fxml

import EmulatedArduino
import EmulatedSerial
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.TextInputDialog
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.text.Text
import util.SimpleLogger

class Arduino {
    lateinit var gridPane: GridPane  // The main grid pane that contains the Arduino components
    lateinit var name: Text // The name of the Arduino device

    // LED indicators
    lateinit var txLed: Circle // Red (Transmit)
    lateinit var rxLed: Circle // Yellow (Receive)
    lateinit var pinOneLed: Circle // Pink
    lateinit var pinTwoLed: Circle // White
    lateinit var pinThreeLed: Circle // Green
    lateinit var pinFourLed: Circle // Blue

    // Pin connectors
    lateinit var pinOne: Arc
    lateinit var pinTwo: Arc
    lateinit var pinThree: Arc
    lateinit var pinFour: Arc

    // For connecting devices
    var selectedPin: Int? = null

    // Buttons
    lateinit var buttonOne: Circle
    lateinit var buttonTwo: Circle

    // Emulated Arduino device
    lateinit var arduino: EmulatedArduino
    var serial: EmulatedSerial = EmulatedSerial()

    // Running flag
    private var running = true

    // Arduino's emulation thread
    private lateinit var emulationThread: Thread

    // Listener thread that listens for serial data
    private lateinit var listenerThread: Thread

    // Keep track of the connected lines on the GUI
    val connectedLines = mutableListOf<Line>()

    /**
     * Initializes the Arduino device. This method should be automatically called after the FXML file has been loaded.
     */
    fun initialize() {
        requestDeviceName()
        addDragListener(gridPane)
        addButtonListeners()
        addPinListeners()

        arduino = EmulatedArduino(name.text) // Create a new EmulatedArduino object
        arduino.connectSerial(serial) // Connect the EmulatedArduino object to the EmulatedSerial object

        // Start listening for serial data
        listenerThread = Thread { serialListener(serial) }
        listenerThread.start()

        // Start the emulation thread
        emulationThread = Thread { arduino.run() }
        emulationThread.start()
    }

    /**
     * Listener function for the hardware serial connection.
     *
     * @param es the EmulatedSerial object to listen to
     */
    private fun serialListener(es: EmulatedSerial) {
        while (running) {
            if (es.available() > 0) {
                var c = es.readBytes(1)[0]
                var line = ""
                while (c != '\n'.code.toByte()) {
                    line += c.toInt().toChar()
                    c = es.readBytes(1)[0]
                }
                processSerialData(line)
            } else {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    // This case is likely to occur when the program is shutting down or removing the device from the scene, and is not an error
                    break
                }
            }
        }
    }

    /**
     * Makes a node draggable by adding mouse listeners to the given node.
     *
     * @param node the node to make draggable
     */
    private fun addDragListener(node: Node) {
        val dragContext = object {
            var x = 0.0
            var y = 0.0
        }

        node.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            dragContext.x = node.layoutX - event.sceneX
            dragContext.y = node.layoutY - event.sceneY
        }

        node.addEventFilter(MouseEvent.MOUSE_DRAGGED) { event ->
            node.layoutX = dragContext.x + event.sceneX
            node.layoutY = dragContext.y + event.sceneY
        }
    }

    /**
     * Adds listeners to the buttons on the Arduino device.
     */
    private fun addButtonListeners() {
        buttonOne.setOnMousePressed { serial.writeBytes("B1,1\n".toByteArray()) }
        buttonOne.setOnMouseReleased { serial.writeBytes("B1,0\n".toByteArray()) }
        buttonTwo.setOnMousePressed { serial.writeBytes("B2,1\n".toByteArray()) }
        buttonTwo.setOnMouseReleased { serial.writeBytes("B2,0\n".toByteArray()) }
    }

    /**
     * Adds listeners to the pins on the Arduino device.
     */
    private fun addPinListeners() {
        listOf(pinOne, pinTwo, pinThree, pinFour).forEachIndexed { index, pin ->
            pin.setOnMousePressed {
                selectedPin = index
            }
        }
    }

    /**
     * Requests the user to enter a name for the Arduino device.
     */
    private fun requestDeviceName() {
        val dialog = TextInputDialog("")
        dialog.title = "Add New Device"
        dialog.headerText = "Please name your new device. Maximum 10 characters."
        dialog.contentText = "Name:"
        dialog.graphic = null
        val result = dialog.showAndWait()
        name.text = result.orElse("Device").take(10)
    }

    /**
     * Processes the serial data received from the EmulatedSerial object.
     *
     * @param data the serial data to process
     */
    private fun processSerialData(data: String) {
        val parts = data.split(",")
        if (parts[0] == "LED") {
            Platform.runLater { setLedState(parts[1].toInt(), parts[2] == "1") }
        }
    }

    /**
     * Sets the state of the LED at the specified index. The index corresponds to the following LEDs:
     * 0: Pin 1 LED (Pink)
     * 1: Pin 2 LED (White)
     * 2: Pin 3 LED (Green)
     * 3: Pin 4 LED (Blue)
     * 4: TX LED (Red)
     * 5: RX LED (Yellow)
     *
     * @param ledIndex the index of the LED to set the state of
     * @param state the state of the LED (true for on, false for off)
     */
    private fun setLedState(ledIndex: Int, state: Boolean) {
        when (ledIndex) {
            0 -> pinOneLed.fill = if (state) Color.PINK else Color.GREY
            1 -> pinTwoLed.fill = if (state) Color.WHITE else Color.GREY
            2 -> pinThreeLed.fill = if (state) Color.GREEN else Color.GREY
            3 -> pinFourLed.fill = if (state) Color.BLUE else Color.GREY
            4 -> txLed.fill = if (state) Color.RED else Color.GREY
            5 -> rxLed.fill = if (state) Color.YELLOW else Color.GREY
        }
    }

    /**
     * Returns the Arc object corresponding to the given pin.
     *
     * @param pinNumber the pin (0-3)
     */
    fun getPinByNumber(pinNumber: Int): Arc {
        return when (pinNumber) {
            0 -> pinOne
            1 -> pinTwo
            2 -> pinThree
            3 -> pinFour
            else -> throw IllegalArgumentException("Invalid pin number: $pinNumber")
        }
    }

    /**
     * Pauses the Arduino device.
     */
    fun pause() {
        SimpleLogger.info("[${this.name}] Trying to pause.")
        arduino.pause()
    }

    /**
     * Resumes the Arduino device.
     */
    fun resume() {
        arduino.resume()
    }

    /**
     * Steps through the Arduino device's execution.
     */
    fun step() {
        arduino.step()
    }

    /**
     * Shuts down the Arduino device.
     */
    fun shutdown() {
        running = false
        emulationThread.interrupt()
        listenerThread.interrupt()
    }
}