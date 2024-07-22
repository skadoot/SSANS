import handler.ButtonHandler
import handler.LedHandler
import handler.MessageHandler
import handler.SerialHandler
import routing.RoutingTable
import util.SimpleLogger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class EmulatedArduino(
    val name: String = ByteArray(10).map { ('a'..'z').random() }
        .joinToString("") // Randomly generate a name for the Arduino if one is not provided.
) : Runnable {
    /* ========== Handlers ========== */
    val routingTable = RoutingTable()
    val messageHandler = MessageHandler(this)
    val buttonHandler = ButtonHandler(this)
    val ledHandler = LedHandler(this)
    var serialHandler = SerialHandler(this)

    /* ========== Running flags ========== */
    private var running = AtomicBoolean(false)
    private var paused = AtomicBoolean(false)
    private var step = AtomicBoolean(false)
    private var delay = AtomicLong(0)

    /**
     * Connects the given serial object to the hardware serial.
     */
    fun connectSerial(other: EmulatedSerial) {
        synchronized(serialHandler) {
            serialHandler.connectSerial(other)

            if (serialHandler.getHardwareSerial().isConnected() && other.isConnected()) {
                SimpleLogger.info("[${this.name}] New hardware serial connection established.")
            } else {
                SimpleLogger.warning("[${this.name}] Failed to establish hardware serial connection.")
            }
        }
    }

    /**
     * Connects the given serial object to the specified port.
     */
    fun connectSerial(port: Int, other: EmulatedSerial) {
        synchronized(serialHandler) {
            serialHandler.connectSerial(port, other)

            if (serialHandler.getSoftwareSerial(port).isConnected() && other.isConnected()) {
                SimpleLogger.info("[${this.name}] New software serial connection on port $port established.")
            } else {
                SimpleLogger.warning("[${this.name}] Failed to establish software serial connection on port $port.")
            }
        }
    }

    override fun run() {
        running.set(true)
        SimpleLogger.info("[${this.name}] started.")

        while (running.get()) {
            checkRunning()
            buttonHandler.checkButtons()
            serialHandler.checkHardwareSerial()
            serialHandler.checkSoftwareSerial()
        }

        SimpleLogger.info("[${this.name}] Arduino emulation stopped.")
    }

    /**
     * Checks if the Arduino is running, and if it is paused, waits for the pause to be lifted.
     * Provides debugger-style stepping functionality.
     */
    fun checkRunning() {
        if (!running.get()) {
            Thread.currentThread().interrupt()
        }

        Thread.sleep(delay.toLong())

        if (paused.get()) {
            SimpleLogger.info("[${this.name}] Paused.")
            while (paused.get()) {
                Thread.sleep(100)
                if (step.get()) {
                    SimpleLogger.info("[${this.name}] Stepping.")
                    step.set(false)
                    break
                }
            }
        }
    }

    /**
     * Stops the Arduino emulation.
     */
    fun stop() {
        running.set(false)
    }

    /**
     * Pauses the Arduino emulation.
     */
    fun pause() {
        SimpleLogger.info("[${this.name}] Trying to pause.")
        paused.set(true)
    }

    /**
     * Resumes the Arduino emulation.
     */
    fun resume() {
        paused.set(false)
    }

    /**
     * Steps through the Arduino emulation.
     */
    fun step() {
        step.set(true)
    }
}