package handler

import EmulatedArduino
import util.SimpleLogger

/**
 * Handles messages received from a serial connection. This includes messages from both hardware and software serial connections.
 *
 * @property arduino The Arduino instance that this message handler is associated with.
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class MessageHandler(private val arduino: EmulatedArduino) {
    /**
     * Check the type of message received.
     *
     * @param port The port number the message was received on.
     * @param message The message received.
     */
    fun checkMessageType(port: Int, message: String): String {
        val type = message.split(",")[0]
        return if (type in listOf("RTS", "CTS", "MSG")) {
            type
        } else {
            SimpleLogger.warning("[${arduino.name}] Unknown message type: \"$type\"")
            ""
        }
    }

    /**
     * Handles the message received from a serial connection (hardware or software).
     *
     * @param port The port number the message was received on.
     * @param message The message received.
     * @return The type of message that was received (RTS, CTS, or MSG).
     */
    fun handleMessage(port: Int, message: String): String {
        val parts = message.split(",")

        when (parts[0]) {
            "PING" -> {
                SimpleLogger.info("[${arduino.name}] Responding to PING.")
                arduino.serialHandler.writeLine(port, "PONG")
                return ""
            }

            "MSG" -> {
                processMessage(port, message)
                return "MSG"
            }

            "CTS" -> {
                return "CTS"
            }

            "RTS" -> {
                return "RTS"
            }

            "B1", "B2" -> {
                processCommand(message)
                return ""
            }

            else -> {
                SimpleLogger.warning("[${arduino.name}] Unknown message type: \"${parts[0]}\"")
                return ""
            }
        }
    }

    /**
     * Processes the message received from a serial connection.
     *
     * @param port The port number the message was received on.
     * @param message The message received.
     */
    private fun processMessage(port: Int, message: String) {
        SimpleLogger.info("[${arduino.name}] Processing message: \"$message\" from port $port.")
        val parts = message.split(",")

        //val type = parts[0]
        val content = parts[1]
        val destination = parts[2]
        val sender = parts[3]

        if (sender != arduino.name && port != -1) {
            arduino.routingTable.setDevice(port, sender)
            SimpleLogger.info("[${arduino.name}] Updated routing table: ${arduino.routingTable.getTable()}")
        } else {
            SimpleLogger.info("[${arduino.name}] No routing table update needed.")
        }

        processCommand(content)

        when (destination) {
            arduino.name -> { // The message is for this Arduino.
                SimpleLogger.info("[${arduino.name}] Message is for this Arduino.")
            }

            "?" -> { // The message is for all known devices
                SimpleLogger.info("[${arduino.name}] Message is for all known devices.")
                forwardToAllKnown(content, port)
            }

            "" -> { // The message is for all devices
                SimpleLogger.info("[${arduino.name}] Message is for all devices.")
                forwardToAll(content, port)
            }
        }
    }

    /**
     * Forwards the message to all devices, regardless of whether they are known or not.
     *
     * @param content The content of the message to forward.
     */
    fun forwardToAll(content: String) {
        for (route in arduino.routingTable.getTable()) {
            arduino.serialHandler.sendMessage(
                "", content, "MSG", arduino.name, route.key
            )
        }
    }

    /**
     * Forwards the message to all devices, regardless of whether they are known or not, *except the sender*.
     *
     * @param content The content of the message to forward.
     * @param sender The port of the sender.
     */
    private fun forwardToAll(content: String, sender: Int) {
        for (route in arduino.routingTable.getTable()) {
            if (route.key != sender) {
                arduino.serialHandler.sendMessage(
                    "", content, "MSG", arduino.name, route.key
                )
            }
        }
    }

    /**
     * Forwards the message to all known devices.
     *
     * @param content The content of the message to forward.
     */
    fun forwardToAllKnown(content: String) {
        for (route in arduino.routingTable.getTable()) {
            if (route.value != "UNKNOWN") {
                arduino.serialHandler.sendMessage(
                    "", content, "MSG", arduino.name, route.key
                )
            }
        }
    }

    /**
     * Forwards the message to all known devices, *except the sender*.
     *
     * @param content The content of the message to forward.
     * @param sender The port of the sender.
     */
    fun forwardToAllKnown(content: String, sender: Int) {
        for (route in arduino.routingTable.getTable()) {
            if (route.value != "UNKNOWN" && route.key != sender) {
                arduino.serialHandler.sendMessage(
                    "", content, "MSG", arduino.name, route.key
                )
            }
        }
    }

    /**
     * Processes the command received from a message.
     *
     * @param command The command to process.
     */
    private fun processCommand(command: String) {
        SimpleLogger.info("[${arduino.name}] Processing command: $command")
        if (command == "reset") {
            SimpleLogger.info("[${arduino.name}] Resetting.")
            arduino.routingTable.clear()
            arduino.ledHandler.blinkLeds(1, 2000)
        } else if (command.contains("B1") || command.contains("B2")) {
            SimpleLogger.info("[${arduino.name}] Processing button command.")
            arduino.buttonHandler.processButtonCommand(command)
        } else { // Assume it is an LED command. This is not a safe assumption. The LED handler will ignore unknown commands.
            SimpleLogger.info("[${arduino.name}] Assuming and processing LED command.")
            arduino.ledHandler.processLedCommand(command)
        }
    }

    /**
     * Sends an RTS message to the specified port.
     *
     * @param port The port to send the message on.
     */
    fun sendRTS(port: Int) {
        SimpleLogger.finest("[${arduino.name}] Sending RTS port $port.")
        arduino.serialHandler.writeLine(port, "RTS\n")
    }

    /**
     * Sends a CTS message to the specified port.
     *
     * @param port The port to send the message on.
     */
    fun sendCTS(port: Int) {
        SimpleLogger.info("[${arduino.name}] Sending CTS to port $port.")
        arduino.serialHandler.writeLine(port, "CTS\n")
    }
}