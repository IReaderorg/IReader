package ireader.presentation.ui.settings.sync


import ireader.domain.data.repository.RemoteRepository
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

data class SupabaseConfigState(
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isTesting: Boolean = false,
    val isSyncing: Boolean = false,
    val testResult: String? = null,
    val error: String? = null,
    // Custom configuration toggle
    val useCustomSupabase: Boolean = false,
    // Default configuration (from local.properties/config.properties)
    val hasDefaultConfig: Boolean = false,
    // 7-Project configuration
    // Project 1 - Auth
    val authUrl: String = "",
    val authApiKey: String = "",
    // Project 2 - Reading
    val readingUrl: String = "",
    val readingApiKey: String = "",
    // Project 3 - Library
    val libraryUrl: String = "",
    val libraryApiKey: String = "",
    // Project 4 - Book Reviews
    val bookReviewsUrl: String = "",
    val bookReviewsApiKey: String = "",
    // Project 5 - Chapter Reviews
    val chapterReviewsUrl: String = "",
    val chapterReviewsApiKey: String = "",
    // Project 6 - Badges
    val badgesUrl: String = "",
    val badgesApiKey: String = "",
    // Project 7 - Analytics
    val analyticsUrl: String = "",
    val analyticsApiKey: String = ""
)

class SupabaseConfigViewModel(
    private val supabasePreferences: SupabasePreferences,
    private val remoteRepository: RemoteRepository,
    private val syncManager: ireader.domain.services.SyncManager? = null,
    private val bookRepository: ireader.domain.data.repository.BookRepository? = null
) : StateViewModel<SupabaseConfigState>(SupabaseConfigState()) {
    
    init {
        loadConfiguration()
    }
    
    private fun loadConfiguration() {
        scope.launch {
            // Check if default config exists (from local.properties/config.properties)
            val hasDefault = try {
                val defaultAuthUrl = ireader.domain.config.PlatformConfig.getSupabaseAuthUrl()
                defaultAuthUrl.isNotEmpty()
            } catch (e: Exception) {
                false
            }
            
            updateState { it.copy(
                autoSyncEnabled = supabasePreferences.autoSyncEnabled().get(),
                syncOnWifiOnly = supabasePreferences.syncOnWifiOnly().get(),
                lastSyncTime = supabasePreferences.lastSyncTime().get(),
                useCustomSupabase = supabasePreferences.useCustomSupabase().get(),
                hasDefaultConfig = hasDefault,
                // 7-Project configuration (user overrides)
                authUrl = supabasePreferences.supabaseAuthUrl().get(),
                authApiKey = supabasePreferences.supabaseAuthKey().get(),
                readingUrl = supabasePreferences.supabaseReadingUrl().get(),
                readingApiKey = supabasePreferences.supabaseReadingKey().get(),
                libraryUrl = supabasePreferences.supabaseLibraryUrl().get(),
                libraryApiKey = supabasePreferences.supabaseLibraryKey().get(),
                bookReviewsUrl = supabasePreferences.supabaseBookReviewsUrl().get(),
                bookReviewsApiKey = supabasePreferences.supabaseBookReviewsKey().get(),
                chapterReviewsUrl = supabasePreferences.supabaseChapterReviewsUrl().get(),
                chapterReviewsApiKey = supabasePreferences.supabaseChapterReviewsKey().get(),
                badgesUrl = supabasePreferences.supabaseBadgesUrl().get(),
                badgesApiKey = supabasePreferences.supabaseBadgesKey().get(),
                analyticsUrl = supabasePreferences.supabaseAnalyticsUrl().get(),
                analyticsApiKey = supabasePreferences.supabaseAnalyticsKey().get()
            )}
        }
    }
    
    fun setUseCustomSupabase(useCustom: Boolean) {
        updateState { it.copy(useCustomSupabase = useCustom) }
        scope.launch {
            supabasePreferences.useCustomSupabase().set(useCustom)
        }
    }
    
    fun fillAllWithSame(url: String, apiKey: String) {
        updateState { it.copy(
            authUrl = url,
            authApiKey = apiKey,
            readingUrl = url,
            readingApiKey = apiKey,
            libraryUrl = url,
            libraryApiKey = apiKey,
            bookReviewsUrl = url,
            bookReviewsApiKey = apiKey,
            chapterReviewsUrl = url,
            chapterReviewsApiKey = apiKey,
            badgesUrl = url,
            badgesApiKey = apiKey,
            analyticsUrl = url,
            analyticsApiKey = apiKey
        )}
    }
    
    fun setAutoSync(enabled: Boolean) {
        updateState { it.copy(autoSyncEnabled = enabled) }
        scope.launch {
            supabasePreferences.autoSyncEnabled().set(enabled)
        }
    }
    
    fun setWifiOnly(wifiOnly: Boolean) {
        updateState { it.copy(syncOnWifiOnly = wifiOnly) }
        scope.launch {
            supabasePreferences.syncOnWifiOnly().set(wifiOnly)
        }
    }
    
    fun saveConfiguration() {
        scope.launch {
            try {
                // Save all 7 project configurations
                supabasePreferences.supabaseAuthUrl().set(currentState.authUrl)
                supabasePreferences.supabaseAuthKey().set(currentState.authApiKey)
                supabasePreferences.supabaseReadingUrl().set(currentState.readingUrl)
                supabasePreferences.supabaseReadingKey().set(currentState.readingApiKey)
                supabasePreferences.supabaseLibraryUrl().set(currentState.libraryUrl)
                supabasePreferences.supabaseLibraryKey().set(currentState.libraryApiKey)
                supabasePreferences.supabaseBookReviewsUrl().set(currentState.bookReviewsUrl)
                supabasePreferences.supabaseBookReviewsKey().set(currentState.bookReviewsApiKey)
                supabasePreferences.supabaseChapterReviewsUrl().set(currentState.chapterReviewsUrl)
                supabasePreferences.supabaseChapterReviewsKey().set(currentState.chapterReviewsApiKey)
                supabasePreferences.supabaseBadgesUrl().set(currentState.badgesUrl)
                supabasePreferences.supabaseBadgesKey().set(currentState.badgesApiKey)
                supabasePreferences.supabaseAnalyticsUrl().set(currentState.analyticsUrl)
                supabasePreferences.supabaseAnalyticsKey().set(currentState.analyticsApiKey)
                
                updateState { it.copy(
                    testResult = "? Configuration saved successfully! Total storage: 3.5GB",
                    error = null
                )}
            } catch (e: Exception) {
                updateState { it.copy(
                    error = "Failed to save configuration: ${e.message}"
                )}
            }
        }
    }
    
    fun testConnection() {
        scope.launch {
            updateState { it.copy(isTesting = true, testResult = null) }
            
            try {
                // Test connection by trying to get current user
                val result = remoteRepository.getCurrentUser()
                
                if (result.isSuccess) {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "? Connection successful! Supabase is configured correctly."
                    )}
                } else {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "? Connection failed: ${result.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isTesting = false,
                    testResult = "? Connection failed: ${e.message}"
                )}
            }
        }
    }
    
    fun triggerManualSync() {
        scope.launch {
            updateState { it.copy(isSyncing = true) }
            
            try {
                // Check if sync manager is available
                if (syncManager == null || bookRepository == null) {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Sync not available. Please restart the app."
                    )}
                    return@launch
                }
                
                // Get current user
                val userResult = remoteRepository.getCurrentUser()
                val user = userResult.getOrNull()
                
                if (user == null) {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Please sign in to sync"
                    )}
                    return@launch
                }
                
                // Get all books
                val books = bookRepository.findAllBooks()
                
                if (books.isEmpty()) {
                    updateState { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTimeToLong(),
                        error = "No books to sync"
                    )}
                    return@launch
                }
                
                // Perform full sync
                val syncResult = syncManager.performFullSync(user.id, books)
                
                if (syncResult.isSuccess) {
                    val currentTime = currentTimeToLong()
                    supabasePreferences.lastSyncTime().set(currentTime)
                    
                    val favoriteCount = books.count { it.favorite }
                    updateState { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTime,
                        testResult = "? Synced $favoriteCount favorite books successfully!",
                        error = null
                    )}
                } else {
                    updateState { it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${syncResult.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )}
            }
        }
    }
    
    fun clearError() {
        updateState { it.copy(error = null) }
    }
    
    // Individual project setters
    fun setAuthUrl(url: String) {
        updateState { it.copy(authUrl = url) }
    }
    
    fun setAuthApiKey(apiKey: String) {
        updateState { it.copy(authApiKey = apiKey) }
    }
    
    fun setReadingUrl(url: String) {
        updateState { it.copy(readingUrl = url) }
    }
    
    fun setReadingApiKey(apiKey: String) {
        updateState { it.copy(readingApiKey = apiKey) }
    }
    
    fun setLibraryUrl(url: String) {
        updateState { it.copy(libraryUrl = url) }
    }
    
    fun setLibraryApiKey(apiKey: String) {
        updateState { it.copy(libraryApiKey = apiKey) }
    }
    
    fun setBookReviewsUrl(url: String) {
        updateState { it.copy(bookReviewsUrl = url) }
    }
    
    fun setBookReviewsApiKey(apiKey: String) {
        updateState { it.copy(bookReviewsApiKey = apiKey) }
    }
    
    fun setChapterReviewsUrl(url: String) {
        updateState { it.copy(chapterReviewsUrl = url) }
    }
    
    fun setChapterReviewsApiKey(apiKey: String) {
        updateState { it.copy(chapterReviewsApiKey = apiKey) }
    }
    
    fun setBadgesUrl(url: String) {
        updateState { it.copy(badgesUrl = url) }
    }
    
    fun setBadgesApiKey(apiKey: String) {
        updateState { it.copy(badgesApiKey = apiKey) }
    }
    
    fun setAnalyticsUrl(url: String) {
        updateState { it.copy(analyticsUrl = url) }
    }
    
    fun setAnalyticsApiKey(apiKey: String) {
        updateState { it.copy(analyticsApiKey = apiKey) }
    }
}
