package ireader.presentation.ui.leaderboard

import androidx.compose.runtime.Stable
import ireader.domain.usecases.leaderboard.LeaderboardUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * ViewModel for the Reading Leaderboard screen following Mihon's StateScreenModel pattern.
 * 
 * Uses a single immutable StateFlow<LeaderboardScreenState> instead of mutableStateOf.
 * This provides:
 * - Single source of truth for UI state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 */
@Stable
class LeaderboardViewModel(
    private val leaderboardUseCases: LeaderboardUseCases
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(LeaderboardScreenState())
    val state: StateFlow<LeaderboardScreenState> = _state.asStateFlow()
    
    private var realtimeJob: Job? = null
    
    init {
        loadLeaderboard()
        loadUserRank()
        
        // Start realtime updates if enabled
        if (leaderboardUseCases.isRealtimeEnabled()) {
            startRealtimeUpdates()
        }
    }
    
    fun loadLeaderboard() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            leaderboardUseCases.getLeaderboard(limit = 100)
                .onSuccess { entries ->
                    _state.update { current ->
                        current.copy(
                            leaderboard = entries,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            error = error.message ?: "Failed to load leaderboard",
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun loadUserRank() {
        scope.launch {
            leaderboardUseCases.getUserRank()
                .onSuccess { entry ->
                    _state.update { it.copy(userRank = entry) }
                }
                .onFailure {
                    // User might not be on leaderboard yet
                    _state.update { it.copy(userRank = null) }
                }
        }
    }
    
    fun syncUserStats() {
        scope.launch {
            _state.update { it.copy(isSyncing = true, syncError = null) }
            
            leaderboardUseCases.syncCurrentUserStats()
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            isSyncing = false,
                            lastSyncTime = currentTimeToLong(),
                            syncError = null // Clear any previous errors
                        )
                    }
                    // Reload to get updated rank
                    loadUserRank()
                    loadLeaderboard()
                }
                .onFailure { error ->
                    // Parse error message to provide user-friendly feedback
                    val userFriendlyMessage = when {
                        error.message?.contains("not logged in", ignoreCase = true) == true ->
                            "Please sign in first (More → Profile & Sync)"
                        
                        error.message?.contains("duplicate key", ignoreCase = true) == true ->
                            "Stats updated successfully!" // This is actually success
                        
                        error.message?.contains("permission denied", ignoreCase = true) == true ->
                            "Permission denied. Please check your account."
                        
                        error.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Check your connection."
                        
                        error.message?.contains("JWT", ignoreCase = true) == true ->
                            "Session expired. Please sign in again."
                        
                        else -> error.message ?: "Failed to sync stats"
                    }
                    
                    // If it's a duplicate key error, treat it as success
                    if (error.message?.contains("duplicate key", ignoreCase = true) == true) {
                        _state.update { current ->
                            current.copy(
                                isSyncing = false,
                                lastSyncTime = currentTimeToLong(),
                                syncError = null
                            )
                        }
                        // Reload to get updated rank
                        loadUserRank()
                        loadLeaderboard()
                    } else {
                        _state.update { current ->
                            current.copy(
                                syncError = userFriendlyMessage,
                                isSyncing = false
                            )
                        }
                    }
                }
        }
    }
    
    fun toggleRealtimeUpdates(enabled: Boolean) {
        leaderboardUseCases.setRealtimeEnabled(enabled)
        _state.update { it.copy(isRealtimeEnabled = enabled) }
        
        if (enabled) {
            startRealtimeUpdates()
        } else {
            stopRealtimeUpdates()
        }
    }
    
    private fun startRealtimeUpdates() {
        realtimeJob?.cancel()
        realtimeJob = scope.launch {
            leaderboardUseCases.observeLeaderboard(limit = 100)
                .catch { error ->
                    _state.update { current ->
                        current.copy(error = "Realtime updates failed: ${error.message}")
                    }
                }
                .collectLatest { entries ->
                    _state.update { current ->
                        current.copy(
                            leaderboard = entries,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    private fun stopRealtimeUpdates() {
        realtimeJob?.cancel()
        realtimeJob = null
    }
    
    fun clearError() {
        _state.update { it.copy(error = null, syncError = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}
