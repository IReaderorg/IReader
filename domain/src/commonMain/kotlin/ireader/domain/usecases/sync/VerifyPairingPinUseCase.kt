package ireader.domain.usecases.sync

/**
 * Use case for verifying a pairing PIN entered by the user.
 * 
 * This use case compares the expected PIN (generated on one device) with
 * the PIN entered by the user on another device during the pairing process.
 * 
 * The verification is a simple string comparison. Empty or null PINs are
 * considered invalid and will always return false.
 * 
 * @return true if the PINs match exactly, false otherwise
 */
class VerifyPairingPinUseCase {
    
    /**
     * Verifies that the entered PIN matches the expected PIN.
     * 
     * @param expectedPin The PIN that was generated and displayed on the initiating device
     * @param enteredPin The PIN that was entered by the user on the receiving device
     * @return true if both PINs are non-empty and match exactly, false otherwise
     */
    operator fun invoke(expectedPin: String, enteredPin: String): Boolean {
        // Empty PINs are invalid
        if (expectedPin.isEmpty() || enteredPin.isEmpty()) {
            return false
        }
        
        // Simple string comparison
        return expectedPin == enteredPin
    }
}
