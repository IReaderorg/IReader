package ireader.presentation.ui.settings.cloudflare

import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Cloudflare bypass settings screen.
 */
class CloudflareBypassSettingsViewModel(
    val bypassManager: CloudflareBypassPluginManager,
    private val preferenceStore: PreferenceStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // FlareSolverr URL preference
    private val flareSolverrUrlPref = preferenceStore.getString(
        key = "flaresolverr_url",
        defaultValue = "http://localhost:8191/v1"
    )
    
    private val _flareSolverrUrl = MutableStateFlow(flareSolverrUrlPref.get())
    val flareSolverrUrl: StateFlow<String> = _flareSolverrUrl.asStateFlow()
    
    val providers = bypassManager.providers
    val status = bypassManager.status
    
    fun updateFlareSolverrUrl(url: String) {
        _flareSolverrUrl.value = url
        flareSolverrUrlPref.set(url)
    }
    
    fun clearCache() {
        bypassManager.clearCache()
    }
    
    suspend fun testConnection(): Boolean {
        return bypassManager.hasAvailableProvider()
    }
}
