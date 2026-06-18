package ireader.presentation.ui.readinghub

import androidx.compose.runtime.Stable
import ireader.core.log.Log
import ireader.domain.data.repository.ReadingChallengeRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.ReadingStatisticsType1
import ireader.domain.models.entities.ReaderLevel
import ireader.domain.models.gamification.ChallengeType
import ireader.domain.models.gamification.Milestone
import ireader.domain.models.gamification.MilestoneMetric
import ireader.domain.models.gamification.Milestones
import ireader.domain.models.gamification.ReadingChallenge
import ireader.domain.models.gamification.ReadingChallengeState
import ireader.domain.models.quote.*
import ireader.domain.preferences.prefs.ReadingBuddyPreferences
import ireader.domain.usecases.quote.ReadingBuddyUseCases
import ireader.domain.usecases.statistics.GetReadingStatisticsUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Unified state for the Reading Hub screen.
 * Combines statistics and reading buddy into a single source of truth.
 */
@Stable
data class ReadingHubState(
    // Statistics
    val statistics: ReadingStatisticsType1 = ReadingStatisticsType1(),
    
    // Reading Buddy
    val buddyState: ReadingBuddyState = ReadingBuddyState(),
    val unlockedAchievements: List<BuddyAchievement> = emptyList(),
    val allAchievements: List<BuddyAchievement> = BuddyAchievement.ALL_ACHIEVEMENTS,
    val levelProgress: Float = 0f,
    
    // Reading Challenges
    val challengeState: ReadingChallengeState = ReadingChallengeState(),
    
    // Milestones
    val currentMilestone: Milestone? = null,
    val showMilestoneCelebration: Boolean = false,
    val unseenMilestones: List<Milestone> = emptyList(),
    
    // Quote card style preference
    val selectedCardStyle: QuoteCardStyle = QuoteCardStyle.GRADIENT_SUNSET,
    
    // UI State
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showAchievementDialog: Boolean = false,
    val newAchievement: BuddyAchievement? = null,
    val isAdmin: Boolean = false,
    
    // Settings
    val showResetConfirmDialog: Boolean = false
)

/**
 * Unified ViewModel for the Reading Hub screen.
 * Single source of truth for all reading-related statistics and features.
 */
