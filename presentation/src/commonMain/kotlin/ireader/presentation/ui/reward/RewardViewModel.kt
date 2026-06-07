package ireader.presentation.ui.reward

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.Reward
import ireader.domain.models.entities.UserAchievement
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class RewardScreenState(
    val isLoading: Boolean = false,
    val currentLevel: Int = 1,
    val currentXp: Long = 0,
    val xpToNextLevel: Long = 60,
    val totalXp: Long = 0,
    val achievements: List<UserAchievement> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val error: String? = null
)

class RewardViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(RewardScreenState())
    val state: StateFlow<RewardScreenState> = _state.asStateFlow()

    init {
        loadRewards()
    }

    private fun loadRewards() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // For now, show empty state
                // TODO: Load from RewardRepository when implemented
                _state.update {
                    it.copy(
                        isLoading = false,
                        currentLevel = 1,
                        currentXp = 0,
                        xpToNextLevel = 60,
                        totalXp = 0
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load rewards"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadRewards()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
