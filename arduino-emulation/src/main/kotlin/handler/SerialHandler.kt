package handler

import EmulatedArduino
import EmulatedSerial
import util.SimpleLogger

/**
 * Handles the serial communication between the Arduino and the control program.
 *
 * @param arduino The Arduino object to handle the serial communication for.
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class SerialHandler(private val arduino: EmulatedArduino) {
    // The hardware serial is the serial connection between the Arduino and the computer.
    private var hardwareSerial = EmulatedSerial()

    /* ========== Delay/timeout variables for tuning ========== */
    private var checkSoftwarePortDelay = 500L // Delay before checking a software serial port.
    private var afterCtsDelay = 500L // Delay to check for MSG after sending a CTS.
    private var afterRtsDelay = 500L // Delay to check for CTS after sending an RTS.
    private var sendCtsAttempts = 20 // Try sending a CTS message this many times before giving up.
    private var sendRtsAttempts = 20 // Try sending an RTS message this many times before giving up.

    // The software serials are stored in a map, where the key is the port number and the value is the serial object.
    private var softwareSerials: MutableMap<Int, EmulatedSerial> = run {
        val map = mutableMapOf<Int, EmulatedSerial>()
        for (i in 0..3) {
            map[i] = EmulatedSerial()
        }
        map
    }

    /**
     * Connects the given serial object to the hardware serial. If the Arduino is already connected to a serial object,
     * the current connection is closed and the new connection is established.
     *
     * @param other The serial object to connect to the hardware serial.
     * @return True if the connection was successful, false otherwise.
     */
    fun connectSerial(other: EmulatedSerial): Boolean {
        // If the hardware serial is already connected, close it first, then connect the new serial object.
        if (hardwareSerial.isConnected()) {
            hardwareSerial.close()
            hardwareSerial = EmulatedSerial()
        }
        return hardwareSerial.connect(other)
    }

    /**
     * Connects the given serial object to the specified port.
     *
     * @param port The port number to connect the serial object to.
     * @param other The serial object to connect to the specified port.
     */
    fun connectSerial(port: Int, other: EmulatedSerial): Boolean {
        // If the software serial is already connected, close it first, then connect the new serial object.
        if (softwareSerials[port]?.isConnected() == true) {
            softwareSerials[port]!!.close()
            softwareSerials[port] = EmulatedSerial()
        }
        return softwareSerials[port]!!.connect(other)
    }

    /**
     * Disconnects the hardware serial.
     */
    fun disconnectHardwareSerial() {
        hardwareSerial.close()
        hardwareSerial = EmulatedSerial()
    }

    /**
     * Disconnects the software serial on the specified port.
     *
     * @param port The port number to disconnect the serial from.
     */
    fun disconnectSoftwareSerial(port: Int) {
        SimpleLogger.info("[${arduino.name}] Disconnecting software serial on port $port.")
        softwareSerials[port]!!.close()
        softwareSerials[port] = EmulatedSerial()
    }

    /**
     * Get the hardware serial object.
     */
    fun getHardwareSerial(): EmulatedSerial {
        return hardwareSerial
    }

    /**
     * Get the software serial object for the specified port.
     *
     * @param port The port number to get the serial object for.
     */
    fun getSoftwareSerial(port: Int): EmulatedSerial {
        return softwareSerials[port]!!
    }

    /**
     * Writes a line to the hardware serial. If the serial is not connected, the line is not written.
     *
     * @param line The line to write to the serial.
     */
    fun writeLine(line: String) {
        SimpleLogger.fine("[${arduino.name}] Writing line to hardware serial: $line")
        if (hardwareSerial.isConnected()) {
            hardwareSerial.writeBytes((line).toByteArray())
        } else {
            SimpleLogger.warning("[${arduino.name}] Hardware serial is not connected.")
        }
    }

    /**
     * Writes a line to the specified software serial port. If the serial is not connected, the line is not written.
     *
     * @param port The port number to write the line to.
     * @param line The line to write to the serial.
     */
    fun writeLine(port: Int, line: String) {
        SimpleLogger.fine("[${arduino.name}] Writing line to software serial on port $port: $line")
        if (port == -1) {
            hardwareSerial.writeBytes((line).toByteArray())
        } else if (softwareSerials[port]!!.isConnected()) {
            softwareSerials[port]!!.writeBytes(line.toByteArray())
        } else {
            SimpleLogger.fine("[${arduino.name}] Software serial on port $port is not connected.")
        }
    }

    /**
     * Reads a line from the hardware serial. If the serial is not connected, an empty string is returned. Reads until
     * a newline character is encountered.
     *
     * @return The line read from the serial.
     */
    private fun readLine(): String {
        if (hardwareSerial.isConnected()) {
            var c = hardwareSerial.readBytes(1)[0]
            var line = ""
            while (c != '\n'.code.toByte()) {
                line += c.toInt().toChar()
                c = hardwareSerial.readBytes(1)[0]
            }
            return line
        } else {
            SimpleLogger.warning("[${arduino.name}] Hardware serial is not connected.")
            return ""
        }
    }

    /**
     * Reads a line from the specified software serial port. If the serial is not connected, an empty string is returned.
     * Read until a newline character is encountered.
     *
     * @param port The port number to read the line from.
     * @return The line read from the serial.
     */
    private fun readLine(port: Int): String {
        if (softwareSerials[port]!!.isConnected()) {
            if (softwareSerials[port]!!.available() > 0) {
                SimpleLogger.info("[${arduino.name}] Reading line from software serial on port $port.")
                var c = softwareSerials[port]!!.readBytes(1)[0]
                var line = ""
                while (c != "\n".toByteArray()[0]) {
                    line += c.toInt().toChar()
                    c = softwareSerials[port]!!.readBytes(1)[0]
                    SimpleLogger.info("[${arduino.name}] Read next byte: $c")
                }
                return line
            } else {
                return ""
            }
        } else {
            SimpleLogger.info("[${arduino.name}] Software serial on port $port is not connected.")
            return ""
        }
    }

    /**
     * Checks the software serial ports for incoming data. If data is available, the message is read and handled.
     */
    fun checkSoftwareSerial() {
        softwareSerials.forEach { port ->
            arduino.ledHandler.showLed(port.key, true)
            SimpleLogger.fine("[${arduino.name}] Checking software serial on port ${port.key}")

            softwareSerials[port.key]?.clear()
            Thread.sleep(checkSoftwarePortDelay)

            if ((softwareSerials[port.key]?.available() ?: 0) > 0) {
                arduino.ledHandler.showReceiving(true)
                val message = readLine(port.key)
                SimpleLogger.info("[${arduino.name}] Received message on port ${port.key}: $message")

                if (message == "RTS") {
                    SimpleLogger.info("[${arduino.name}] Received RTS message on port ${port.key}.")
                    arduino.serialHandler.softwareSerials[port.key]?.flush()
                    arduino.serialHandler.softwareSerials[port.key]?.clear()
                    arduino.messageHandler.sendCTS(port.key)

                    Thread.sleep(afterCtsDelay)

                    if ((softwareSerials[port.key]?.available() ?: 0) > 0) {
                        val newMessage = readLine(port.key)
                        arduino.messageHandler.handleMessage(port.key, newMessage)
                    } else {
                        SimpleLogger.info("[${arduino.name}] No message received after CTS on port ${port.key}.")
                    }
                } else {
                    // This shouldn't happen, since a message should only be sent if the RTS/CTS exchange is successful.
                    SimpleLogger.info("[${arduino.name}] Message received on port ${port.key} was not RTS, disregarding.")
                }
            }
            arduino.ledHandler.showLed(port.key, false)
        }
    }

    /**
     * Checks the hardware serial for incoming data. If data is available, the message is read and handled.
     */
    fun checkHardwareSerial() {
        SimpleLogger.finest("[${arduino.name}] Checking hardware serial.")
        if (hardwareSerial.isConnected()) {
            Thread.sleep(checkSoftwarePortDelay)
            // Check if there is data available to read.
            if (hardwareSerial.available() > 0) {
                SimpleLogger.fine("[${arduino.name}] Data available on hardware serial.")
                arduino.messageHandler.handleMessage(-1, readLine())
            } else {
                SimpleLogger.fine("[${arduino.name}] No data available on hardware serial.")
            }
        }
    }

    /**
     * Creates and sends a message to the specified destination. Handles the RTS/CTS exchange to ensure that the message
     * is sent correctly.
     *
     * @param destination The destination of the message.
     * @param message The message to send.
     * @param type The type of message to send.
     * @param sender The sender of the message.
     * @param port The port to send the message on.
     * @return Whether the message was sent successfully.
     */
    fun sendMessage(destination: String, message: String, type: String, sender: String, port: Int): Boolean {
        SimpleLogger.finer("[${arduino.name}] Attempting to send message to $destination on port $port.")
        val fullMessage = "$type,$message,$destination,$sender\n" // Create the full message to send.
        var sent = false
        var attempts = sendRtsAttempts

        arduino.ledHandler.showLed(port, true) // Turn on the LED for the port.
        do {
            softwareSerials[port]?.clear() // Clear the serial buffer.
            softwareSerials[port]?.flush() // Flush the serial buffer.

            arduino.messageHandler.sendRTS(port) // Send the RTS message.
            Thread.sleep(afterRtsDelay) // Wait for the CTS message.

            if ((softwareSerials[port]?.available() ?: 0) > 0) {
                val response = readLine(port) // Read the response from the serial.

                if (response == "CTS") {
                    SimpleLogger.info("[${arduino.name}] Received CTS message on port $port. Writing message.")
                    writeLine(port, fullMessage) // Write the message to the serial.
                    sent = true
                } else {
                    SimpleLogger.warning("[${arduino.name}] Unexpected response to RTS message on port $port: $response. Discarding.")
                }
            } else {
                SimpleLogger.finer("[${arduino.name}] No response to RTS message on port $port. Retrying.")
            }

            attempts--
        } while (!sent && attempts > 0)

        arduino.ledHandler.showLed(port, false)
        return sent
    }
}
