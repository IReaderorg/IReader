package ireader.presentation.ui.settings.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

/**
 * ViewModel for the enhanced security and privacy settings screen.
 * Manages comprehensive security preferences following Mihon's SecurityPreferences system.
 */
class SettingsSecurityViewModel(
    private val uiPreferences: UiPreferences
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
    
    // Dialog states
    var showLockMethodDialog by mutableStateOf(false)
        private set
    var showLockAfterInactivityDialog by mutableStateOf(false)
        private set
    var showSecureScreenModeDialog by mutableStateOf(false)
        private set
    var showClearAuthDataDialog by mutableStateOf(false)
        private set
    
    // Platform-specific biometric availability
    val isBiometricAvailable: Boolean = checkBiometricAvailability()
    
    // App lock functions
    fun setAppLockEnabled(enabled: Boolean) {
        uiPreferences.appLockEnabled().set(enabled)
        // Sync with legacy useAuthenticator preference for SecureActivityDelegate
        uiPreferences.useAuthenticator().set(enabled)
        if (!enabled) {
            // Reset related settings when app lock is disabled
            uiPreferences.biometricEnabled().set(false)
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
        // TODO: Implement method-specific setup (PIN entry, password setup, etc.)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        if (isBiometricAvailable) {
            uiPreferences.biometricEnabled().set(enabled)
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
        // TODO: Implement clearing of authentication tokens and sessions
        // This should clear:
        // - Source login tokens
        // - Tracking service tokens
        // - Cloud backup authentication
        // - Any other stored authentication data
    }
    
    // Navigation functions
    fun navigateToSecurityAudit() {
        // TODO: Implement navigation to security audit screen
    }
    
    // Platform-specific functions
    private fun checkBiometricAvailability(): Boolean {
        // TODO: Implement platform-specific biometric availability check
        // This should check if the device supports fingerprint, face unlock, etc.
        return true // Placeholder
    }
    
    // PIN/Password setup functions
    fun setupPIN() {
        // TODO: Implement PIN setup dialog/screen
    }
    
    fun setupPassword() {
        // TODO: Implement password setup dialog/screen
    }
    
    fun setupPattern() {
        // TODO: Implement pattern setup dialog/screen
    }
    
    fun setupBiometric() {
        // TODO: Implement biometric enrollment if needed
    }
    
    // Security validation functions
    fun validateCurrentAuthentication(callback: (Boolean) -> Unit) {
        // TODO: Implement current authentication validation
        // This should prompt for current PIN/password/biometric before allowing changes
        callback(true) // Placeholder
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
}