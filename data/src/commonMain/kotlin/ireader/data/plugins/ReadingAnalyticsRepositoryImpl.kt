package ireader.data.plugins

import ireader.domain.plugins.analytics.*
import data.ReadingAnalyticsQueries
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Implementation of ReadingAnalyticsRepository using SQLDelight.
 */
class ReadingAnalyticsRepositoryImpl(
    private val queries: ReadingAnalyticsQueries
) : ReadingAnalyticsRepository {
    
    override suspend fun saveSession(session: ReadingSession) {
        queries.insertSession(
            id = session.id,
            book_id = session.bookId,
            book_title = session.bookTitle,
            start_time = session.startTime,
            end_time = session.endTime,
            start_chapter_id = session.startChapterId,
            end_chapter_id = session.endChapterId,
            start_position = session.startPosition.toLong(),
            end_position = session.endPosition?.toLong(),
            pages_read = session.pagesRead.toLong(),
            words_read = session.wordsRead.toLong(),
            characters_read = session.charactersRead.toLong(),
            pause_duration_ms = session.pauseDurationMs,
            device_type = session.deviceType,
            is_completed = session.isCompleted
        )
    }
    
    override suspend fun getRecentSessions(limit: Int): List<ReadingSession> {
        return queries.selectRecentSessions(limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getSessionsForBook(bookId: Long): List<ReadingSession> {
        return queries.selectSessionsByBook(bookId)
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getDailyStats(date: String): DailyReadingStats? {
        return queries.selectDailyStats(date)
            .executeAsOneOrNull()
            ?.toDomain()
    }
    
    override suspend fun getWeeklyStats(): List<DailyReadingStats> {
        return queries.selectWeeklyStats()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getMonthlyStats(year: Int, month: Int): List<DailyReadingStats> {
        val monthStr = "$year-${month.toString().padStart(2, '0')}"
        return queries.selectMonthlyStats(monthStr)
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getBookStats(bookId: Long): BookReadingStats? {
        val sessions = getSessionsForBook(bookId)
        if (sessions.isEmpty()) return null
        
        val totalTime = sessions.sumOf { it.durationMs }
        val totalWords = sessions.sumOf { it.wordsRead }
        
        return BookReadingStats(
            bookId = bookId,
            bookTitle = sessions.first().bookTitle,
            totalReadingTimeMs = totalTime,
            sessionsCount = sessions.size,
            chaptersRead = sessions.mapNotNull { it.endChapterId }.distinct().size,
            totalChapters = 0, // Would need book info
            progressPercent = 0f, // Would need book info
            averageSessionLengthMs = if (sessions.isNotEmpty()) totalTime / sessions.size else 0,
            averageWordsPerMinute = if (totalTime > 0) (totalWords.toFloat() / totalTime) * 60_000 else 0f,
            firstReadDate = sessions.minOf { it.startTime },
            lastReadDate = sessions.maxOf { it.startTime },
            estimatedTimeToFinishMs = null,
            readingStreak = 0
        )
    }
    
    override suspend fun getOverallStats(): OverallReadingStats {
        val totalTimeResult = queries.getTotalReadingTime().executeAsOneOrNull()
        val totalTime = (totalTimeResult as? Long) ?: (totalTimeResult as? Number)?.toLong() ?: 0L
        val totalSessionsResult = queries.getTotalSessions().executeAsOneOrNull()
        val totalSessions = (totalSessionsResult as? Long)?.toInt() ?: (totalSessionsResult as? Number)?.toInt() ?: 0
        val totalWordsResult = queries.getTotalWordsRead().executeAsOneOrNull()
        val totalWords = (totalWordsResult as? Long) ?: (totalWordsResult as? Number)?.toLong() ?: 0L
        
        return OverallReadingStats(
            totalReadingTimeMs = totalTime,
            totalSessions = totalSessions,
            totalBooksStarted = 0,
            totalBooksCompleted = 0,
            totalChaptersRead = 0,
            totalPagesRead = 0,
            totalWordsRead = totalWords,
            averageWordsPerMinute = if (totalTime > 0) (totalWords.toFloat() / totalTime) * 60_000 else 0f,
            averageSessionLengthMs = if (totalSessions > 0) totalTime / totalSessions else 0,
            longestStreak = getLongestStreak(),
            currentStreak = getCurrentStreak(),
            favoriteReadingHour = 0,
            favoriteReadingDay = "Monday",
            mostReadGenre = null,
            fastestBook = null,
            longestSession = null
        )
    }
    
    override suspend fun getReadingPattern(): ReadingPattern {
        return ReadingPattern(
            hourlyDistribution = emptyMap(),
            dailyDistribution = emptyMap(),
            weeklyTrend = emptyList(),
            monthlyTrend = emptyList(),
            preferredSessionLength = SessionLengthPreference.MEDIUM,
            readingConsistency = 0.5f,
            peakProductivityHours = listOf(20, 21, 22)
        )
    }
    
    override suspend fun getReadingHeatmap(year: Int): ReadingHeatmap {
        return ReadingHeatmap(year = year, data = emptyMap())
    }
    
    override suspend fun getSpeedAnalysis(): ReadingSpeedAnalysis {
        val stats = getOverallStats()
        return ReadingSpeedAnalysis(
            overallWpm = stats.averageWordsPerMinute,
            byGenre = emptyMap(),
            byTimeOfDay = emptyMap(),
            trend = SpeedTrend.STABLE,
            percentile = 50,
            improvementPercent = 0f
        )
    }
    
    override suspend fun comparePeriods(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long
    ): PeriodComparison {
        val currentSessions = queries.selectSessionsByDateRange(currentStart, currentEnd)
            .executeAsList().map { it.toDomain() }
        val previousSessions = queries.selectSessionsByDateRange(previousStart, previousEnd)
            .executeAsList().map { it.toDomain() }
        
        val currentTime = currentSessions.sumOf { it.durationMs }
        val previousTime = previousSessions.sumOf { it.durationMs }
        
        return PeriodComparison(
            currentPeriod = PeriodStats(
                startDate = currentStart,
                endDate = currentEnd,
                totalTimeMs = currentTime,
                sessions = currentSessions.size,
                averageWpm = 0f,
                booksCompleted = 0
            ),
            previousPeriod = PeriodStats(
                startDate = previousStart,
                endDate = previousEnd,
                totalTimeMs = previousTime,
                sessions = previousSessions.size,
                averageWpm = 0f,
                booksCompleted = 0
            ),
            readingTimeChange = if (previousTime > 0) 
                ((currentTime - previousTime).toFloat() / previousTime) * 100 else 0f,
            sessionsChange = if (previousSessions.isNotEmpty()) 
                ((currentSessions.size - previousSessions.size).toFloat() / previousSessions.size) * 100 else 0f,
            speedChange = 0f,
            booksChange = 0
        )
    }
    
    override suspend fun getMilestones(): List<ReadingMilestone> {
        return queries.selectAllMilestones()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveMilestone(milestone: ReadingMilestone) {
        queries.insertMilestone(
            id = milestone.id,
            type = milestone.type.name,
            value_ = milestone.value,
            reached_date = milestone.reachedDate,
            book_id = milestone.bookId,
            book_title = milestone.bookTitle
        )
    }
    
    override suspend fun getCurrentStreak(): Int {
        return queries.selectStreak().executeAsOneOrNull()?.current_streak?.toInt() ?: 0
    }
    
    override suspend fun getLongestStreak(): Int {
        return queries.selectStreak().executeAsOneOrNull()?.longest_streak?.toInt() ?: 0
    }
    
    // Extension functions
    private fun data.Reading_session.toDomain() = ReadingSession(
        id = id,
        bookId = book_id,
        bookTitle = book_title,
        startTime = start_time,
        endTime = end_time,
        startChapterId = start_chapter_id,
        endChapterId = end_chapter_id,
        startPosition = start_position.toInt(),
        endPosition = end_position?.toInt(),
        pagesRead = pages_read.toInt(),
        wordsRead = words_read.toInt(),
        charactersRead = characters_read.toInt(),
        pauseDurationMs = pause_duration_ms,
        deviceType = device_type,
        isCompleted = is_completed
    )
    
    private fun data.Daily_reading_stats.toDomain() = DailyReadingStats(
        date = date,
        totalReadingTimeMs = total_reading_time_ms,
        sessionsCount = sessions_count.toInt(),
        booksRead = books_read.toInt(),
        chaptersRead = chapters_read.toInt(),
        pagesRead = pages_read.toInt(),
        wordsRead = words_read.toInt(),
        averageWordsPerMinute = average_wpm.toFloat(),
        longestSessionMs = longest_session_ms,
        peakReadingHour = peak_reading_hour.toInt(),
        goalProgress = null
    )
    
    private fun data.Reading_milestone.toDomain() = ReadingMilestone(
        id = id,
        type = MilestoneType.valueOf(type),
        value = value_,
        reachedDate = reached_date,
        bookId = book_id,
        bookTitle = book_title
    )
}

/**
 * Implementation of ReadingGoalRepository.
 */
class ReadingGoalRepositoryImpl(
    private val queries: ReadingAnalyticsQueries
) : ReadingGoalRepository {
    
    override suspend fun getActiveGoals(): List<ReadingGoal> {
        return queries.selectActiveGoals()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getAllGoals(): List<ReadingGoal> {
        return queries.selectAllGoals()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun saveGoal(goal: ReadingGoal) {
        queries.insertGoal(
            id = goal.id,
            type = goal.type.name,
            target = goal.target.toLong(),
            period = goal.period.name,
            start_date = goal.startDate,
            end_date = goal.endDate,
            current_progress = goal.currentProgress.toLong(),
            is_active = goal.isActive,
            is_completed = goal.isCompleted,
            completed_date = goal.completedDate,
            streak_days = goal.streakDays.toLong()
        )
    }
    
    override suspend fun deleteGoal(goalId: String) {
        queries.deleteGoal(goalId)
    }
    
    private fun data.Reading_goal.toDomain() = ReadingGoal(
        id = id,
        type = GoalType.valueOf(type),
        target = target.toInt(),
        period = GoalPeriod.valueOf(period),
        startDate = start_date,
        endDate = end_date,
        currentProgress = current_progress.toInt(),
        isActive = is_active,
        isCompleted = is_completed,
        completedDate = completed_date,
        streakDays = streak_days.toInt()
    )
}

/**
 * Implementation of AchievementRepository.
 */
class AchievementRepositoryImpl(
    private val queries: ReadingAnalyticsQueries
) : AchievementRepository {
    
    override suspend fun getAllAchievements(): List<ReadingAchievement> {
        return queries.selectAllAchievements()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getUnlockedAchievements(): List<ReadingAchievement> {
        return queries.selectUnlockedAchievements()
            .executeAsList()
            .map { it.toDomain() }
    }
    
    override suspend fun getAchievementProgress(): Map<String, Int> {
        return getAllAchievements().associate { it.id to it.progress }
    }
    
    override suspend fun unlockAchievement(achievement: ReadingAchievement) {
        queries.unlockAchievement(
            unlocked_date = achievement.unlockedDate,
            id = achievement.id
        )
    }
    
    private fun data.Reading_achievement.toDomain() = ReadingAchievement(
        id = id,
        name = name,
        description = description,
        iconUrl = icon_url,
        category = AchievementCategory.valueOf(category),
        tier = AchievementTier.valueOf(tier),
        requirement = AchievementRequirement(
            type = requirement_type,
            value = requirement_value.toInt(),
            description = requirement_description
        ),
        progress = progress.toInt(),
        isUnlocked = is_unlocked,
        unlockedDate = unlocked_date,
        points = points.toInt()
    )
}
