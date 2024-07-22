import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * This class is used to emulate a serial port. Can be connected to another EmulatedSerial object to simulate a serial
 * connection. Emulates full-duplex communication.
 *
 * This class is essentially a wrapper around paired PipedInputStreams and PipedOutputStreams.
 *
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class EmulatedSerial {
    private var outputStream: PipedOutputStream = PipedOutputStream()
    private var inputStream: PipedInputStream = PipedInputStream()
    private var isConnected = false

    /**
     * Connects this EmulatedSerial object to another EmulatedSerial object.
     *
     * @param other The other EmulatedSerial object to connect with.
     * @return Boolean value indicating the success of the connection.
     */
    fun connect(other: EmulatedSerial): Boolean {
        require(!isConnected) { "Serial port is already connected" }
        require(!other.isConnected) { "Other serial port is already connected" }
        require(this != other) { "Cannot connect serial port to itself" }

        try {
            // Attempt to connect the output stream of this EmulatedSerial object to the input stream of the other EmulatedSerial object.
            outputStream.connect(other.inputStream)
            inputStream.connect(other.outputStream)

            // Set the connection status of both EmulatedSerial objects to true.
            isConnected = true
            other.isConnected = true

            // Confirm the connection was successful.
            return true
        } catch (e: IOException) {
            throw IOException("Failed to connect serial ports: $e")
        }
    }

    /**
     * Checks if the EmulatedSerial object is connected to another EmulatedSerial object.
     *
     * @return Boolean value indicating the connection status.
     */
    fun isConnected(): Boolean {
        return isConnected
    }

    /**
     * Write the specified data to the output stream of the EmulatedSerial object.
     *
     * @param data The ByteArray of data to write to the output stream.
     */
    fun writeBytes(data: ByteArray) {
        outputStream.write(data)
    }

    /**
     * Read data from the input stream of the EmulatedSerial object.
     *
     * @param b The number of bytes to read.
     * @return The data read from the input stream.
     */
    fun readBytes(b: Int): ByteArray {
        val buffer = ByteArray(b)
        inputStream.read(buffer)
        return buffer
    }

    /**
     * Close the input and output streams of this EmulatedSerial object. The object should be discarded after calling
     * this function.
     */
    fun close() {
        inputStream.close()
        outputStream.close()
        isConnected = false
    }

    /**
     * Return the number of bytes available to read from the input stream of the EmulatedSerial object.
     *
     * @return The number of bytes available to read.
     */
    fun available(): Int {
        return inputStream.available()
    }

    /**
     * Flush the output stream of the EmulatedSerial object.
     */
    fun flush() {
        outputStream.flush()
    }

    /**
     * Clear the input stream of the EmulatedSerial object.
     */
    fun clear() {
        inputStream.skip(inputStream.available().toLong())
    }
}