@Stable
class ReadingHubViewModel(
    private val statisticsRepository: ReadingStatisticsRepository,
    private val getReadingStatistics: GetReadingStatisticsUseCase,
    private val readingBuddyUseCases: ReadingBuddyUseCases,
    private val preferences: ReadingBuddyPreferences,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?,
    private val challengeRepository: ReadingChallengeRepository? = null
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(ReadingHubState())
    val state: StateFlow<ReadingHubState> = _state.asStateFlow()
    
    init {
        loadInitialData()
        observeStatistics()
        observeChallenges()
    }
    
    /**
     * Observe statistics changes reactively from the database.
     * This is the single source of truth for all statistics.
     */
    private fun observeStatistics() {
        scope.launch {
            getReadingStatistics().collect { stats ->
                val buddyState = createBuddyStateFromStats(stats)
                val achievements = try {
                    readingBuddyUseCases.getUnlockedAchievements()
                } catch (e: Exception) {
                    emptyList()
                }
                val levelProgress = ReaderLevel.fromMinutes(stats.totalReadingTimeMinutes).progress
                
                _state.update { current ->
                    current.copy(
                        statistics = stats,
                        buddyState = buddyState,
                        unlockedAchievements = achievements,
                        levelProgress = levelProgress
                    )
                }
                
                // Update challenge progress when stats change
                challengeRepository?.updateChallengeProgress(0)
                
                // Check for milestones
                checkMilestones(stats)
            }
        }
    }
    
    private fun observeChallenges() {
        val repo = challengeRepository ?: return
        scope.launch {
            repo.observeChallenges().collect { challengeState ->
                _state.update { it.copy(challengeState = challengeState) }
            }
        }
    }
    
    private fun checkMilestones(stats: ReadingStatisticsType1) {
        scope.launch {
            val repo = challengeRepository ?: return@launch
            val seen = repo.getSeenMilestones()
            val newMilestones = mutableListOf<Milestone>()
            
            Milestones.ALL.forEach { milestone ->
                if (milestone.id !in seen) {
                    val currentValue = when (milestone.metric) {
                        MilestoneMetric.BOOKS_READ -> stats.booksCompleted.toLong()
                        MilestoneMetric.CHAPTERS_READ -> stats.totalChaptersRead.toLong()
                        MilestoneMetric.READING_MINUTES -> stats.totalReadingTimeMinutes.toLong()
                        MilestoneMetric.STREAK_DAYS -> stats.readingStreak.toLong()
                    }
                    if (currentValue >= milestone.threshold) {
                        newMilestones.add(milestone)
                    }
                }
            }
            
            if (newMilestones.isNotEmpty()) {
                val firstMilestone = newMilestones.first()
                repo.markMilestoneSeen(firstMilestone.id)
                _state.update {
                    it.copy(
                        currentMilestone = firstMilestone,
                        showMilestoneCelebration = true,
                        unseenMilestones = newMilestones
                    )
                }
            }
        }
    }
    
    private fun createBuddyStateFromStats(stats: ReadingStatisticsType1): ReadingBuddyState {
        return ReadingBuddyState(
            level = ReaderLevel.fromMinutes(stats.totalReadingTimeMinutes).level,
            experience = stats.buddyExperience,
            currentStreak = stats.readingStreak,
            longestStreak = stats.longestStreak,
            totalChaptersRead = stats.totalChaptersRead,
            totalBooksRead = stats.booksCompleted,
            lastInteractionTime = stats.lastReadDate,
            mood = determineMood(stats),
            message = generateMessage(stats)
        )
    }
    
    private fun determineMood(stats: ReadingStatisticsType1): BuddyMood {
        return when {
            stats.readingStreak >= 7 -> BuddyMood.EXCITED
            stats.readingStreak >= 3 -> BuddyMood.HAPPY
            stats.lastReadDate == 0L -> BuddyMood.NEUTRAL
            else -> BuddyMood.HAPPY
        }
    }
    
    private fun generateMessage(stats: ReadingStatisticsType1): String {
        return when {
            stats.readingStreak >= 7 -> "Amazing! ${stats.readingStreak} day streak! 🔥"
            stats.readingStreak >= 3 -> "Great job! Keep the streak going!"
            stats.totalChaptersRead == 0 -> "Ready to start reading? 📚"
            else -> "Welcome back! Let's read together!"
        }
    }
    
    private fun loadInitialData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Check if user is admin
            val isAdmin = try {
                getCurrentUser()?.isAdmin == true
            } catch (e: Exception) {
                false
            }
            
            // Load card style preference
            val cardStyle = try {
                QuoteCardStyle.valueOf(preferences.preferredCardStyle().get())
            } catch (e: Exception) {
                QuoteCardStyle.GRADIENT_SUNSET
            }
            
            _state.update { current ->
                current.copy(
                    isAdmin = isAdmin,
                    selectedCardStyle = cardStyle,
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
    
    // Reset statistics
    fun showResetConfirmDialog() {
        _state.update { it.copy(showResetConfirmDialog = true) }
    }
    
    fun dismissResetConfirmDialog() {
        _state.update { it.copy(showResetConfirmDialog = false) }
    }
    
    fun createChallenge(type: ChallengeType, minutes: Long) {
        val repo = challengeRepository
        if (repo == null) {
            _state.update { it.copy(error = "Challenges not available") }
            return
        }
        scope.launch {
            try {
                when (type) {
                    ChallengeType.DAILY -> repo.createDailyGoal(minutes)
                    ChallengeType.WEEKLY -> repo.createWeeklyGoal(minutes)
                    ChallengeType.MONTHLY -> repo.createMonthlyGoal(minutes)
                }
                _state.update { it.copy(successMessage = "${type.label} goal set! 🎯") }
            } catch (e: Exception) {
                Log.error(e, "Failed to create challenge")
                _state.update { it.copy(error = "Failed to create goal: ${e.message}") }
            }
        }
    }
    
    fun dismissMilestoneCelebration() {
        _state.update {
            val remaining = it.unseenMilestones.drop(1)
            val next = remaining.firstOrNull()
            it.copy(
                showMilestoneCelebration = false,
                currentMilestone = next,
                unseenMilestones = remaining
            )
        }
    }
    
    fun resetStatistics() {
        scope.launch {
            try {
                statisticsRepository.resetStatistics()
                _state.update { 
                    it.copy(
                        showResetConfirmDialog = false,
                        successMessage = "Statistics reset successfully"
                    )
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to reset statistics")
                _state.update { 
                    it.copy(
                        showResetConfirmDialog = false,
                        error = "Failed to reset statistics: ${e.message}"
                    )
                }
            }
        }
    }
}
