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
    val error: String? = null
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
                lastSyncTime = supabasePreferences.lastSyncTime().get()
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
}
