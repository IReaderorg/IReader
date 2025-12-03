package ireader.data.security

import platform.LocalAuthentication.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of BiometricAuthenticator
 * Uses LocalAuthentication framework for Face ID / Touch ID
 */
@OptIn(ExperimentalForeignApi::class)
class BiometricAuthenticatorImpl : BiometricAuthenticator {
    
    private val context = LAContext()
    
    /**
     * Check if biometric authentication is available
     */
    override fun isBiometricAvailable(): Boolean {
        val newContext = LAContext()
        return newContext.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )
    }
    
    /**
     * Get the type of biometric available
     */
    fun getBiometricType(): BiometricType {
        val newContext = LAContext()
        
        if (!newContext.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)) {
            return BiometricType.NONE
        }
        
        return when (newContext.biometryType) {
            LABiometryTypeFaceID -> BiometricType.FACE_ID
            LABiometryTypeTouchID -> BiometricType.TOUCH_ID
            else -> BiometricType.NONE
        }
    }
    
    /**
     * Authenticate using biometrics
     */
    override suspend fun authenticate(): Result<Boolean> {
        return authenticate("Authenticate to access IReader")
    }
    
    /**
     * Authenticate with custom reason
     */
    suspend fun authenticate(reason: String): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val newContext = LAContext()
        
        // Check if biometrics are available
        if (!newContext.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)) {
            continuation.resume(Result.failure(BiometricException("Biometric authentication not available")))
            return@suspendCancellableCoroutine
        }
        
        // Configure context
        newContext.localizedFallbackTitle = "Use Passcode"
        newContext.localizedCancelTitle = "Cancel"
        
        // Evaluate policy
        newContext.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = reason
        ) { success, error ->
            if (success) {
                continuation.resume(Result.success(true))
            } else {
                val errorMessage = when (error?.code) {
                    LAErrorAuthenticationFailed -> "Authentication failed"
                    LAErrorUserCancel -> "User cancelled"
                    LAErrorUserFallback -> "User chose fallback"
                    LAErrorSystemCancel -> "System cancelled"
                    LAErrorPasscodeNotSet -> "Passcode not set"
                    LAErrorBiometryNotAvailable -> "Biometry not available"
                    LAErrorBiometryNotEnrolled -> "Biometry not enrolled"
                    LAErrorBiometryLockout -> "Biometry locked out"
                    else -> error?.localizedDescription ?: "Unknown error"
                }
                
                val isCancelled = error?.code == LAErrorUserCancel || error?.code == LAErrorSystemCancel
                
                if (isCancelled) {
                    continuation.resume(Result.success(false))
                } else {
                    continuation.resume(Result.failure(BiometricException(errorMessage)))
                }
            }
        }
    }
    
    /**
     * Authenticate with device passcode fallback
     */
    suspend fun authenticateWithPasscodeFallback(reason: String): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val newContext = LAContext()
        
        // Check if device owner authentication is available (includes passcode)
        if (!newContext.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)) {
            continuation.resume(Result.failure(BiometricException("Device authentication not available")))
            return@suspendCancellableCoroutine
        }
        
        // Evaluate policy with passcode fallback
        newContext.evaluatePolicy(
            LAPolicyDeviceOwnerAuthentication,
            localizedReason = reason
        ) { success, error ->
            if (success) {
                continuation.resume(Result.success(true))
            } else {
                val errorMessage = error?.localizedDescription ?: "Authentication failed"
                val isCancelled = error?.code == LAErrorUserCancel || error?.code == LAErrorSystemCancel
                
                if (isCancelled) {
                    continuation.resume(Result.success(false))
                } else {
                    continuation.resume(Result.failure(BiometricException(errorMessage)))
                }
            }
        }
    }
    
    /**
     * Invalidate the current context
     * Call this when the app goes to background
     */
    fun invalidate() {
        context.invalidate()
    }
}

/**
 * Biometric type enum
 */
enum class BiometricType {
    NONE,
    TOUCH_ID,
    FACE_ID
}

/**
 * Biometric exception
 */
class BiometricException(message: String) : Exception(message)
