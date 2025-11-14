package ireader.presentation.ui.settings.sync


import ireader.domain.data.repository.RemoteRepository
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

data class SupabaseConfigState(
    val useCustomSupabase: Boolean = false,
    val supabaseUrl: String = "",
    val supabaseApiKey: String = "",
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isTesting: Boolean = false,
    val isSyncing: Boolean = false,
    val testResult: String? = null,
    val error: String? = null,
    // Multi-endpoint configuration
    val useMultiEndpoint: Boolean = false,
    val booksUrl: String = "",
    val booksApiKey: String = "",
    val progressUrl: String = "",
    val progressApiKey: String = "",
    val reviewsUrl: String = "",
    val reviewsApiKey: String = "",
    val communityUrl: String = "",
    val communityApiKey: String = ""
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
            updateState { it.copy(
                useCustomSupabase = supabasePreferences.useCustomSupabase().get(),
                supabaseUrl = supabasePreferences.supabaseUrl().get(),
                supabaseApiKey = supabasePreferences.supabaseApiKey().get(),
                autoSyncEnabled = supabasePreferences.autoSyncEnabled().get(),
                syncOnWifiOnly = supabasePreferences.syncOnWifiOnly().get(),
                lastSyncTime = supabasePreferences.lastSyncTime().get(),
                // Multi-endpoint
                useMultiEndpoint = supabasePreferences.useMultiEndpoint().get(),
                booksUrl = supabasePreferences.booksUrl().get(),
                booksApiKey = supabasePreferences.booksApiKey().get(),
                progressUrl = supabasePreferences.progressUrl().get(),
                progressApiKey = supabasePreferences.progressApiKey().get(),
                reviewsUrl = supabasePreferences.reviewsUrl().get(),
                reviewsApiKey = supabasePreferences.reviewsApiKey().get(),
                communityUrl = supabasePreferences.communityUrl().get(),
                communityApiKey = supabasePreferences.communityApiKey().get()
            )}
        }
    }
    
    fun setUseCustom(useCustom: Boolean) {
        updateState { it.copy(useCustomSupabase = useCustom) }
        scope.launch {
            supabasePreferences.useCustomSupabase().set(useCustom)
        }
    }
    
    fun setUrl(url: String) {
        updateState { it.copy(supabaseUrl = url) }
    }
    
    fun setApiKey(apiKey: String) {
        updateState { it.copy(supabaseApiKey = apiKey) }
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
                supabasePreferences.supabaseUrl().set(currentState.supabaseUrl)
                supabasePreferences.supabaseApiKey().set(currentState.supabaseApiKey)
                
                updateState { it.copy(
                    testResult = "Configuration saved successfully!",
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
                        testResult = "✓ Connection successful! Supabase is configured correctly."
                    )}
                } else {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = "✗ Connection failed: ${result.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isTesting = false,
                    testResult = "✗ Connection failed: ${e.message}"
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
                        lastSyncTime = System.currentTimeMillis(),
                        error = "No books to sync"
                    )}
                    return@launch
                }
                
                // Perform full sync
                val syncResult = syncManager.performFullSync(user.id, books)
                
                if (syncResult.isSuccess) {
                    val currentTime = System.currentTimeMillis()
                    supabasePreferences.lastSyncTime().set(currentTime)
                    
                    val favoriteCount = books.count { it.favorite }
                    updateState { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTime,
                        testResult = "✓ Synced $favoriteCount favorite books successfully!",
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
    
    // Multi-endpoint configuration methods
    fun setUseMultiEndpoint(useMulti: Boolean) {
        updateState { it.copy(useMultiEndpoint = useMulti) }
    }
    
    fun setBooksUrl(url: String) {
        updateState { it.copy(booksUrl = url) }
    }
    
    fun setBooksApiKey(apiKey: String) {
        updateState { it.copy(booksApiKey = apiKey) }
    }
    
    fun setProgressUrl(url: String) {
        updateState { it.copy(progressUrl = url) }
    }
    
    fun setProgressApiKey(apiKey: String) {
        updateState { it.copy(progressApiKey = apiKey) }
    }
    
    fun setReviewsUrl(url: String) {
        updateState { it.copy(reviewsUrl = url) }
    }
    
    fun setReviewsApiKey(apiKey: String) {
        updateState { it.copy(reviewsApiKey = apiKey) }
    }
    
    fun setCommunityUrl(url: String) {
        updateState { it.copy(communityUrl = url) }
    }
    
    fun setCommunityApiKey(apiKey: String) {
        updateState { it.copy(communityApiKey = apiKey) }
    }
    
    fun saveMultiEndpointConfiguration() {
        scope.launch {
            try {
                // Save primary endpoint
                supabasePreferences.supabaseUrl().set(currentState.supabaseUrl)
                supabasePreferences.supabaseApiKey().set(currentState.supabaseApiKey)
                supabasePreferences.useMultiEndpoint().set(currentState.useMultiEndpoint)
                
                // Save books endpoint
                supabasePreferences.booksUrl().set(currentState.booksUrl)
                supabasePreferences.booksApiKey().set(currentState.booksApiKey)
                supabasePreferences.booksEnabled().set(
                    currentState.booksUrl.isNotEmpty() && currentState.booksApiKey.isNotEmpty()
                )
                
                // Save progress endpoint
                supabasePreferences.progressUrl().set(currentState.progressUrl)
                supabasePreferences.progressApiKey().set(currentState.progressApiKey)
                supabasePreferences.progressEnabled().set(
                    currentState.progressUrl.isNotEmpty() && currentState.progressApiKey.isNotEmpty()
                )
                
                // Save reviews endpoint
                supabasePreferences.reviewsUrl().set(currentState.reviewsUrl)
                supabasePreferences.reviewsApiKey().set(currentState.reviewsApiKey)
                supabasePreferences.reviewsEnabled().set(
                    currentState.reviewsUrl.isNotEmpty() && currentState.reviewsApiKey.isNotEmpty()
                )
                
                // Save community endpoint
                supabasePreferences.communityUrl().set(currentState.communityUrl)
                supabasePreferences.communityApiKey().set(currentState.communityApiKey)
                supabasePreferences.communityEnabled().set(
                    currentState.communityUrl.isNotEmpty() && currentState.communityApiKey.isNotEmpty()
                )
                
                updateState { it.copy(
                    testResult = "✓ Multi-endpoint configuration saved successfully!",
                    error = null
                )}
            } catch (e: Exception) {
                updateState { it.copy(
                    error = "Failed to save multi-endpoint configuration: ${e.message}"
                )}
            }
        }
    }
}
