package ireader.presentation.ui.settings.sync

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.preferences.prefs.SupabasePreferences
import kotlinx.coroutines.flow.update
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
) : StateScreenModel<SupabaseConfigState>(SupabaseConfigState()) {
    
    init {
        loadConfiguration()
    }
    
    private fun loadConfiguration() {
        screenModelScope.launch {
            mutableState.update { it.copy(
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
        mutableState.update { it.copy(useCustomSupabase = useCustom) }
        screenModelScope.launch {
            supabasePreferences.useCustomSupabase().set(useCustom)
        }
    }
    
    fun setUrl(url: String) {
        mutableState.update { it.copy(supabaseUrl = url) }
    }
    
    fun setApiKey(apiKey: String) {
        mutableState.update { it.copy(supabaseApiKey = apiKey) }
    }
    
    fun setAutoSync(enabled: Boolean) {
        mutableState.update { it.copy(autoSyncEnabled = enabled) }
        screenModelScope.launch {
            supabasePreferences.autoSyncEnabled().set(enabled)
        }
    }
    
    fun setWifiOnly(wifiOnly: Boolean) {
        mutableState.update { it.copy(syncOnWifiOnly = wifiOnly) }
        screenModelScope.launch {
            supabasePreferences.syncOnWifiOnly().set(wifiOnly)
        }
    }
    
    fun saveConfiguration() {
        screenModelScope.launch {
            try {
                supabasePreferences.supabaseUrl().set(state.value.supabaseUrl)
                supabasePreferences.supabaseApiKey().set(state.value.supabaseApiKey)
                
                mutableState.update { it.copy(
                    testResult = "Configuration saved successfully!",
                    error = null
                )}
            } catch (e: Exception) {
                mutableState.update { it.copy(
                    error = "Failed to save configuration: ${e.message}"
                )}
            }
        }
    }
    
    fun testConnection() {
        screenModelScope.launch {
            mutableState.update { it.copy(isTesting = true, testResult = null) }
            
            try {
                // Test connection by trying to get current user
                val result = remoteRepository.getCurrentUser()
                
                if (result.isSuccess) {
                    mutableState.update { it.copy(
                        isTesting = false,
                        testResult = "✓ Connection successful! Supabase is configured correctly."
                    )}
                } else {
                    mutableState.update { it.copy(
                        isTesting = false,
                        testResult = "✗ Connection failed: ${result.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                mutableState.update { it.copy(
                    isTesting = false,
                    testResult = "✗ Connection failed: ${e.message}"
                )}
            }
        }
    }
    
    fun triggerManualSync() {
        screenModelScope.launch {
            mutableState.update { it.copy(isSyncing = true) }
            
            try {
                // Check if sync manager is available
                if (syncManager == null || bookRepository == null) {
                    mutableState.update { it.copy(
                        isSyncing = false,
                        error = "Sync not available. Please restart the app."
                    )}
                    return@launch
                }
                
                // Get current user
                val userResult = remoteRepository.getCurrentUser()
                val user = userResult.getOrNull()
                
                if (user == null) {
                    mutableState.update { it.copy(
                        isSyncing = false,
                        error = "Please sign in to sync"
                    )}
                    return@launch
                }
                
                // Get all books
                val books = bookRepository.findAllBooks()
                
                if (books.isEmpty()) {
                    mutableState.update { it.copy(
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
                    mutableState.update { it.copy(
                        isSyncing = false,
                        lastSyncTime = currentTime,
                        testResult = "✓ Synced $favoriteCount favorite books successfully!",
                        error = null
                    )}
                } else {
                    mutableState.update { it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${syncResult.exceptionOrNull()?.message}"
                    )}
                }
            } catch (e: Exception) {
                mutableState.update { it.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )}
            }
        }
    }
    
    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
    
    // Multi-endpoint configuration methods
    fun setUseMultiEndpoint(useMulti: Boolean) {
        mutableState.update { it.copy(useMultiEndpoint = useMulti) }
    }
    
    fun setBooksUrl(url: String) {
        mutableState.update { it.copy(booksUrl = url) }
    }
    
    fun setBooksApiKey(apiKey: String) {
        mutableState.update { it.copy(booksApiKey = apiKey) }
    }
    
    fun setProgressUrl(url: String) {
        mutableState.update { it.copy(progressUrl = url) }
    }
    
    fun setProgressApiKey(apiKey: String) {
        mutableState.update { it.copy(progressApiKey = apiKey) }
    }
    
    fun setReviewsUrl(url: String) {
        mutableState.update { it.copy(reviewsUrl = url) }
    }
    
    fun setReviewsApiKey(apiKey: String) {
        mutableState.update { it.copy(reviewsApiKey = apiKey) }
    }
    
    fun setCommunityUrl(url: String) {
        mutableState.update { it.copy(communityUrl = url) }
    }
    
    fun setCommunityApiKey(apiKey: String) {
        mutableState.update { it.copy(communityApiKey = apiKey) }
    }
    
    fun saveMultiEndpointConfiguration() {
        screenModelScope.launch {
            try {
                // Save primary endpoint
                supabasePreferences.supabaseUrl().set(state.value.supabaseUrl)
                supabasePreferences.supabaseApiKey().set(state.value.supabaseApiKey)
                supabasePreferences.useMultiEndpoint().set(state.value.useMultiEndpoint)
                
                // Save books endpoint
                supabasePreferences.booksUrl().set(state.value.booksUrl)
                supabasePreferences.booksApiKey().set(state.value.booksApiKey)
                supabasePreferences.booksEnabled().set(
                    state.value.booksUrl.isNotEmpty() && state.value.booksApiKey.isNotEmpty()
                )
                
                // Save progress endpoint
                supabasePreferences.progressUrl().set(state.value.progressUrl)
                supabasePreferences.progressApiKey().set(state.value.progressApiKey)
                supabasePreferences.progressEnabled().set(
                    state.value.progressUrl.isNotEmpty() && state.value.progressApiKey.isNotEmpty()
                )
                
                // Save reviews endpoint
                supabasePreferences.reviewsUrl().set(state.value.reviewsUrl)
                supabasePreferences.reviewsApiKey().set(state.value.reviewsApiKey)
                supabasePreferences.reviewsEnabled().set(
                    state.value.reviewsUrl.isNotEmpty() && state.value.reviewsApiKey.isNotEmpty()
                )
                
                // Save community endpoint
                supabasePreferences.communityUrl().set(state.value.communityUrl)
                supabasePreferences.communityApiKey().set(state.value.communityApiKey)
                supabasePreferences.communityEnabled().set(
                    state.value.communityUrl.isNotEmpty() && state.value.communityApiKey.isNotEmpty()
                )
                
                mutableState.update { it.copy(
                    testResult = "✓ Multi-endpoint configuration saved successfully!",
                    error = null
                )}
            } catch (e: Exception) {
                mutableState.update { it.copy(
                    error = "Failed to save multi-endpoint configuration: ${e.message}"
                )}
            }
        }
    }
}
