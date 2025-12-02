package ireader.presentation.ui.leaderboard

import androidx.compose.runtime.Stable
import ireader.domain.usecases.leaderboard.DonationLeaderboardUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Donation Leaderboard screen following Mihon's StateScreenModel pattern.
 * 
 * Uses a single immutable StateFlow<DonationLeaderboardScreenState> instead of mutableStateOf.
 * This provides:
 * - Single source of truth for UI state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 */
@Stable
class DonationLeaderboardViewModel(
    private val donationLeaderboardUseCases: DonationLeaderboardUseCases
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(DonationLeaderboardScreenState())
    val state: StateFlow<DonationLeaderboardScreenState> = _state.asStateFlow()
    
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
            _state.update { it.copy(isLoading = true, error = null) }
            
            donationLeaderboardUseCases.getDonationLeaderboard(limit = 100)
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
                            error = error.message ?: "Failed to load donation leaderboard",
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun loadUserDonationRank() {
        scope.launch {
            donationLeaderboardUseCases.getUserDonationRank()
                .onSuccess { entry ->
                    _state.update { it.copy(userRank = entry) }
                }
                .onFailure {
                    // User might not be on leaderboard yet
                    _state.update { it.copy(userRank = null) }
                }
        }
    }
    
    fun toggleRealtimeUpdates(enabled: Boolean) {
        donationLeaderboardUseCases.setRealtimeEnabled(enabled)
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
            donationLeaderboardUseCases.observeDonationLeaderboard(limit = 100)
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
        _state.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}
