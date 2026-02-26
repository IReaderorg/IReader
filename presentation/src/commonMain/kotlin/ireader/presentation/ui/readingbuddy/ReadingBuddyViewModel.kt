package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Stable
import ireader.domain.models.quote.*
import ireader.domain.preferences.prefs.ReadingBuddyPreferences
import ireader.domain.usecases.quote.ReadingBuddyUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the Reading Buddy screen
 */
@Stable
data class ReadingBuddyScreenState(
    val buddyState: ReadingBuddyState = ReadingBuddyState(),
    val selectedCardStyle: QuoteCardStyle = QuoteCardStyle.GRADIENT_SUNSET,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val unlockedAchievements: List<BuddyAchievement> = emptyList(),
    val levelProgress: Float = 0f,
    val showAchievementDialog: Boolean = false,
    val newAchievement: BuddyAchievement? = null,
    val isAdmin: Boolean = false
)

/**
 * ViewModel for Reading Buddy feature.
 * Now uses unified database statistics via ReadingBuddyUseCases for consistent
 * data across Leaderboard, Statistics, and Reading Buddy screens.
 */
@Stable
class ReadingBuddyViewModel(
    private val readingBuddyUseCases: ReadingBuddyUseCases,
    private val preferences: ReadingBuddyPreferences, // Only for card style preference
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(ReadingBuddyScreenState())
    val state: StateFlow<ReadingBuddyScreenState> = _state.asStateFlow()
    
    init {
        loadInitialData()
        observeBuddyState()
    }
    
    /**
     * Observe buddy state changes reactively from the database
     */
    private fun observeBuddyState() {
        scope.launch {
            readingBuddyUseCases.getBuddyStateFlow().collect { buddyState ->
                val achievements = readingBuddyUseCases.getUnlockedAchievements()
                val levelProgress = readingBuddyUseCases.getLevelProgress()
                _state.update { current ->
                    current.copy(
                        buddyState = buddyState,
                        unlockedAchievements = achievements,
                        levelProgress = levelProgress
                    )
                }
            }
        }
    }
    
    private fun loadInitialData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Load buddy state from database
            val buddyState = readingBuddyUseCases.getBuddyState()
            val achievements = readingBuddyUseCases.getUnlockedAchievements()
            val levelProgress = readingBuddyUseCases.getLevelProgress()
            
            // Check if user is admin
            val isAdmin = try {
                getCurrentUser()?.isAdmin == true
            } catch (e: Exception) {
                false
            }
            
            _state.update { current ->
                current.copy(
                    buddyState = buddyState,
                    unlockedAchievements = achievements,
                    levelProgress = levelProgress,
                    isAdmin = isAdmin,
                    isLoading = false
                )
            }
        }
    }
    
    fun setCardStyle(style: QuoteCardStyle) {
        _state.update { it.copy(selectedCardStyle = style) }
        scope.launch {
            preferences.preferredCardStyle().set(style.name)
        }
    }
    
    fun onReadingStarted() {
        scope.launch {
            readingBuddyUseCases.onReadingStarted()
            // State will be updated via Flow observation
        }
    }
    
    fun onChapterCompleted() {
        scope.launch {
            val achievement = readingBuddyUseCases.onChapterCompleted()
            if (achievement != null) {
                _state.update { 
                    it.copy(
                        showAchievementDialog = true,
                        newAchievement = achievement
                    )
                }
            }
            // State will be updated via Flow observation
        }
    }
    
    fun onBookCompleted() {
        scope.launch {
            val achievement = readingBuddyUseCases.onBookCompleted()
            if (achievement != null) {
                _state.update { 
                    it.copy(
                        showAchievementDialog = true,
                        newAchievement = achievement
                    )
                }
            }
            // State will be updated via Flow observation
        }
    }
    
    fun dismissAchievementDialog() {
        _state.update { it.copy(showAchievementDialog = false, newAchievement = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
    
    fun showMessage(message: String) {
        _state.update { it.copy(successMessage = message) }
    }
    
    fun showError(error: String) {
        _state.update { it.copy(error = error) }
    }
}
