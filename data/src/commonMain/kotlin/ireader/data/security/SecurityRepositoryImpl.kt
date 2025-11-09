package ireader.data.security

import ireader.domain.data.repository.SecurityRepository
import ireader.domain.models.security.AuthMethod
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of SecurityRepository using preferences for storage
 */
class SecurityRepositoryImpl(
    private val uiPreferences: UiPreferences,
    private val biometricAuthenticator: BiometricAuthenticator
) : SecurityRepository {
    
    override suspend fun setAuthMethod(method: AuthMethod): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            when (method) {
                is AuthMethod.PIN -> {
                    uiPreferences.appLockMethod().set("pin")
                    uiPreferences.appLockPin().set(hashPassword(method.pin))
                    uiPreferences.appLockEnabled().set(true)
                }
                is AuthMethod.Password -> {
                    uiPreferences.appLockMethod().set("password")
                    uiPreferences.appLockPassword().set(hashPassword(method.password))
                    uiPreferences.appLockEnabled().set(true)
                }
                is AuthMethod.Biometric -> {
                    if (biometricAuthenticator.isBiometricAvailable()) {
                        uiPreferences.appLockMethod().set("biometric")
                        uiPreferences.biometricEnabled().set(true)
                        uiPreferences.appLockEnabled().set(true)
                    } else {
                        return@withContext Result.failure(Exception("Biometric authentication not available"))
                    }
                }
                is AuthMethod.None -> {
                    uiPreferences.appLockEnabled().set(false)
                    uiPreferences.appLockMethod().set("none")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAuthMethod(): AuthMethod = withContext(Dispatchers.Default) {
        val method = uiPreferences.appLockMethod().get()
        val enabled = uiPreferences.appLockEnabled().get()
        
        if (!enabled) {
            return@withContext AuthMethod.None
        }
        
        when (method) {
            "pin" -> AuthMethod.PIN("")
            "password" -> AuthMethod.Password("")
            "biometric" -> AuthMethod.Biometric
            else -> AuthMethod.None
        }
    }
    
    override suspend fun authenticate(input: String): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            val method = uiPreferences.appLockMethod().get()
            val hashedInput = hashPassword(input)
            
            val isValid = when (method) {
                "pin" -> {
                    val storedPin = uiPreferences.appLockPin().get()
                    hashedInput == storedPin
                }
                "password" -> {
                    val storedPassword = uiPreferences.appLockPassword().get()
                    hashedInput == storedPassword
                }
                else -> false
            }
            
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun authenticateBiometric(): Result<Boolean> {
        return biometricAuthenticator.authenticate()
    }
    
    override suspend fun isAuthEnabled(): Boolean = withContext(Dispatchers.Default) {
        uiPreferences.appLockEnabled().get()
    }
    
    override suspend fun clearAuthMethod(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            uiPreferences.appLockEnabled().set(false)
            uiPreferences.appLockMethod().set("none")
            uiPreferences.appLockPin().set("")
            uiPreferences.appLockPassword().set("")
            uiPreferences.biometricEnabled().set(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isBiometricAvailable(): Boolean {
        return biometricAuthenticator.isBiometricAvailable()
    }
    
    /**
     * Simple hash function for passwords/PINs
     * In production, use a proper cryptographic hash like bcrypt or Argon2
     */
    private fun hashPassword(input: String): String {
        return input.hashCode().toString()
    }
}
