package ireader.domain.usecases.sync.pairing

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GeneratePairingPinUseCaseTest {

    @Test
    fun `invoke should generate 6-digit PIN`() = runTest {
        // Arrange
        val useCase = GeneratePairingPinUseCase()

        // Act
        val pin = useCase()

        // Assert
        assertEquals(6, pin.length, "PIN should be exactly 6 digits")
        assertTrue(pin.all { it.isDigit() }, "PIN should contain only digits")
    }

    @Test
    fun `invoke should generate PIN within valid range`() = runTest {
        // Arrange
        val useCase = GeneratePairingPinUseCase()

        // Act
        val pin = useCase()
        val pinValue = pin.toInt()

        // Assert
        assertTrue(pinValue >= 100000, "PIN should be >= 100000")
        assertTrue(pinValue <= 999999, "PIN should be <= 999999")
    }

    @Test
    fun `invoke should generate different PINs on multiple calls`() = runTest {
        // Arrange
        val useCase = GeneratePairingPinUseCase()

        // Act
        val pin1 = useCase()
        val pin2 = useCase()
        val pin3 = useCase()

        // Assert
        // While theoretically they could be the same, the probability is extremely low
        // This test verifies randomness is working
        val allPins = listOf(pin1, pin2, pin3)
        assertTrue(allPins.distinct().size > 1, "Multiple calls should generate different PINs (randomness check)")
    }

    @Test
    fun `invoke should not generate PIN with leading zeros`() = runTest {
        // Arrange
        val useCase = GeneratePairingPinUseCase()

        // Act - Generate multiple PINs to test
        val pins = (1..10).map { useCase() }

        // Assert
        pins.forEach { pin ->
            assertNotEquals('0', pin.first(), "PIN should not start with 0")
            assertTrue(pin.toInt() >= 100000, "PIN should be at least 100000")
        }
    }
}
