package ireader.domain.storage

import android.content.Context
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Checks if SAF folder is available and manages requests to re-select folder.
 * Similar to RequiredPluginChecker but for SAF storage.
 */
class SafFolderChecker(
    private val context: Context,
    private val uiPreferences: UiPreferences,
    private val safStorageManager: SafStorageManager
) {
    private val _safFolderRequired = MutableStateFlow(false)
    val safFolderRequired: StateFlow<Boolean> = _safFolderRequired.asStateFlow()
    
    /**
     * Check if SAF folder is available and accessible.
     * Returns true if folder is available, false if permission was lost or folder removed.
     */
    fun isSafFolderAvailable(): Boolean {
        // Check if user has completed onboarding (selected a folder)
        val hasCompletedOnboarding = uiPreferences.hasCompletedOnboarding().get()
        if (!hasCompletedOnboarding) {
            // User hasn't completed onboarding yet, don't show SAF required screen
            return true
        }
        
        // Check if SAF folder is accessible
        return safStorageManager.isSafStorageAvailable()
    }
    
    /**
     * Request user to select SAF folder again.
     * This will show the SAF folder selection screen.
     */
    fun requestSafFolder() {
        _safFolderRequired.value = true
    }
    
    /**
     * Clear the SAF folder request after user has selected a folder or dismissed.
     */
    fun clearSafFolderRequest() {
        _safFolderRequired.value = false
    }
    
    /**
     * Check SAF folder availability and request if needed.
     * Call this on app startup to ensure storage is available.
     */
    fun checkAndRequestIfNeeded() {
        if (!isSafFolderAvailable()) {
            requestSafFolder()
        }
    }
}
