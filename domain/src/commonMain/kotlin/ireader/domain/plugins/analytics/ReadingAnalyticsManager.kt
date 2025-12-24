package ireader.domain.plugins.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ireader.core.util.createICoroutineScope
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.formatIsoDate

/**
 * Manager for Reading Analytics.
 * Provides API for plugins to access reading statistics.
 */
class ReadingAnalyticsManager(
    private val analyticsRepository: ReadingAnalyticsRepository,
    private val goalRepository: ReadingGoalRepository,
    private val achievementRepository: AchievementRepository
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private var sessionTrackingJob: Job? = null
    
    private val _currentSession = MutableStateFlow<ReadingSession?>(null)
    val currentSession: StateFlow<ReadingSession?> = _currentSession.asStateFlow()
    
    private val _todayStats = MutableStateFlow<DailyReadingStats?>(null)
    val todayStats: StateFlow<DailyReadingStats?> = _todayStats.asStateFlow()
    
    private val _activeGoals = MutableStateFlow<List<ReadingGoal>>(emptyList())
    val activeGoals: StateFlow<List<ReadingGoal>> = _activeGoals.asStateFlow()
    
    private val _recentAchievements = MutableStateFlow<List<ReadingAchievement>>(emptyList())
    val recentAchievements: StateFlow<List<ReadingAchievement>> = _recentAchievements.asStateFlow()
    
    private val _events = MutableSharedFlow<ReadingAnalyticsEvent>(replay = 0)
    val events: Flow<ReadingAnalyticsEvent> = _events.asSharedFlow()
    
    // Session tracking
    
    suspend fun startSession(
        bookId: Long,
        bookTitle: String,
        chapterId: Long,
        position: Int,
        deviceType: String
    ): ReadingSession {
        // End any existing session
        _currentSession.value?.let { endSession() }
        
        val session = ReadingSession(
            id = "session_${currentTimeToLong()}",
            bookId = bookId,
            bookTitle = bookTitle,
            startTime = currentTimeToLong(),
            endTime = null,
            startChapterId = chapterId,
            endChapterId = null,
            startPosition = position,
            endPosition = null,
            deviceType = deviceType
        )
        
        _currentSession.value = session
        _events.emit(ReadingAnalyticsEvent.SessionStarted(session))
        
        // Start periodic tracking
        startSessionTracking()
        
        return session
    }
    
    suspend fun updateSession(
        chapterId: Long,
        position: Int,
        wordsRead: Int,
        pagesRead: Int
    ) {
        val session = _currentSession.value ?: return
        
        val updated = session.copy(
            endChapterId = chapterId,
            endPosition = position,
            wordsRead = session.wordsRead + wordsRead,
            pagesRead = session.pagesRead + pagesRead
        )
        
        _currentSession.value = updated
    }
    
    suspend fun pauseSession() {
        val session = _currentSession.value ?: return
        sessionTrackingJob?.cancel()
        _events.emit(ReadingAnalyticsEvent.SessionPaused(session.id))
    }
    
    suspend fun resumeSession() {
        val session = _currentSession.value ?: return
        startSessionTracking()
        _events.emit(ReadingAnalyticsEvent.SessionResumed(session.id))
    }

    suspend fun endSession(): ReadingSession? {
        val session = _currentSession.value ?: return null
        sessionTrackingJob?.cancel()
        
        val completed = session.copy(
            endTime = currentTimeToLong(),
            isCompleted = true
        )
        
        // Save session
        analyticsRepository.saveSession(completed)
        
        // Update daily stats
        refreshTodayStats()
        
        // Check goals
        checkGoalProgress(completed)
        
        // Check achievements
        checkAchievements(completed)
        
        _currentSession.value = null
        _events.emit(ReadingAnalyticsEvent.SessionEnded(completed))
        
        return completed
    }
    
    private fun startSessionTracking() {
        sessionTrackingJob?.cancel()
        sessionTrackingJob = scope.launch {
            while (isActive) {
                delay(60_000) // Update every minute
                _currentSession.value?.let { session ->
                    // Auto-save progress
                    analyticsRepository.saveSession(session.copy(endTime = currentTimeToLong()))
                }
            }
        }
    }
    
    // Statistics retrieval
    
    suspend fun getDailyStats(date: String): DailyReadingStats? {
        return analyticsRepository.getDailyStats(date)
    }
    
    suspend fun getWeeklyStats(): List<DailyReadingStats> {
        return analyticsRepository.getWeeklyStats()
    }
    
    suspend fun getMonthlyStats(year: Int, month: Int): List<DailyReadingStats> {
        return analyticsRepository.getMonthlyStats(year, month)
    }
    
    suspend fun getBookStats(bookId: Long): BookReadingStats? {
        return analyticsRepository.getBookStats(bookId)
    }
    
    suspend fun getOverallStats(): OverallReadingStats {
        return analyticsRepository.getOverallStats()
    }
    
    suspend fun getReadingPattern(): ReadingPattern {
        return analyticsRepository.getReadingPattern()
    }
    
    suspend fun getReadingHeatmap(year: Int): ReadingHeatmap {
        return analyticsRepository.getReadingHeatmap(year)
    }
    
    suspend fun getSpeedAnalysis(): ReadingSpeedAnalysis {
        return analyticsRepository.getSpeedAnalysis()
    }
    
    suspend fun comparePeriods(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long
    ): PeriodComparison {
        return analyticsRepository.comparePeriods(currentStart, currentEnd, previousStart, previousEnd)
    }
    
    suspend fun getRecentSessions(limit: Int = 10): List<ReadingSession> {
        return analyticsRepository.getRecentSessions(limit)
    }
    
    suspend fun getSessionsForBook(bookId: Long): List<ReadingSession> {
        return analyticsRepository.getSessionsForBook(bookId)
    }
    
    // Goals
    
    suspend fun loadActiveGoals() {
        _activeGoals.value = goalRepository.getActiveGoals()
    }
    
    suspend fun createGoal(
        type: GoalType,
        target: Int,
        period: GoalPeriod,
        startDate: Long = currentTimeToLong(),
        endDate: Long? = null
    ): Result<ReadingGoal> {
        val goal = ReadingGoal(
            id = "goal_${currentTimeToLong()}",
            type = type,
            target = target,
            period = period,
            startDate = startDate,
            endDate = endDate,
            currentProgress = 0,
            isActive = true,
            isCompleted = false,
            completedDate = null,
            streakDays = 0
        )
        
        return try {
            goalRepository.saveGoal(goal)
            _activeGoals.value = _activeGoals.value + goal
            _events.emit(ReadingAnalyticsEvent.GoalCreated(goal))
            Result.success(goal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGoalProgress(goalId: String, progress: Int) {
        val goal = _activeGoals.value.find { it.id == goalId } ?: return
        
        val updated = goal.copy(
            currentProgress = progress,
            isCompleted = progress >= goal.target,
            completedDate = if (progress >= goal.target && !goal.isCompleted) currentTimeToLong() else goal.completedDate
        )
        
        goalRepository.saveGoal(updated)
        _activeGoals.value = _activeGoals.value.map { if (it.id == goalId) updated else it }
        
        if (updated.isCompleted && !goal.isCompleted) {
            _events.emit(ReadingAnalyticsEvent.GoalCompleted(updated))
        }
    }
    
    suspend fun deleteGoal(goalId: String) {
        goalRepository.deleteGoal(goalId)
        _activeGoals.value = _activeGoals.value.filter { it.id != goalId }
    }
    
    private suspend fun checkGoalProgress(session: ReadingSession) {
        for (goal in _activeGoals.value) {
            if (!goal.isActive || goal.isCompleted) continue
            
            val increment = when (goal.type) {
                GoalType.TIME_MINUTES -> (session.durationMs / 60_000).toInt()
                GoalType.WORDS -> session.wordsRead
                GoalType.PAGES -> session.pagesRead
                GoalType.SESSIONS -> 1
                GoalType.CHAPTERS -> if (session.endChapterId != session.startChapterId) 1 else 0
                GoalType.BOOKS -> 0 // Handled separately on book completion
            }
            
            if (increment > 0) {
                updateGoalProgress(goal.id, goal.currentProgress + increment)
            }
        }
    }
    
    // Achievements
    
    suspend fun loadAchievements(): List<ReadingAchievement> {
        return achievementRepository.getAllAchievements()
    }
    
    suspend fun getUnlockedAchievements(): List<ReadingAchievement> {
        return achievementRepository.getUnlockedAchievements()
    }
    
    suspend fun getAchievementProgress(): Map<String, Int> {
        return achievementRepository.getAchievementProgress()
    }
    
    private suspend fun checkAchievements(session: ReadingSession) {
        val stats = getOverallStats()
        val newlyUnlocked = mutableListOf<ReadingAchievement>()
        
        // Check various achievement conditions
        val achievements = achievementRepository.getAllAchievements()
        
        for (achievement in achievements) {
            if (achievement.isUnlocked) continue
            
            val shouldUnlock = when (achievement.category) {
                AchievementCategory.READING_TIME -> 
                    stats.totalReadingTimeMs >= achievement.requirement.value * 60_000L
                AchievementCategory.BOOKS_COMPLETED -> 
                    stats.totalBooksCompleted >= achievement.requirement.value
                AchievementCategory.STREAK -> 
                    stats.currentStreak >= achievement.requirement.value
                AchievementCategory.SPEED -> 
                    stats.averageWordsPerMinute >= achievement.requirement.value
                else -> false
            }
            
            if (shouldUnlock) {
                val unlocked = achievement.copy(
                    isUnlocked = true,
                    unlockedDate = currentTimeToLong(),
                    progress = achievement.requirement.value
                )
                achievementRepository.unlockAchievement(unlocked)
                newlyUnlocked.add(unlocked)
            }
        }
        
        if (newlyUnlocked.isNotEmpty()) {
            _recentAchievements.value = newlyUnlocked
            for (achievement in newlyUnlocked) {
                _events.emit(ReadingAnalyticsEvent.AchievementUnlocked(achievement))
            }
        }
    }
    
    // Milestones
    
    suspend fun getMilestones(): List<ReadingMilestone> {
        return analyticsRepository.getMilestones()
    }
    
    suspend fun recordMilestone(type: MilestoneType, value: Long, bookId: Long?, bookTitle: String?) {
        val milestone = ReadingMilestone(
            id = "milestone_${currentTimeToLong()}",
            type = type,
            value = value,
            reachedDate = currentTimeToLong(),
            bookId = bookId,
            bookTitle = bookTitle
        )
        analyticsRepository.saveMilestone(milestone)
        _events.emit(ReadingAnalyticsEvent.MilestoneReached(milestone))
    }
    
    // Book completion
    
    suspend fun recordBookCompletion(bookId: Long, bookTitle: String) {
        // Update book completion goals
        for (goal in _activeGoals.value) {
            if (goal.type == GoalType.BOOKS && goal.isActive && !goal.isCompleted) {
                updateGoalProgress(goal.id, goal.currentProgress + 1)
            }
        }
        
        // Check for book milestones
        val stats = getOverallStats()
        val completedBooks = stats.totalBooksCompleted + 1
        
        when (completedBooks) {
            1 -> recordMilestone(MilestoneType.FIRST_BOOK_COMPLETED, 1, bookId, bookTitle)
            10 -> recordMilestone(MilestoneType.BOOKS_10, 10, bookId, bookTitle)
            50 -> recordMilestone(MilestoneType.BOOKS_50, 50, bookId, bookTitle)
            100 -> recordMilestone(MilestoneType.BOOKS_100, 100, bookId, bookTitle)
        }
        
        _events.emit(ReadingAnalyticsEvent.BookCompleted(bookId, bookTitle))
    }
    
    // Streak tracking
    
    suspend fun getCurrentStreak(): Int {
        return analyticsRepository.getCurrentStreak()
    }
    
    suspend fun getLongestStreak(): Int {
        return analyticsRepository.getLongestStreak()
    }
    
    // Helper
    
    private suspend fun refreshTodayStats() {
        val today = getTodayDateString()
        _todayStats.value = analyticsRepository.getDailyStats(today)
    }
    
    private fun getTodayDateString(): String {
        // Returns YYYY-MM-DD format using existing formatIsoDate extension
        return currentTimeToLong().formatIsoDate()
    }
}

/**
 * Events from the reading analytics system.
 */
sealed class ReadingAnalyticsEvent {
    data class SessionStarted(val session: ReadingSession) : ReadingAnalyticsEvent()
    data class SessionPaused(val sessionId: String) : ReadingAnalyticsEvent()
    data class SessionResumed(val sessionId: String) : ReadingAnalyticsEvent()
    data class SessionEnded(val session: ReadingSession) : ReadingAnalyticsEvent()
    data class GoalCreated(val goal: ReadingGoal) : ReadingAnalyticsEvent()
    data class GoalCompleted(val goal: ReadingGoal) : ReadingAnalyticsEvent()
    data class AchievementUnlocked(val achievement: ReadingAchievement) : ReadingAnalyticsEvent()
    data class MilestoneReached(val milestone: ReadingMilestone) : ReadingAnalyticsEvent()
    data class BookCompleted(val bookId: Long, val bookTitle: String) : ReadingAnalyticsEvent()
    data class StreakUpdated(val currentStreak: Int, val isNewRecord: Boolean) : ReadingAnalyticsEvent()
}

/**
 * Repository interface for reading analytics data.
 */
interface ReadingAnalyticsRepository {
    suspend fun saveSession(session: ReadingSession)
    suspend fun getRecentSessions(limit: Int): List<ReadingSession>
    suspend fun getSessionsForBook(bookId: Long): List<ReadingSession>
    suspend fun getDailyStats(date: String): DailyReadingStats?
    suspend fun getWeeklyStats(): List<DailyReadingStats>
    suspend fun getMonthlyStats(year: Int, month: Int): List<DailyReadingStats>
    suspend fun getBookStats(bookId: Long): BookReadingStats?
    suspend fun getOverallStats(): OverallReadingStats
    suspend fun getReadingPattern(): ReadingPattern
    suspend fun getReadingHeatmap(year: Int): ReadingHeatmap
    suspend fun getSpeedAnalysis(): ReadingSpeedAnalysis
    suspend fun comparePeriods(currentStart: Long, currentEnd: Long, previousStart: Long, previousEnd: Long): PeriodComparison
    suspend fun getMilestones(): List<ReadingMilestone>
    suspend fun saveMilestone(milestone: ReadingMilestone)
    suspend fun getCurrentStreak(): Int
    suspend fun getLongestStreak(): Int
}

/**
 * Repository interface for reading goals.
 */
interface ReadingGoalRepository {
    suspend fun getActiveGoals(): List<ReadingGoal>
    suspend fun getAllGoals(): List<ReadingGoal>
    suspend fun saveGoal(goal: ReadingGoal)
    suspend fun deleteGoal(goalId: String)
}

/**
 * Repository interface for achievements.
 */
interface AchievementRepository {
    suspend fun getAllAchievements(): List<ReadingAchievement>
    suspend fun getUnlockedAchievements(): List<ReadingAchievement>
    suspend fun getAchievementProgress(): Map<String, Int>
    suspend fun unlockAchievement(achievement: ReadingAchievement)
}
