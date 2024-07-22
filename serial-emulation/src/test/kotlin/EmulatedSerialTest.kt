import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmulatedSerialTest {
    private lateinit var emulatedSerialOne: EmulatedSerial
    private lateinit var emulatedSerialTwo: EmulatedSerial

    @BeforeEach
    @Test
    fun setUp() {
        emulatedSerialOne = EmulatedSerial()
        emulatedSerialTwo = EmulatedSerial()
    }

    @AfterEach
    @Test
    fun tearDown() {
        emulatedSerialOne.close()
        emulatedSerialTwo.close()

        assertFalse { emulatedSerialOne.isConnected() }
        assertFalse { emulatedSerialTwo.isConnected() }
    }


    @Test
    fun `should connect two emulated serial objects`() {
        emulatedSerialOne.connect(emulatedSerialTwo)

        assertTrue(emulatedSerialOne.isConnected())
        assertTrue(emulatedSerialTwo.isConnected())
    }

    @Test
    fun `should disconnect two emulated serial objects`() {
        emulatedSerialOne.connect(emulatedSerialTwo)
        emulatedSerialOne.close()
        emulatedSerialTwo.close()

        assertFalse(emulatedSerialOne.isConnected())
        assertFalse(emulatedSerialTwo.isConnected())
    }

    @Test
    fun `should write and read bytes between two connected emulated serial objects`() {
        emulatedSerialOne.connect(emulatedSerialTwo)

        val data = "Hello, World!".toByteArray()
        emulatedSerialOne.writeBytes(data)

        val readData = emulatedSerialTwo.readBytes(data.size)

        assertArrayEquals(data, readData)
    }
}