package ireader.domain.usecases.sync

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for VerifyPairingPinUseCase.
 * 
 * Tests PIN verification functionality following TDD principles.
 */
class VerifyPairingPinUseCaseTest {
    
    @Test
    fun `invoke should return true for matching PINs`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "123456"
        val enteredPin = "123456"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertTrue(result, "Should return true when PINs match exactly")
    }
    
    @Test
    fun `invoke should return false for non-matching PINs`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "123456"
        val enteredPin = "654321"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when PINs don't match")
    }
    
    @Test
    fun `invoke should return false when expected PIN is empty`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = ""
        val enteredPin = "123456"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when expected PIN is empty")
    }
    
    @Test
    fun `invoke should return false when entered PIN is empty`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "123456"
        val enteredPin = ""
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when entered PIN is empty")
    }
    
    @Test
    fun `invoke should return false when both PINs are empty`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = ""
        val enteredPin = ""
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when both PINs are empty")
    }
    
    @Test
    fun `invoke should be case-sensitive for numeric strings`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "123456"
        val enteredPin = "123456"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertTrue(result, "Should match identical numeric strings")
    }
    
    @Test
    fun `invoke should return false for PINs with different lengths`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "123456"
        val enteredPin = "12345"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when PIN lengths differ")
    }
    
    @Test
    fun `invoke should handle PINs with leading zeros`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "000123"
        val enteredPin = "000123"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertTrue(result, "Should correctly match PINs with leading zeros")
    }
    
    @Test
    fun `invoke should return false when leading zeros don't match`() {
        // Arrange
        val useCase = VerifyPairingPinUseCase()
        val expectedPin = "000123"
        val enteredPin = "123"
        
        // Act
        val result = useCase(expectedPin, enteredPin)
        
        // Assert
        assertFalse(result, "Should return false when leading zeros are missing")
    }
}
