package ireader.domain.services.platform

import ireader.domain.services.common.PlatformService
import ireader.domain.services.common.ServiceResult

/**
 * Platform-agnostic biometric authentication service
 * 
 * Provides fingerprint, face recognition, and other biometric authentication
 */
interface BiometricService : PlatformService {
    
    /**
     * Check if biometric authentication is available on this device
     * 
     * @return true if biometric authentication is available
     */
    suspend fun isBiometricAvailable(): Boolean
    
    /**
     * Check if biometric authentication is enrolled (user has set it up)
     * 
     * @return true if biometric is enrolled
     */
    suspend fun isBiometricEnrolled(): Boolean
    
    /**
     * Authenticate user with biometric
     * 
     * @param title Dialog title
     * @param subtitle Dialog subtitle (optional)
     * @param description Dialog description (optional)
     * @param negativeButtonText Text for cancel button
     * @param confirmationRequired Require explicit user confirmation after biometric
     * @return Authentication result
     */
    suspend fun authenticate(
        title: String,
        subtitle: String? = null,
        description: String? = null,
        negativeButtonText: String = "Cancel",
        confirmationRequired: Boolean = false
    ): ServiceResult<BiometricResult>
    
    /**
     * Get supported biometric types on this device
     * 
     * @return List of supported biometric types
     */
    fun getSupportedBiometricTypes(): List<BiometricType>
    
    /**
     * Get biometric capability level
     * 
     * @return Capability level
     */
    fun getBiometricCapability(): BiometricCapability
}

/**
 * Biometric authentication result
 */
sealed class BiometricResult {
    /**
     * Authentication succeeded
     */
    object Success : BiometricResult()
    
    /**
     * User cancelled authentication
     */
    object Cancelled : BiometricResult()
    
    /**
     * Authentication failed (wrong biometric)
     */
    data class Failed(val attemptsRemaining: Int? = null) : BiometricResult()
    
    /**
     * Error occurred during authentication
     */
    data class Error(
        val errorCode: BiometricErrorCode,
        val message: String
    ) : BiometricResult()
    
    /**
     * Biometric is locked out (too many failed attempts)
     */
    data class LockedOut(val lockoutDurationMs: Long? = null) : BiometricResult()
}

/**
 * Biometric type enumeration
 */
enum class BiometricType {
    /**
     * Fingerprint sensor
     */
    FINGERPRINT,
    
    /**
     * Face recognition (2D or 3D)
     */
    FACE,
    
    /**
     * Iris scanner
     */
    IRIS,
    
    /**
     * Voice recognition
     */
    VOICE,
    
    /**
     * Unknown or multiple types
     */
    UNKNOWN
}

/**
 * Biometric capability level
 */
enum class BiometricCapability {
    /**
     * Strong biometric (Class 3) - Secure enough for payments
     */
    STRONG,
    
    /**
     * Weak biometric (Class 2) - Suitable for app unlock
     */
    WEAK,
    
    /**
     * No biometric available
     */
    NONE
}

/**
 * Biometric error codes
 */
enum class BiometricErrorCode {
    /**
     * Hardware not available
     */
    HARDWARE_UNAVAILABLE,
    
    /**
     * No biometric enrolled
     */
    NO_BIOMETRIC_ENROLLED,
    
    /**
     * User cancelled
     */
    USER_CANCELLED,
    
    /**
     * Too many failed attempts
     */
    LOCKOUT,
    
    /**
     * Permanent lockout (requires device unlock)
     */
    LOCKOUT_PERMANENT,
    
    /**
     * Biometric sensor is busy
     */
    BUSY,
    
    /**
     * Timeout occurred
     */
    TIMEOUT,
    
    /**
     * Unknown error
     */
    UNKNOWN
}
