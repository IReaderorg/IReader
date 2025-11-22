package ireader.presentation.ui.leaderboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.usecases.leaderboard.LeaderboardUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val leaderboardUseCases: LeaderboardUseCases
) : BaseViewModel() {
    
    var state by mutableStateOf(LeaderboardState())
        private set
    
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
            state = state.copy(isLoading = true, error = null)
            
            leaderboardUseCases.getLeaderboard(limit = 100)
                .onSuccess { entries ->
                    state = state.copy(
                        leaderboard = entries,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        error = error.message ?: "Failed to load leaderboard",
                        isLoading = false
                    )
                }
        }
    }
    
    fun loadUserRank() {
        scope.launch {
            leaderboardUseCases.getUserRank()
                .onSuccess { entry ->
                    state = state.copy(userRank = entry)
                }
                .onFailure {
                    // User might not be on leaderboard yet
                    state = state.copy(userRank = null)
                }
        }
    }
    
    fun syncUserStats() {
        scope.launch {
            state = state.copy(isSyncing = true, syncError = null)
            
            leaderboardUseCases.syncCurrentUserStats()
                .onSuccess {
                    state = state.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    // Reload to get updated rank
                    loadUserRank()
                    loadLeaderboard()
                }
                .onFailure { error ->
                    state = state.copy(
                        syncError = error.message ?: "Failed to sync stats",
                        isSyncing = false
                    )
                }
        }
    }
    
    fun toggleRealtimeUpdates(enabled: Boolean) {
        leaderboardUseCases.setRealtimeEnabled(enabled)
        state = state.copy(isRealtimeEnabled = enabled)
        
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
                    state = state.copy(
                        error = "Realtime updates failed: ${error.message}"
                    )
                }
                .collectLatest { entries ->
                    state = state.copy(
                        leaderboard = entries,
                        isLoading = false
                    )
                }
        }
    }
    
    private fun stopRealtimeUpdates() {
        realtimeJob?.cancel()
        realtimeJob = null
    }
    
    fun clearError() {
        state = state.copy(error = null, syncError = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}

data class LeaderboardState(
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userRank: LeaderboardEntry? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val syncError: String? = null,
    val lastSyncTime: Long = 0,
    val isRealtimeEnabled: Boolean = false
)
