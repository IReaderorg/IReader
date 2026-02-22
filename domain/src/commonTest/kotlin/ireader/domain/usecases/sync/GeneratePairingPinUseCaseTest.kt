package ireader.domain.usecases.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Test suite for GeneratePairingPinUseCase.
 * 
 * Tests PIN generation functionality following TDD principles.
 */
class GeneratePairingPinUseCaseTest {
    
    @Test
    fun `invoke should generate 6-digit PIN`() {
        // Arrange
        val useCase = GeneratePairingPinUseCase()
        
        // Act
        val pin = useCase()
        
        // Assert
        assertEquals(6, pin.length, "PIN should be exactly 6 digits")
    }
    
    @Test
    fun `invoke should generate numeric only PIN`() {
        // Arrange
        val useCase = GeneratePairingPinUseCase()
        
        // Act
        val pin = useCase()
        
        // Assert
        assertTrue(pin.all { it.isDigit() }, "PIN should contain only numeric digits")
    }
    
    @Test
    fun `invoke should generate different PINs on multiple calls`() {
        // Arrange
        val useCase = GeneratePairingPinUseCase()
        
        // Act
        val pin1 = useCase()
        val pin2 = useCase()
        val pin3 = useCase()
        
        // Assert
        // While theoretically possible to get same PIN, probability is 1/1,000,000
        // Testing 3 calls makes collision extremely unlikely
        val allPins = setOf(pin1, pin2, pin3)
        assertTrue(
            allPins.size >= 2,
            "Multiple calls should generate different PINs (got: $allPins)"
        )
    }
    
    @Test
    fun `invoke should generate PIN with leading zeros if needed`() {
        // Arrange
        val useCase = GeneratePairingPinUseCase()
        
        // Act - Generate multiple PINs to increase chance of getting one with leading zero
        val pins = (1..100).map { useCase() }
        
        // Assert
        assertTrue(
            pins.any { it.startsWith("0") },
            "Should be able to generate PINs with leading zeros"
        )
    }
}
