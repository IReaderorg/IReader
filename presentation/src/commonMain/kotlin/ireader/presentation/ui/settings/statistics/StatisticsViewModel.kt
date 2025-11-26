package ireader.presentation.ui.settings.statistics

import ireader.data.statistics.StatisticsSyncService
import ireader.data.statistics.UserBadge
import ireader.domain.models.entities.ReadingStatisticsType1
import ireader.domain.usecases.statistics.StatisticsUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val statisticsUseCases: StatisticsUseCases
) : BaseViewModel() {

    val statistics: StateFlow<ReadingStatisticsType1> = statisticsUseCases
        .getReadingStatistics()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReadingStatisticsType1()
        )
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _userBadges = MutableStateFlow<List<UserBadge>>(emptyList())
    val userBadges: StateFlow<List<UserBadge>> = _userBadges.asStateFlow()
    
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val message: String) : SyncState()
        data class Error(val message: String) : SyncState()
    }

    init {
        refreshStatistics()
        loadUserBadges()
    }

    private fun refreshStatistics() {
        scope.launch {
            // Statistics are automatically updated via Flow
        }
    }
    
    /**
     * Manually trigger sync with Supabase
     * Can be called from UI (e.g., pull-to-refresh)
     */
    fun syncWithRemote() {
        scope.launch {
            try {
                _syncState.value = SyncState.Syncing
                
                // Trigger sync through use case
                val result = statisticsUseCases.syncStatistics()
                
                if (result.isSuccess) {
                    _syncState.value = SyncState.Success("Statistics synced successfully")
                } else {
                    _syncState.value = SyncState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
                }
                
                // Reload badges after sync
                loadUserBadges()
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            }
        }
    }
    
    /**
     * Load user badges from Supabase
     */
    private fun loadUserBadges() {
        scope.launch {
            try {
                // Note: This requires adding a method to fetch badges
                // For now, we'll use an empty list
                _userBadges.value = emptyList()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Clear sync state
     */
    fun clearSyncState() {
        _syncState.value = SyncState.Idle
    }
}
