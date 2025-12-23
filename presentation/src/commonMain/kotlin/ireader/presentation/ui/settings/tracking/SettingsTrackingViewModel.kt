package ireader.presentation.ui.settings.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.entities.TrackerCredentials
import ireader.domain.models.entities.TrackerService
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the enhanced tracking settings screen.
 * Manages external service integration for AniList, MyAnimeList, Kitsu, and MangaUpdates.
 */
class SettingsTrackingViewModel(
    private val preferenceStore: PreferenceStore,
    private val trackingRepository: TrackingRepository
) : BaseViewModel() {
    
    // ==================== Service State ====================
    
    // AniList state
    private val _aniListEnabled = MutableStateFlow(preferenceStore.getBoolean("anilist_enabled", false).get())
    val aniListEnabled: StateFlow<Boolean> = _aniListEnabled.asStateFlow()
    
    private val _aniListLoggedIn = MutableStateFlow(false)
    val aniListLoggedIn: StateFlow<Boolean> = _aniListLoggedIn.asStateFlow()
    
    // MAL state
    private val _malEnabled = MutableStateFlow(preferenceStore.getBoolean("mal_enabled", false).get())
    val malEnabled: StateFlow<Boolean> = _malEnabled.asStateFlow()
    
    private val _malLoggedIn = MutableStateFlow(false)
    val malLoggedIn: StateFlow<Boolean> = _malLoggedIn.asStateFlow()
    
    // Kitsu state
    private val _kitsuEnabled = MutableStateFlow(preferenceStore.getBoolean("kitsu_enabled", false).get())
    val kitsuEnabled: StateFlow<Boolean> = _kitsuEnabled.asStateFlow()
    
    private val _kitsuLoggedIn = MutableStateFlow(false)
    val kitsuLoggedIn: StateFlow<Boolean> = _kitsuLoggedIn.asStateFlow()
    
    // MangaUpdates state
    private val _mangaUpdatesEnabled = MutableStateFlow(preferenceStore.getBoolean("mangaupdates_enabled", false).get())
    val mangaUpdatesEnabled: StateFlow<Boolean> = _mangaUpdatesEnabled.asStateFlow()
    
    private val _mangaUpdatesLoggedIn = MutableStateFlow(false)
    val mangaUpdatesLoggedIn: StateFlow<Boolean> = _mangaUpdatesLoggedIn.asStateFlow()
    
    // ==================== Auto-Sync Preferences ====================
    
    private val _autoSyncEnabled = MutableStateFlow(preferenceStore.getBoolean("auto_sync_enabled", false).get())
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()
    
    private val _autoSyncInterval = MutableStateFlow(preferenceStore.getInt("auto_sync_interval", 60).get())
    val autoSyncInterval: StateFlow<Int> = _autoSyncInterval.asStateFlow()
    
    private val _syncOnlyOverWifi = MutableStateFlow(preferenceStore.getBoolean("sync_only_over_wifi", true).get())
    val syncOnlyOverWifi: StateFlow<Boolean> = _syncOnlyOverWifi.asStateFlow()
    
    // ==================== Auto-Update Preferences ====================
    
    private val _autoUpdateStatus = MutableStateFlow(preferenceStore.getBoolean("auto_update_status", true).get())
    val autoUpdateStatus: StateFlow<Boolean> = _autoUpdateStatus.asStateFlow()
    
    private val _autoUpdateProgress = MutableStateFlow(preferenceStore.getBoolean("auto_update_progress", true).get())
    val autoUpdateProgress: StateFlow<Boolean> = _autoUpdateProgress.asStateFlow()
    
    private val _autoUpdateScore = MutableStateFlow(preferenceStore.getBoolean("auto_update_score", false).get())
    val autoUpdateScore: StateFlow<Boolean> = _autoUpdateScore.asStateFlow()
    
    // ==================== Sync State ====================
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(preferenceStore.getLong("last_sync_time", 0L).get())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()
    
    // ==================== Statistics ====================
    
    private val _trackedBooksCount = MutableStateFlow(0)
    val trackedBooksCount: StateFlow<Int> = _trackedBooksCount.asStateFlow()
    
    // ==================== Dialog States ====================
    
    var showSyncIntervalDialog by mutableStateOf(false)
        private set
    var showClearSyncDataDialog by mutableStateOf(false)
        private set
    var showSyncHistoryDialog by mutableStateOf(false)
        private set
    
    // AniList OAuth state
    private val _aniListAuthUrl = MutableStateFlow<String?>(null)
    val aniListAuthUrl: StateFlow<String?> = _aniListAuthUrl.asStateFlow()
    var showAniListLoginDialog by mutableStateOf(false)
        private set
    var aniListLoginError by mutableStateOf<String?>(null)
        private set
    
    // MAL OAuth state
    private val _malAuthUrl = MutableStateFlow<String?>(null)
    val malAuthUrl: StateFlow<String?> = _malAuthUrl.asStateFlow()
    var showMalLoginDialog by mutableStateOf(false)
        private set
    var malLoginError by mutableStateOf<String?>(null)
        private set
    
    // Kitsu login state (username/password)
    var showKitsuLoginDialog by mutableStateOf(false)
        private set
    var kitsuLoginError by mutableStateOf<String?>(null)
        private set
    
    // MangaUpdates login state (username/password)
    var showMangaUpdatesLoginDialog by mutableStateOf(false)
        private set
    var mangaUpdatesLoginError by mutableStateOf<String?>(null)
        private set
    
    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    init {
        loadInitialState()
    }
    
    private fun loadInitialState() {
        scope.launch {
            try {
                // Check all service login statuses
                _aniListLoggedIn.value = trackingRepository.isAuthenticated(TrackerService.ANILIST)
                _malLoggedIn.value = trackingRepository.isAuthenticated(TrackerService.MYANIMELIST)
                _kitsuLoggedIn.value = trackingRepository.isAuthenticated(TrackerService.KITSU)
                _mangaUpdatesLoggedIn.value = trackingRepository.isAuthenticated(TrackerService.MANGAUPDATES)
                
                // Update enabled states based on login
                if (_aniListLoggedIn.value) {
                    _aniListEnabled.value = true
                    preferenceStore.getBoolean("anilist_enabled", false).set(true)
                }
                if (_malLoggedIn.value) {
                    _malEnabled.value = true
                    preferenceStore.getBoolean("mal_enabled", false).set(true)
                }
                if (_kitsuLoggedIn.value) {
                    _kitsuEnabled.value = true
                    preferenceStore.getBoolean("kitsu_enabled", false).set(true)
                }
                if (_mangaUpdatesLoggedIn.value) {
                    _mangaUpdatesEnabled.value = true
                    preferenceStore.getBoolean("mangaupdates_enabled", false).set(true)
                }
                
                // Load tracking statistics
                loadTrackingStatistics()
            } catch (e: Exception) {
                Log.error(e, "Failed to load initial tracking state")
            }
        }
    }
    
    private suspend fun loadTrackingStatistics() {
        try {
            val stats = trackingRepository.getTrackingStatistics()
            _trackedBooksCount.value = stats.totalTrackedBooks
        } catch (e: Exception) {
            Log.error(e, "Failed to load tracking statistics")
        }
    }
    
    // ==================== AniList Functions ====================
    
    fun setAniListEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("anilist_enabled", false).set(enabled)
        _aniListEnabled.value = enabled
        if (!enabled) {
            logoutFromAniList()
        }
    }
    
    fun loginToAniList() {
        showAniListLoginDialog = true
        aniListLoginError = null
        _aniListAuthUrl.value = "https://anilist.co/api/v2/oauth/authorize?client_id=21652&response_type=token"
    }
    
    fun dismissAniListLoginDialog() {
        showAniListLoginDialog = false
        _aniListAuthUrl.value = null
        aniListLoginError = null
    }
    
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
                    showSnackbar("Successfully logged in to AniList")
                } else {
                    aniListLoginError = "Login failed. Please check your token and try again."
                }
            } catch (e: Exception) {
                Log.error(e, "AniList login error")
                aniListLoginError = "Login error: ${e.message}"
            }
        }
    }
    
    fun logoutFromAniList() {
        scope.launch {
            try {
                trackingRepository.logout(TrackerService.ANILIST)
                _aniListLoggedIn.value = false
                showSnackbar("Logged out from AniList")
            } catch (e: Exception) {
                Log.error(e, "AniList logout error")
            }
        }
    }
    
    fun configureAniList() {
        showSnackbar("AniList is configured through login")
    }
    
    // ==================== MAL Functions ====================
    
    fun setMalEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("mal_enabled", false).set(enabled)
        _malEnabled.value = enabled
        if (!enabled) {
            logoutFromMal()
        }
    }
    
    fun loginToMal() {
        showMalLoginDialog = true
        malLoginError = null
        // MAL uses OAuth2 with PKCE - the auth URL will be generated by the repository
        _malAuthUrl.value = "https://myanimelist.net/v1/oauth2/authorize?response_type=code&client_id=6114d00ca681b7701d1e15fe11a4987e&code_challenge_method=plain"
    }
    
    fun dismissMalLoginDialog() {
        showMalLoginDialog = false
        _malAuthUrl.value = null
        malLoginError = null
    }
    
    fun completeMalLogin(authCode: String) {
        scope.launch {
            try {
                val credentials = TrackerCredentials(
                    serviceId = TrackerService.MYANIMELIST,
                    accessToken = authCode
                )
                val success = trackingRepository.authenticate(TrackerService.MYANIMELIST, credentials)
                
                if (success) {
                    _malLoggedIn.value = true
                    _malEnabled.value = true
                    preferenceStore.getBoolean("mal_enabled", false).set(true)
                    showMalLoginDialog = false
                    malLoginError = null
                    showSnackbar("Successfully logged in to MyAnimeList")
                } else {
                    malLoginError = "Login failed. Please check your authorization code."
                }
            } catch (e: Exception) {
                Log.error(e, "MAL login error")
                malLoginError = "Login error: ${e.message}"
            }
        }
    }
    
    fun logoutFromMal() {
        scope.launch {
            try {
                trackingRepository.logout(TrackerService.MYANIMELIST)
                _malLoggedIn.value = false
                showSnackbar("Logged out from MyAnimeList")
            } catch (e: Exception) {
                Log.error(e, "MAL logout error")
            }
        }
    }
    
    fun configureMal() {
        showSnackbar("MyAnimeList is configured through login")
    }
    
    // ==================== Kitsu Functions ====================
    
    fun setKitsuEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("kitsu_enabled", false).set(enabled)
        _kitsuEnabled.value = enabled
        if (!enabled) {
            logoutFromKitsu()
        }
    }
    
    fun loginToKitsu() {
        showKitsuLoginDialog = true
        kitsuLoginError = null
    }
    
    fun dismissKitsuLoginDialog() {
        showKitsuLoginDialog = false
        kitsuLoginError = null
    }
    
    fun completeKitsuLogin(username: String, password: String) {
        scope.launch {
            try {
                // Kitsu uses username/password authentication
                // We'll pass the credentials through the repository
                val credentials = TrackerCredentials(
                    serviceId = TrackerService.KITSU,
                    accessToken = "$username:$password", // Temporary encoding
                    username = username
                )
                val success = trackingRepository.authenticate(TrackerService.KITSU, credentials)
                
                if (success) {
                    _kitsuLoggedIn.value = true
                    _kitsuEnabled.value = true
                    preferenceStore.getBoolean("kitsu_enabled", false).set(true)
                    showKitsuLoginDialog = false
                    kitsuLoginError = null
                    showSnackbar("Successfully logged in to Kitsu")
                } else {
                    kitsuLoginError = "Login failed. Please check your credentials."
                }
            } catch (e: Exception) {
                Log.error(e, "Kitsu login error")
                kitsuLoginError = "Login error: ${e.message}"
            }
        }
    }
    
    fun logoutFromKitsu() {
        scope.launch {
            try {
                trackingRepository.logout(TrackerService.KITSU)
                _kitsuLoggedIn.value = false
                showSnackbar("Logged out from Kitsu")
            } catch (e: Exception) {
                Log.error(e, "Kitsu logout error")
            }
        }
    }
    
    fun configureKitsu() {
        showSnackbar("Kitsu is configured through login")
    }
    
    // ==================== MangaUpdates Functions ====================
    
    fun setMangaUpdatesEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("mangaupdates_enabled", false).set(enabled)
        _mangaUpdatesEnabled.value = enabled
        if (!enabled) {
            logoutFromMangaUpdates()
        }
    }
    
    fun loginToMangaUpdates() {
        showMangaUpdatesLoginDialog = true
        mangaUpdatesLoginError = null
    }
    
    fun dismissMangaUpdatesLoginDialog() {
        showMangaUpdatesLoginDialog = false
        mangaUpdatesLoginError = null
    }
    
    fun completeMangaUpdatesLogin(username: String, password: String) {
        scope.launch {
            try {
                val credentials = TrackerCredentials(
                    serviceId = TrackerService.MANGAUPDATES,
                    accessToken = "$username:$password",
                    username = username
                )
                val success = trackingRepository.authenticate(TrackerService.MANGAUPDATES, credentials)
                
                if (success) {
                    _mangaUpdatesLoggedIn.value = true
                    _mangaUpdatesEnabled.value = true
                    preferenceStore.getBoolean("mangaupdates_enabled", false).set(true)
                    showMangaUpdatesLoginDialog = false
                    mangaUpdatesLoginError = null
                    showSnackbar("Successfully logged in to MangaUpdates")
                } else {
                    mangaUpdatesLoginError = "Login failed. Please check your credentials."
                }
            } catch (e: Exception) {
                Log.error(e, "MangaUpdates login error")
                mangaUpdatesLoginError = "Login error: ${e.message}"
            }
        }
    }
    
    fun logoutFromMangaUpdates() {
        scope.launch {
            try {
                trackingRepository.logout(TrackerService.MANGAUPDATES)
                _mangaUpdatesLoggedIn.value = false
                showSnackbar("Logged out from MangaUpdates")
            } catch (e: Exception) {
                Log.error(e, "MangaUpdates logout error")
            }
        }
    }
    
    fun configureMangaUpdates() {
        showSnackbar("MangaUpdates is configured through login")
    }

    
    // ==================== Auto-Sync Functions ====================
    
    fun setAutoSyncEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("auto_sync_enabled", false).set(enabled)
        _autoSyncEnabled.value = enabled
        
        if (enabled) {
            scheduleAutoSync()
            showSnackbar("Auto-sync enabled")
        } else {
            cancelAutoSync()
            showSnackbar("Auto-sync disabled")
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
        _autoSyncInterval.value = interval
        
        if (_autoSyncEnabled.value) {
            scheduleAutoSync()
        }
    }
    
    fun setSyncOnlyOverWifi(enabled: Boolean) {
        preferenceStore.getBoolean("sync_only_over_wifi", true).set(enabled)
        _syncOnlyOverWifi.value = enabled
    }
    
    // ==================== Auto-Update Functions ====================
    
    fun setAutoUpdateStatus(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_status", true).set(enabled)
        _autoUpdateStatus.value = enabled
    }
    
    fun setAutoUpdateProgress(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_progress", true).set(enabled)
        _autoUpdateProgress.value = enabled
    }
    
    fun setAutoUpdateScore(enabled: Boolean) {
        preferenceStore.getBoolean("auto_update_score", false).set(enabled)
        _autoUpdateScore.value = enabled
    }
    
    // ==================== Manual Sync Functions ====================
    
    fun performManualSync() {
        if (_isSyncing.value) {
            showSnackbar("Sync already in progress")
            return
        }
        
        scope.launch {
            _isSyncing.value = true
            _syncError.value = null
            
            try {
                val stats = trackingRepository.getTrackingStatistics()
                
                if (stats.totalTrackedBooks == 0) {
                    showSnackbar("No tracked books to sync")
                    _isSyncing.value = false
                    return@launch
                }
                
                val enabledServices = trackingRepository.getEnabledServices()
                var syncedCount = 0
                var failedCount = 0
                
                for (service in enabledServices) {
                    try {
                        val tracks = trackingRepository.getTracksByService(service.id)
                        for (track in tracks) {
                            try {
                                val success = trackingRepository.syncTrack(track.mangaId, service.id)
                                if (success) syncedCount++ else failedCount++
                            } catch (e: Exception) {
                                Log.error(e, "Failed to sync track ${track.id}")
                                failedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Log.error(e, "Failed to get tracks for service ${service.name}")
                    }
                }
                
                val currentTime = currentTimeToLong()
                preferenceStore.getLong("last_sync_time", 0L).set(currentTime)
                _lastSyncTime.value = currentTime
                
                loadTrackingStatistics()
                
                if (failedCount == 0) {
                    showSnackbar("Synced $syncedCount tracks successfully")
                } else {
                    showSnackbar("Synced $syncedCount tracks, $failedCount failed")
                }
                
            } catch (e: Exception) {
                Log.error(e, "Manual sync failed")
                _syncError.value = e.message
                showSnackbar("Sync failed: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    // ==================== Clear Data Functions ====================
    
    fun showClearSyncDataDialog() {
        showClearSyncDataDialog = true
    }
    
    fun dismissClearSyncDataDialog() {
        showClearSyncDataDialog = false
    }
    
    fun clearAllSyncData() {
        scope.launch {
            try {
                // Logout from all services
                if (_aniListLoggedIn.value) {
                    trackingRepository.logout(TrackerService.ANILIST)
                    _aniListLoggedIn.value = false
                    _aniListEnabled.value = false
                    preferenceStore.getBoolean("anilist_enabled", false).set(false)
                }
                if (_malLoggedIn.value) {
                    trackingRepository.logout(TrackerService.MYANIMELIST)
                    _malLoggedIn.value = false
                    _malEnabled.value = false
                    preferenceStore.getBoolean("mal_enabled", false).set(false)
                }
                if (_kitsuLoggedIn.value) {
                    trackingRepository.logout(TrackerService.KITSU)
                    _kitsuLoggedIn.value = false
                    _kitsuEnabled.value = false
                    preferenceStore.getBoolean("kitsu_enabled", false).set(false)
                }
                if (_mangaUpdatesLoggedIn.value) {
                    trackingRepository.logout(TrackerService.MANGAUPDATES)
                    _mangaUpdatesLoggedIn.value = false
                    _mangaUpdatesEnabled.value = false
                    preferenceStore.getBoolean("mangaupdates_enabled", false).set(false)
                }
                
                preferenceStore.getBoolean("auto_sync_enabled", false).set(false)
                _autoSyncEnabled.value = false
                
                preferenceStore.getLong("last_sync_time", 0L).set(0L)
                _lastSyncTime.value = 0L
                
                cancelAutoSync()
                loadTrackingStatistics()
                
                showSnackbar("All sync data cleared")
            } catch (e: Exception) {
                Log.error(e, "Failed to clear sync data")
                showSnackbar("Failed to clear data: ${e.message}")
            }
        }
    }
    
    // ==================== Sync History Functions ====================
    
    fun navigateToSyncHistory() {
        showSyncHistoryDialog = true
    }
    
    fun dismissSyncHistoryDialog() {
        showSyncHistoryDialog = false
    }
    
    // ==================== Background Sync Functions ====================
    
    private fun scheduleAutoSync() {
        Log.debug { "Auto-sync scheduled with interval: ${_autoSyncInterval.value} minutes" }
    }
    
    private fun cancelAutoSync() {
        Log.debug { "Auto-sync cancelled" }
    }
    
    // ==================== Sync Status Functions ====================
    
    fun getSyncStatus(): Map<String, SyncStatus> {
        return mapOf(
            "anilist" to getSyncStatusForService(TrackerService.ANILIST),
            "mal" to getSyncStatusForService(TrackerService.MYANIMELIST),
            "kitsu" to getSyncStatusForService(TrackerService.KITSU),
            "mangaupdates" to getSyncStatusForService(TrackerService.MANGAUPDATES)
        )
    }
    
    private fun getSyncStatusForService(serviceId: Int): SyncStatus {
        val enabled = when (serviceId) {
            TrackerService.ANILIST -> _aniListEnabled.value
            TrackerService.MYANIMELIST -> _malEnabled.value
            TrackerService.KITSU -> _kitsuEnabled.value
            TrackerService.MANGAUPDATES -> _mangaUpdatesEnabled.value
            else -> false
        }
        
        val loggedIn = when (serviceId) {
            TrackerService.ANILIST -> _aniListLoggedIn.value
            TrackerService.MYANIMELIST -> _malLoggedIn.value
            TrackerService.KITSU -> _kitsuLoggedIn.value
            TrackerService.MANGAUPDATES -> _mangaUpdatesLoggedIn.value
            else -> false
        }
        
        return when {
            !enabled -> SyncStatus.DISABLED
            !loggedIn -> SyncStatus.NOT_LOGGED_IN
            _isSyncing.value -> SyncStatus.SYNCING
            _syncError.value != null -> SyncStatus.ERROR
            else -> SyncStatus.READY
        }
    }
    
    // ==================== Book Tracking Functions ====================
    
    fun trackBook(bookId: Long, serviceId: Int, searchResult: TrackSearchResult) {
        scope.launch {
            try {
                val success = trackingRepository.linkBook(bookId, serviceId, searchResult)
                if (success) {
                    loadTrackingStatistics()
                    showSnackbar("Added to tracking: ${searchResult.title}")
                } else {
                    showSnackbar("Failed to add tracking")
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to track book")
                showSnackbar("Failed to track: ${e.message}")
            }
        }
    }
    
    fun untrackBook(bookId: Long, serviceId: Int) {
        scope.launch {
            try {
                val success = trackingRepository.removeTrack(bookId, serviceId)
                if (success) {
                    loadTrackingStatistics()
                    showSnackbar("Removed from tracking")
                } else {
                    showSnackbar("Failed to remove tracking")
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to untrack book")
                showSnackbar("Failed to untrack: ${e.message}")
            }
        }
    }
    
    fun updateBookStatus(bookId: Long, status: TrackStatus) {
        scope.launch {
            try {
                val success = trackingRepository.updateStatus(bookId, status)
                if (success) {
                    showSnackbar("Status updated to ${status.name}")
                } else {
                    showSnackbar("Failed to update status")
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update book status")
                showSnackbar("Failed to update status: ${e.message}")
            }
        }
    }
    
    fun updateBookProgress(bookId: Long, progress: Int) {
        scope.launch {
            try {
                val success = trackingRepository.updateReadingProgress(bookId, progress)
                if (success) {
                    showSnackbar("Progress updated to $progress")
                } else {
                    showSnackbar("Failed to update progress")
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update book progress")
                showSnackbar("Failed to update progress: ${e.message}")
            }
        }
    }
    
    fun updateBookScore(bookId: Long, score: Float) {
        scope.launch {
            try {
                val success = trackingRepository.updateScore(bookId, score)
                if (success) {
                    showSnackbar("Score updated to ${"%.1f".format(score)}")
                } else {
                    showSnackbar("Failed to update score")
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to update book score")
                showSnackbar("Failed to update score: ${e.message}")
            }
        }
    }
    
    // ==================== Search Functions ====================
    
    private val _searchResults = MutableStateFlow<List<TrackSearchResult>>(emptyList())
    val searchResults: StateFlow<List<TrackSearchResult>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    fun searchBooks(query: String, serviceId: Int) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        scope.launch {
            _isSearching.value = true
            try {
                val results = trackingRepository.searchTracker(serviceId, query)
                _searchResults.value = results
                
                if (results.isEmpty()) {
                    showSnackbar("No results found for '$query'")
                }
            } catch (e: Exception) {
                Log.error(e, "Search failed")
                _searchResults.value = emptyList()
                showSnackbar("Search failed: ${e.message}")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
    
    // ==================== Utility Functions ====================
    
    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
    
    fun getFormattedLastSyncTime(): String {
        val time = _lastSyncTime.value
        if (time == 0L) return "Never"
        
        val now = currentTimeToLong()
        val diff = now - time
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            else -> "${diff / 86400_000} days ago"
        }
    }
}

enum class SyncStatus {
    DISABLED,
    NOT_LOGGED_IN,
    READY,
    SYNCING,
    ERROR
}
