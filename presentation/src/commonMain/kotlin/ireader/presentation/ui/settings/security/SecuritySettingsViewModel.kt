package ireader.presentation.ui.settings.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.SecurityRepository
import ireader.domain.models.security.AuthMethod
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class SecuritySettingsViewModel(
    private val uiPreferences: UiPreferences,
    private val securityRepository: SecurityRepository
) : BaseViewModel() {
    
    // Security preferences
    val appLockEnabled = PreferenceMutableState(uiPreferences.appLockEnabled(), scope)
    val secureScreenEnabled = PreferenceMutableState(uiPreferences.secureScreenEnabled(), scope)
    val hideContentEnabled = PreferenceMutableState(uiPreferences.hideContentEnabled(), scope)
    val adultSourceLockEnabled = PreferenceMutableState(uiPreferences.adultSourceLockEnabled(), scope)
    val biometricEnabled = PreferenceMutableState(uiPreferences.biometricEnabled(), scope)
    
    var showSetupDialog by mutableStateOf(false)
        private set
    
    var setupDialogType by mutableStateOf<AuthMethod>(AuthMethod.None)
        private set
    
    var isBiometricAvailable by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var showOnboardingDialog by mutableStateOf(false)
        private set
    
    init {
        checkBiometricAvailability()
        checkFirstTimeUser()
    }
    
    private fun checkFirstTimeUser() {
        // Show onboarding if user hasn't seen it and no security is enabled
        val hasSeenOnboarding = uiPreferences.appLockEnabled().get() || 
                                uiPreferences.secureScreenEnabled().get() ||
                                uiPreferences.hideContentEnabled().get() ||
                                uiPreferences.adultSourceLockEnabled().get()
        
        if (!hasSeenOnboarding) {
            // Don't show automatically, let user discover features
            // showOnboardingDialog = true
        }
    }
    
    private fun checkBiometricAvailability() {
        scope.launch {
            isBiometricAvailable = securityRepository.isBiometricAvailable()
        }
    }
    
    fun toggleAppLock(enabled: Boolean) {
        if (enabled) {
            // Show setup dialog
            showSetupDialog = true
        } else {
            // Disable app lock
            scope.launch {
                securityRepository.clearAuthMethod()
                uiPreferences.appLockEnabled().set(false)
            }
        }
    }
    
    fun showSetupDialog(type: AuthMethod) {
        setupDialogType = type
        showSetupDialog = true
        errorMessage = null
    }
    
    fun hideSetupDialog() {
        showSetupDialog = false
        errorMessage = null
    }
    
    fun setupAuthMethod(method: AuthMethod) {
        scope.launch {
            val result = securityRepository.setAuthMethod(method)
            result.fold(
                onSuccess = {
                    hideSetupDialog()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Failed to set up authentication"
                }
            )
        }
    }
    
    fun toggleSecureScreen(enabled: Boolean) {
        uiPreferences.secureScreenEnabled().set(enabled)
    }
    
    fun toggleHideContent(enabled: Boolean) {
        uiPreferences.hideContentEnabled().set(enabled)
    }
    
    fun toggleAdultSourceLock(enabled: Boolean) {
        uiPreferences.adultSourceLockEnabled().set(enabled)
    }
    
    fun showOnboarding() {
        showOnboardingDialog = true
    }
    
    fun hideOnboarding() {
        showOnboardingDialog = false
    }
}
