package handler

import EmulatedArduino
import util.SimpleLogger

/**
 * Handles button commands received from a serial connection.
 *
 * @property arduino The arduino instance to handle button commands for.
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class ButtonHandler(private val arduino: EmulatedArduino) {
    private var buttonOneState = false
    private var buttonTwoState = false

    /**
     * Processes a button command received from a serial connection.
     *
     * @param command The command to process.
     */
    fun processButtonCommand(command: String) {
        val parts = command.split(",")

        if (parts.size != 2) {
            SimpleLogger.warning("[${arduino.name}] Invalid button command: \"$command\"")
            return
        }

        SimpleLogger.info("[${arduino.name}] Button ${parts[0]} state: ${parts[1]}.")

        when (parts[0]) {
            "B1" -> buttonOneState = parts[1] == "1"
            "B2" -> buttonTwoState = parts[1] == "1"
            else -> SimpleLogger.warning("[${arduino.name}] Unknown button command: \"${parts[0]}\"")
        }
    }

    /**
     * Checks if a button is pressed, and performs the appropriate action.
     */
    fun checkButtons() {
        SimpleLogger.fine("[${arduino.name}] Checking buttons.")
        if (buttonOneState) {
            SimpleLogger.info("[${arduino.name}] Button 1 pressed.")
            arduino.messageHandler.forwardToAll("blink leds")
            buttonOneState = false
        }

        if (buttonTwoState) {
            SimpleLogger.info("[${arduino.name}] Button 2 pressed.")
            arduino.messageHandler.forwardToAllKnown("blink leds")
            buttonTwoState = false
        }
    }
}