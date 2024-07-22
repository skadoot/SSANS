package handler

import EmulatedArduino
import util.SimpleLogger

/**
 * A handler for the LED display on the emulated Arduino. Relays commands to the serial handler, which sends them to the
 * connected program to display the LED state.
 *
 * @param arduino The emulated Arduino instance.
 * @property showListening Whether to show the listening LEDs.
 * @property showSending Whether to show the sending LEDs.
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class LedHandler(private val arduino: EmulatedArduino) {
    private var showListening = true
    private var showSending = true

    /**
     * Sets the state of the corresponding LED on the LED display.
     * - 0: Pink
     * - 1: White
     * - 2: Green
     * - 3: Blue
     * - 4: Red (TX)
     * - 5: Yellow (RX)
     *
     * @param port The port number to show.
     * @param state The state to set the LED to.
     */
    fun showLed(port: Int, state: Boolean) {
        if (showListening) {
            if (port < 0 || port > 3) {
                return
            } else {
                arduino.serialHandler.writeLine("LED,$port,${if (state) "1" else "0"}\n")
            }
        }
    }

    /**
     * Processes an LED command received from a serial connection.
     *
     * @param command The command to process.
     */
    fun processLedCommand(command: String) {
        when (command) {
            "all on", "all off" -> for (i in 0..3) showLed(i, command == "all on")
            "cycle leds" -> cycleLeds()
            "blink leds" -> blinkLeds(10, 500)
            "show listen on", "show listen off" -> setShowListening(command == "show listen on")
            "show send on", "show send off" -> setShowSending(command == "show send on")
            else -> {
                val colorCommands = mapOf(
                    "pink" to 0, "white" to 1, "green" to 2, "blue" to 3
                )
                val color = command.split(" ")[0]
                val state = command.split(" ")[1] == "on"
                colorCommands[color]?.let { showLed(it, state) }
            }
        }
    }

    /**
     * Cycle through the LEDs.
     */
    private fun cycleLeds() {
        for (i in 0..3) {
            showLed(i, true)
            Thread.sleep(100)
        }
        for (i in 0..3) {
            showLed(i, false)
            Thread.sleep(100)
        }
    }

    /**
     * Blink the LEDs. Enable all LEDs together for the specified number of times with the specified delay between each blink.
     *
     * @param num The number of times to blink the LEDs.
     * @param delay The delay between each blink.
     */
    fun blinkLeds(num: Int, delay: Int) {
        SimpleLogger.info("[${arduino.name}] Blinking LEDs $num times with a delay of $delay ms.")
        for (i in 0..num) {
            for (j in 0..4) {
                showLed(j, true)
            }

            Thread.sleep(delay.toLong())

            for (j in 0..4) {
                showLed(j, false)
            }

            Thread.sleep(delay.toLong())
        }
    }

    /**
     * Sets the state of the TX LED.
     *
     * @param state The state to set the LED to.
     */
    fun showTransmitting(state: Boolean) {
        showLed(4, state)
    }

    /**
     * Sets the state of the RX LED.
     *
     * @param state The state to set the LED to.
     */
    fun showReceiving(state: Boolean) {
        showLed(5, state)
    }

    /**
     * Sets the state of the listening LEDs.
     *
     * @param show The state to set the LEDs to.
     */
    private fun setShowListening(show: Boolean) {
        if (!show) {
            for (i in 0..3) {
                showLed(i, false)
            }
        }
        showListening = show
    }

    /**
     * Sets the state of the sending LEDs.
     *
     * @param show The state to set the LEDs to.
     */
    private fun setShowSending(show: Boolean) {
        if (!show) {
            for (i in 4..5) {
                showLed(i, false)
            }
        }
        showSending = show
    }

    /**
     * Gets the state of the show listening boolean.
     *
     * @return The state of the show listening boolean.
     */
    fun getShowListening(): Boolean {
        return showListening
    }

    /**
     * Gets the state of the show sending boolean.
     *
     * @return The state of the show sending boolean.
     */
    fun getShowSending(): Boolean {
        return showSending
    }
}