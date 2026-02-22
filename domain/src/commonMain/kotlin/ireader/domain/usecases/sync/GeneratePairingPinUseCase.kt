package ireader.domain.usecases.sync

import kotlin.random.Random

/**
 * Use case for generating a random 6-digit PIN for device pairing.
 * 
 * The PIN is used during the device pairing process to verify that both
 * devices are controlled by the same user. The PIN should be displayed
 * on one device and entered on the other device to complete pairing.
 * 
 * **Security Note**: This PIN is for local network pairing only and provides
 * basic protection against accidental connections. It is not cryptographically
 * secure for protecting sensitive data over untrusted networks.
 * 
 * @return A 6-digit numeric PIN as a String (e.g., "123456", "000789")
 */
class GeneratePairingPinUseCase {
    
    /**
     * Generates a random 6-digit PIN.
     * 
     * The PIN will always be exactly 6 digits, including leading zeros if necessary.
     * For example: "000123", "456789", "001234"
     * 
     * @return A 6-digit numeric PIN string
     */
    operator fun invoke(): String {
        // Generate a random number between 0 and 999999 (inclusive)
        val randomNumber = Random.nextInt(0, 1_000_000)
        
        // Format as 6-digit string with leading zeros
        return randomNumber.toString().padStart(6, '0')
    }
}
