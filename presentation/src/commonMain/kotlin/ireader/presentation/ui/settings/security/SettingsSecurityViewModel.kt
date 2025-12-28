package ireader.presentation.ui.settings.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced security and privacy settings screen.
 * Manages comprehensive security preferences following Mihon's SecurityPreferences system.
 */
class SettingsSecurityViewModel(
    private val uiPreferences: UiPreferences,
    private val preferenceStore: PreferenceStore,
    private val securityHelper: SecurityHelper = DefaultSecurityHelper()
) : BaseViewModel() {
    
    // App lock preferences
    val appLockEnabled: StateFlow<Boolean> = uiPreferences.appLockEnabled().stateIn(scope)
    val appLockMethod: StateFlow<String> = uiPreferences.appLockMethod().stateIn(scope)
    val biometricEnabled: StateFlow<Boolean> = uiPreferences.biometricEnabled().stateIn(scope)
    val lockAfterInactivity: StateFlow<Int> = uiPreferences.lockAppAfter().changes().map { it.toInt() }.stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    // Screen security preferences
    val secureScreenMode: StateFlow<PreferenceValues.SecureScreenMode> = uiPreferences.secureScreen().stateIn(scope)
    
    // Privacy preferences
    val hideNotificationContent: StateFlow<Boolean> = uiPreferences.hideContentEnabled().stateIn(scope)
    val incognitoMode: StateFlow<Boolean> = uiPreferences.incognitoMode().stateIn(scope)
    
    // Content restrictions
    val adultContentLock: StateFlow<Boolean> = uiPreferences.adultSourceLockEnabled().stateIn(scope)
    
    // PIN/Password state
    private val _pinHash = MutableStateFlow(preferenceStore.getString("security_pin_hash", "").get())
    val hasPinSet: StateFlow<Boolean> = _pinHash.asStateFlow().map { it.isNotEmpty() }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    private val _passwordHash = MutableStateFlow(preferenceStore.getString("security_password_hash", "").get())
    val hasPasswordSet: StateFlow<Boolean> = _passwordHash.asStateFlow().map { it.isNotEmpty() }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    // Dialog states
    var showLockMethodDialog by mutableStateOf(false)
        private set
    var showLockAfterInactivityDialog by mutableStateOf(false)
        private set
    var showSecureScreenModeDialog by mutableStateOf(false)
        private set
    var showClearAuthDataDialog by mutableStateOf(false)
        private set
    var showPinSetupDialog by mutableStateOf(false)
        private set
    var showPasswordSetupDialog by mutableStateOf(false)
        private set
    var showAuthValidationDialog by mutableStateOf(false)
        private set
    
    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    // Platform-specific biometric availability
    val isBiometricAvailable: Boolean = securityHelper.isBiometricAvailable()
    
    // App lock functions
    fun setAppLockEnabled(enabled: Boolean) {
        uiPreferences.appLockEnabled().set(enabled)
        // Sync with legacy useAuthenticator preference for SecureActivityDelegate
        uiPreferences.useAuthenticator().set(enabled)
        if (!enabled) {
            // Reset related settings when app lock is disabled
            uiPreferences.biometricEnabled().set(false)
            // Reset runtime lock state so app doesn't prompt for auth anymore
            uiPreferences.isAppLocked = false
        }
    }
    
    fun showLockMethodDialog() {
        showLockMethodDialog = true
    }
    
    fun dismissLockMethodDialog() {
        showLockMethodDialog = false
    }
    
    fun setAppLockMethod(method: String) {
        uiPreferences.appLockMethod().set(method)
        
        // Trigger setup for the selected method
        when (method) {
            "pin" -> if (!hasPinSet.value) showPinSetupDialog = true
            "password" -> if (!hasPasswordSet.value) showPasswordSetupDialog = true
            "biometric" -> if (isBiometricAvailable) setupBiometric()
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        if (isBiometricAvailable) {
            uiPreferences.biometricEnabled().set(enabled)
        } else {
            showSnackbar("Biometric authentication is not available on this device")
        }
    }
    
    fun showLockAfterInactivityDialog() {
        showLockAfterInactivityDialog = true
    }
    
    fun dismissLockAfterInactivityDialog() {
        showLockAfterInactivityDialog = false
    }
    
    fun setLockAfterInactivity(minutes: Int) {
        uiPreferences.lockAppAfter().set(minutes.toLong())
    }
    
    // Screen security functions
    fun showSecureScreenModeDialog() {
        showSecureScreenModeDialog = true
    }
    
    fun dismissSecureScreenModeDialog() {
        showSecureScreenModeDialog = false
    }
    
    fun setSecureScreenMode(mode: PreferenceValues.SecureScreenMode) {
        uiPreferences.secureScreen().set(mode)
    }
    
    // Privacy functions
    fun setHideNotificationContent(enabled: Boolean) {
        uiPreferences.hideContentEnabled().set(enabled)
    }
    
    fun setIncognitoMode(enabled: Boolean) {
        uiPreferences.incognitoMode().set(enabled)
    }
    
    // Content restriction functions
    fun setAdultContentLock(enabled: Boolean) {
        uiPreferences.adultSourceLockEnabled().set(enabled)
    }
    
    // Advanced security functions
    fun showClearAuthDataDialog() {
        showClearAuthDataDialog = true
    }
    
    fun dismissClearAuthDataDialog() {
        showClearAuthDataDialog = false
    }
    
    fun clearAuthenticationData() {
        scope.launch {
            try {
                // Clear PIN and password hashes
                preferenceStore.getString("security_pin_hash", "").set("")
                preferenceStore.getString("security_password_hash", "").set("")
                _pinHash.value = ""
                _passwordHash.value = ""
                
                // Clear tracking service tokens (stored in preferences)
                preferenceStore.getString("anilist_token", "").set("")
                preferenceStore.getString("mal_token", "").set("")
                preferenceStore.getString("kitsu_token", "").set("")
                preferenceStore.getString("mangaupdates_token", "").set("")
                
                // Clear cloud backup authentication
                preferenceStore.getString("cloud_backup_token", "").set("")
                
                // Reset app lock state
                uiPreferences.isAppLocked = false
                uiPreferences.lastAppUnlock().set(0L)
                
                showSnackbar("Authentication data cleared successfully")
                Log.info { "All authentication data cleared" }
            } catch (e: Exception) {
                Log.error(e, "Failed to clear authentication data")
                showSnackbar("Failed to clear authentication data: ${e.message}")
            }
        }
    }
    
    // Navigation functions - these trigger navigation events
    private val _navigationEvent = MutableStateFlow<SecurityNavigationEvent?>(null)
    val navigationEvent: StateFlow<SecurityNavigationEvent?> = _navigationEvent.asStateFlow()
    
    fun navigateToSecurityAudit() {
        _navigationEvent.value = SecurityNavigationEvent.SecurityAudit
    }
    
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
    
    // PIN/Password setup functions
    fun setupPIN() {
        showPinSetupDialog = true
    }
    
    fun dismissPinSetupDialog() {
        showPinSetupDialog = false
    }
    
    fun savePIN(pin: String): Boolean {
        if (pin.length < 4) {
            showSnackbar("PIN must be at least 4 digits")
            return false
        }
        
        val hash = securityHelper.hashCredential(pin)
        preferenceStore.getString("security_pin_hash", "").set(hash)
        _pinHash.value = hash
        showPinSetupDialog = false
        showSnackbar("PIN set successfully")
        return true
    }
    
    fun verifyPIN(pin: String): Boolean {
        val storedHash = preferenceStore.getString("security_pin_hash", "").get()
        return securityHelper.verifyCredential(pin, storedHash)
    }
    
    fun setupPassword() {
        showPasswordSetupDialog = true
    }
    
    fun dismissPasswordSetupDialog() {
        showPasswordSetupDialog = false
    }
    
    fun savePassword(password: String): Boolean {
        if (password.length < 6) {
            showSnackbar("Password must be at least 6 characters")
            return false
        }
        
        val hash = securityHelper.hashCredential(password)
        preferenceStore.getString("security_password_hash", "").set(hash)
        _passwordHash.value = hash
        showPasswordSetupDialog = false
        showSnackbar("Password set successfully")
        return true
    }
    
    fun verifyPassword(password: String): Boolean {
        val storedHash = preferenceStore.getString("security_password_hash", "").get()
        return securityHelper.verifyCredential(password, storedHash)
    }
    
    fun setupPattern() {
        // Pattern lock requires platform-specific UI
        // For now, show a message
        showSnackbar("Pattern lock setup requires platform-specific implementation")
    }
    
    fun setupBiometric() {
        if (isBiometricAvailable) {
            uiPreferences.biometricEnabled().set(true)
            showSnackbar("Biometric authentication enabled")
        } else {
            showSnackbar("Biometric authentication is not available on this device")
        }
    }
    
    // Security validation functions
    fun showAuthValidationDialog() {
        showAuthValidationDialog = true
    }
    
    fun dismissAuthValidationDialog() {
        showAuthValidationDialog = false
    }
    
    fun validateCurrentAuthentication(callback: (Boolean) -> Unit) {
        val method = appLockMethod.value
        
        when (method) {
            "pin" -> {
                if (hasPinSet.value) {
                    // Show PIN validation dialog - callback will be called from UI
                    showAuthValidationDialog = true
                } else {
                    callback(true) // No PIN set, allow
                }
            }
            "password" -> {
                if (hasPasswordSet.value) {
                    // Show password validation dialog - callback will be called from UI
                    showAuthValidationDialog = true
                } else {
                    callback(true) // No password set, allow
                }
            }
            "biometric" -> {
                // Biometric validation is handled by platform-specific code
                callback(true)
            }
            else -> callback(true) // No lock method, allow
        }
    }
    
    // Security audit functions
    fun getSecurityScore(): Int {
        var score = 0
        
        if (appLockEnabled.value) score += 25
        if (biometricEnabled.value && isBiometricAvailable) score += 15
        if (secureScreenMode.value != PreferenceValues.SecureScreenMode.NEVER) score += 20
        if (hideNotificationContent.value) score += 10
        if (adultContentLock.value) score += 10
        if (lockAfterInactivity.value in 1..15) score += 20 // Good inactivity timeout
        
        return score
    }
    
    fun getSecurityRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (!appLockEnabled.value) {
            recommendations.add("Enable app lock to protect your reading data")
        }
        
        if (appLockEnabled.value && !biometricEnabled.value && isBiometricAvailable) {
            recommendations.add("Enable biometric authentication for faster access")
        }
        
        if (secureScreenMode.value == PreferenceValues.SecureScreenMode.NEVER) {
            recommendations.add("Enable secure screen to hide content in recent apps")
        }
        
        if (!hideNotificationContent.value) {
            recommendations.add("Hide notification content to protect your privacy")
        }
        
        if (lockAfterInactivity.value == -1 || lockAfterInactivity.value > 30) {
            recommendations.add("Set a shorter inactivity timeout for better security")
        }
        
        return recommendations
    }
    
    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

/**
 * Navigation events for security settings
 */
sealed class SecurityNavigationEvent {
    object SecurityAudit : SecurityNavigationEvent()
}

/**
 * Interface for platform-specific security operations
 */
interface SecurityHelper {
    fun isBiometricAvailable(): Boolean
    fun hashCredential(credential: String): String
    fun verifyCredential(credential: String, hash: String): Boolean
}

/**
 * Default implementation using simple hashing (for commonMain)
 * Platform-specific implementations can provide stronger hashing
 */
class DefaultSecurityHelper : SecurityHelper {
    override fun isBiometricAvailable(): Boolean = false
    
    override fun hashCredential(credential: String): String {
        // Simple hash for cross-platform compatibility
        // Platform-specific implementations should use stronger algorithms
        var hash = 7L
        for (char in credential) {
            hash = hash * 31 + char.code
        }
        return hash.toString(16)
    }
    
    override fun verifyCredential(credential: String, hash: String): Boolean {
        return hashCredential(credential) == hash
    }
}
