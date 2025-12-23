package ireader.presentation.ui.settings.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.models.entities.TrackerCredentials
import ireader.domain.models.entities.TrackerService
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced tracking settings screen.
 * Manages external service integration following Mihon's TrackerManager system.
 */
class SettingsTrackingViewModel(
    private val preferenceStore: PreferenceStore,
    private val trackingRepository: TrackingRepository
) : BaseViewModel() {
    
    // Tracking service preferences
    val malEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("mal_enabled", false).stateIn(scope)
    val malLoggedIn: StateFlow<Boolean> = preferenceStore.getBoolean("mal_logged_in", false).stateIn(scope)
    
    private val _aniListEnabled = MutableStateFlow(preferenceStore.getBoolean("anilist_enabled", false).get())
    val aniListEnabled: StateFlow<Boolean> = _aniListEnabled.asStateFlow()
    
    private val _aniListLoggedIn = MutableStateFlow(false)
    val aniListLoggedIn: StateFlow<Boolean> = _aniListLoggedIn.asStateFlow()
    
    val kitsuEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("kitsu_enabled", false).stateIn(scope)
    val kitsuLoggedIn: StateFlow<Boolean> = preferenceStore.getBoolean("kitsu_logged_in", false).stateIn(scope)
    val mangaUpdatesEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("mangaupdates_enabled", false).stateIn(scope)
    val mangaUpdatesLoggedIn: StateFlow<Boolean> = preferenceStore.getBoolean("mangaupdates_logged_in", false).stateIn(scope)
    
    // Auto-sync preferences
    val autoSyncEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("auto_sync_enabled", false).stateIn(scope)
    val autoSyncInterval: StateFlow<Int> = preferenceStore.getInt("auto_sync_interval", 60).stateIn(scope)
    val syncOnlyOverWifi: StateFlow<Boolean> = preferenceStore.getBoolean("sync_only_over_wifi", true).stateIn(scope)
    
    // Auto-update preferences
    val autoUpdateStatus: StateFlow<Boolean> = preferenceStore.getBoolean("auto_update_status", true).stateIn(scope)
    val autoUpdateProgress: StateFlow<Boolean> = preferenceStore.getBoolean("auto_update_progress", true).stateIn(scope)
    val autoUpdateScore: StateFlow<Boolean> = preferenceStore.getBoolean("auto_update_score", false).stateIn(scope)
    
    // Dialog states
    var showSyncIntervalDialog by mutableStateOf(false)
        private set
    var showClearSyncDataDialog by mutableStateOf(false)
        private set
    
    // AniList OAuth state
    private val _aniListAuthUrl = MutableStateFlow<String?>(null)
    val aniListAuthUrl: StateFlow<String?> = _aniListAuthUrl.asStateFlow()
    
    var showAniListLoginDialog by mutableStateOf(false)
        private set
    
    var aniListLoginError by mutableStateOf<String?>(null)
        private set
    
    init {
        // Check AniList login status on init
        scope.launch {
            _aniListLoggedIn.value = trackingRepository.isAuthenticated(TrackerService.ANILIST)
            if (_aniListLoggedIn.value) {
                _aniListEnabled.value = true
            }
        }
    }
    
    // MyAnimeList functions
    fun setMalEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("mal_enabled", false).set(enabled)
        if (!enabled) {
            logoutFromMal()
        }
    }
    
    fun loginToMal() {
        // TODO: Implement MAL OAuth login
        preferenceStore.getBoolean("mal_logged_in", false).set(true)
    }
    
    fun logoutFromMal() {
        preferenceStore.getBoolean("mal_logged_in", false).set(false)
    }
    
    fun configureMal() {
        // TODO: Navigate to MAL configuration screen
    }
    
    // AniList functions
    fun setAniListEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("anilist_enabled", false).set(enabled)
        _aniListEnabled.value = enabled
        if (!enabled) {
            logoutFromAniList()
        }
    }
    
    fun loginToAniList() {
        // Show login dialog with OAuth URL
        showAniListLoginDialog = true
        aniListLoginError = null
        
        // Get the auth URL from repository
        scope.launch {
            try {
                // The auth URL is constructed in the API
                _aniListAuthUrl.value = "https://anilist.co/api/v2/oauth/authorize?client_id=21652&response_type=token"
            } catch (e: Exception) {
                aniListLoginError = "Failed to get auth URL: ${e.message}"
            }
        }
    }
    
    fun dismissAniListLoginDialog() {
        showAniListLoginDialog = false
        _aniListAuthUrl.value = null
        aniListLoginError = null
    }
    
    /**
     * Complete AniList login with the access token from OAuth redirect
     */
    fun completeAniListLogin(accessToken: String) {
        scope.launch {
            try {
                val credentials = TrackerCredentials(
                    serviceId = TrackerService.ANILIST,
                    accessToken = accessToken
                )
                val success = trackingRepository.authenticate(TrackerService.ANILIST, credentials)
                
                if (success) {
                    _aniListLoggedIn.value = true
                    _aniListEnabled.value = true
                    preferenceStore.getBoolean("anilist_enabled", false).set(true)
                    showAniListLoginDialog = false
                    aniListLoginError = null
                } else {
                    aniListLoginError = "Login failed. Please try again."
                }
            } catch (e: Exception) {
                aniListLoginError = "Login error: ${e.message}"
            }
        }
    }
    
    fun logoutFromAniList() {
        scope.launch {
            trackingRepository.logout(TrackerService.ANILIST)
            _aniListLoggedIn.value = false
        }
    }
    
    fun configureAniList() {
        // TODO: Navigate to AniList configuration screen
    }
    
    // Kitsu functions
    fun setKitsuEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("kitsu_enabled", false).set(enabled)
        if (!enabled) {
            logoutFromKitsu()
        }
    }
    
    fun loginToKitsu() {
        // TODO: Implement Kitsu login
        preferenceStore.getBoolean("kitsu_logged_in", false).set(true)
    }
    
    fun logoutFromKitsu() {
        preferenceStore.getBoolean("kitsu_logged_in", false).set(false)
    }
    
    fun configureKitsu() {
        // TODO: Navigate to Kitsu configuration screen
    }
    
    // MangaUpdates functions
    fun setMangaUpdatesEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("mangaupdates_enabled", false).set(enabled)
        if (!enabled) {
            logoutFromMangaUpdates()
        }
    }
    
    fun loginToMangaUpdates() {
        // TODO: Implement MangaUpdates login
        preferenceStore.getBoolean("mangaupdates_logged_in", false).set(true)
    }
    
    fun logoutFromMangaUpdates() {
        preferenceStore.getBoolean("mangaupdates_logged_in", false).set(false)
    }
    
    fun configureMangaUpdates() {
        // TODO: Navigate to MangaUpdates configuration screen
    }
    
    // Auto-sync functions
    fun setAutoSyncEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("auto_sync_enabled", false).set(enabled)
        if (enabled) {
            scheduleAutoSync()
        } else {
            cancelAutoSync()
        }
    }
    
    fun showSyncIntervalDialog() {
        showSyncIntervalDialog = true
    }
    
    fun dismissSyncIntervalDialog() {
        showSyncIntervalDialog = false
    }
    
    fun setAutoSyncInterval(interval: Int) {
        preferenceStore.getInt("auto_sync_interval", 60).set(interval)
        if (autoSyncEnabled.value) {
            scheduleAutoSync()
        }
    }
    
    fun setSyncOnlyOverWifi(enabled: Boolean) {
        preferenceStore.getBoolean("sync_only_over_wifi", true).set(enabled)
    }
    
    // Auto-update functions
    fun setAutoUpdateStatus(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_status", true).set(enabled)
    }
    
    fun setAutoUpdateProgress(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_progress", true).set(enabled)
    }
    
    fun setAutoUpdateScore(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_score", false).set(enabled)
    }
    
    // Advanced functions
    fun performManualSync() {
        scope.launch {
            // Get all tracked books and sync them
            val stats = trackingRepository.getTrackingStatistics()
            // TODO: Implement full sync
        }
    }
    
    fun showClearSyncDataDialog() {
        showClearSyncDataDialog = true
    }
    
    fun dismissClearSyncDataDialog() {
        showClearSyncDataDialog = false
    }
    
    fun clearAllSyncData() {
        // Logout from all services
        logoutFromMal()
        logoutFromAniList()
        logoutFromKitsu()
        logoutFromMangaUpdates()
        
        // Cancel auto-sync
        cancelAutoSync()
    }
    
    // Navigation functions
    fun navigateToSyncHistory() {
        // TODO: Navigate to sync history screen
    }
    
    // Background sync functions
    private fun scheduleAutoSync() {
        // TODO: Implement platform-specific background sync scheduling
    }
    
    private fun cancelAutoSync() {
        // TODO: Cancel scheduled background sync
    }
    
    // Sync status functions
    fun getSyncStatus(): Map<String, SyncStatus> {
        return mapOf(
            "mal" to getSyncStatusForService("mal"),
            "anilist" to getSyncStatusForService("anilist"),
            "kitsu" to getSyncStatusForService("kitsu"),
            "mangaupdates" to getSyncStatusForService("mangaupdates")
        )
    }
    
    private fun getSyncStatusForService(service: String): SyncStatus {
        val enabled = when (service) {
            "mal" -> malEnabled.value
            "anilist" -> aniListEnabled.value
            "kitsu" -> kitsuEnabled.value
            "mangaupdates" -> mangaUpdatesEnabled.value
            else -> false
        }
        
        val loggedIn = when (service) {
            "mal" -> malLoggedIn.value
            "anilist" -> aniListLoggedIn.value
            "kitsu" -> kitsuLoggedIn.value
            "mangaupdates" -> mangaUpdatesLoggedIn.value
            else -> false
        }
        
        return when {
            !enabled -> SyncStatus.DISABLED
            !loggedIn -> SyncStatus.NOT_LOGGED_IN
            else -> SyncStatus.READY
        }
    }
    
    // Tracking functions
    fun trackBook(bookId: Long, service: String) {
        // TODO: Implement book tracking for specific service
    }
    
    fun untrackBook(bookId: Long, service: String) {
        // TODO: Implement book untracking for specific service
    }
    
    fun updateBookStatus(bookId: Long, service: String, status: String) {
        // TODO: Update book status on tracking service
    }
    
    fun updateBookProgress(bookId: Long, service: String, progress: Int) {
        // TODO: Update book progress on tracking service
    }
    
    fun updateBookScore(bookId: Long, service: String, score: Float) {
        // TODO: Update book score on tracking service
    }
    
    // Search functions
    fun searchBooks(query: String, service: String): List<TrackingSearchResult> {
        // TODO: Implement book search on tracking service
        return emptyList()
    }
}

enum class SyncStatus {
    DISABLED,
    NOT_LOGGED_IN,
    READY,
    SYNCING,
    ERROR
}

data class TrackingSearchResult(
    val id: String,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val type: String,
    val status: String,
    val startDate: String?,
    val endDate: String?
)
