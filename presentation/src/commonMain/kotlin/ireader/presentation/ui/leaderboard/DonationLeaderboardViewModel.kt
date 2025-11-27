package ireader.presentation.ui.leaderboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.DonationLeaderboardEntry
import ireader.domain.usecases.leaderboard.DonationLeaderboardUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DonationLeaderboardViewModel(
    private val donationLeaderboardUseCases: DonationLeaderboardUseCases
) : BaseViewModel() {
    
    var state by mutableStateOf(DonationLeaderboardState())
        private set
    
    private var realtimeJob: Job? = null
    
    init {
        loadDonationLeaderboard()
        loadUserDonationRank()
        
        // Start realtime updates if enabled
        if (donationLeaderboardUseCases.isRealtimeEnabled()) {
            startRealtimeUpdates()
        }
    }
    
    fun loadDonationLeaderboard() {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            
            donationLeaderboardUseCases.getDonationLeaderboard(limit = 100)
                .onSuccess { entries ->
                    state = state.copy(
                        leaderboard = entries,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        error = error.message ?: "Failed to load donation leaderboard",
                        isLoading = false
                    )
                }
        }
    }
    
    fun loadUserDonationRank() {
        scope.launch {
            donationLeaderboardUseCases.getUserDonationRank()
                .onSuccess { entry ->
                    state = state.copy(userRank = entry)
                }
                .onFailure {
                    // User might not be on leaderboard yet
                    state = state.copy(userRank = null)
                }
        }
    }
    
    fun toggleRealtimeUpdates(enabled: Boolean) {
        donationLeaderboardUseCases.setRealtimeEnabled(enabled)
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
            donationLeaderboardUseCases.observeDonationLeaderboard(limit = 100)
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
        state = state.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}

data class DonationLeaderboardState(
    val leaderboard: List<DonationLeaderboardEntry> = emptyList(),
    val userRank: DonationLeaderboardEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRealtimeEnabled: Boolean = false
)
