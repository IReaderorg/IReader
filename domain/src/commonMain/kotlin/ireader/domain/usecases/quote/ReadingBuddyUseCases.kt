package ireader.domain.usecases.quote

import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.ReadingStatisticsType1
import ireader.domain.models.quote.*
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Use cases for the Reading Buddy feature.
 * Now uses the unified ReadingStatisticsRepository (database) as the single source of truth
 * instead of preferences, ensuring sync across Leaderboard, Statistics, and Reading Buddy screens.
 */
class ReadingBuddyUseCases(
    private val statisticsRepository: ReadingStatisticsRepository
) {
    // Cache the current message to prevent flickering when Flow emits
    private var cachedMood: BuddyMood? = null
    private var cachedMessage: String? = null
    
    /**
     * Get buddy state as a Flow for reactive updates.
     * Uses distinctUntilChanged to prevent unnecessary recompositions.
     */
    fun getBuddyStateFlow(): Flow<ReadingBuddyState> {
        return statisticsRepository.getStatisticsFlow()
            .map { stats -> createBuddyState(stats) }
            .distinctUntilChanged()
    }
    
    /**
     * Get current buddy state based on reading activity from database
     */
    suspend fun getBuddyState(): ReadingBuddyState {
        val stats = statisticsRepository.getStatistics()
        return createBuddyState(stats)
    }
    
    private fun createBuddyState(stats: ReadingStatisticsType1): ReadingBuddyState {
        val now = currentTimeToLong()
        val hoursSinceLastRead = if (stats.lastReadDate > 0) {
            (now - stats.lastReadDate) / (1000 * 60 * 60)
        } else {
            Long.MAX_VALUE
        }
        
        val mood = when {
            hoursSinceLastRead > 168 -> BuddyMood.SLEEPING // > 1 week
            hoursSinceLastRead > 72 -> BuddyMood.SLEEPY // > 3 days
            hoursSinceLastRead > 48 -> BuddyMood.SAD // > 2 days
            hoursSinceLastRead > 24 -> BuddyMood.NEUTRAL // > 1 day
            stats.readingStreak >= 7 -> BuddyMood.PROUD
            stats.readingStreak >= 3 -> BuddyMood.EXCITED
            else -> BuddyMood.HAPPY
        }
        
        // Only generate a new message if mood changed to prevent flickering
        val message = if (mood == cachedMood && cachedMessage != null) {
            cachedMessage!!
        } else {
            val newMessage = getMessageForMood(mood, stats.readingStreak)
            cachedMood = mood
            cachedMessage = newMessage
            newMessage
        }
        
        return ReadingBuddyState(
            mood = mood,
            message = message,
            animation = getAnimationForMood(mood),
            level = stats.buddyLevel,
            experience = stats.buddyExperience,
            totalBooksRead = stats.booksCompleted,
            totalChaptersRead = stats.totalChaptersRead,
            currentStreak = stats.readingStreak,
            longestStreak = stats.longestStreak,
            lastInteractionTime = stats.lastInteractionTime
        )
    }

    /**
     * Record that user started reading.
     * Note: Streak is updated when leaving the reader screen via TrackReadingProgressUseCase
     * to avoid double counting.
     */
    suspend fun onReadingStarted() {
        statisticsRepository.updateLastInteractionTime(currentTimeToLong())
        // Streak update removed - handled by TrackReadingProgressUseCase.updateReadingStreak()
        // when user leaves the reader screen
    }
    
    /**
     * Record chapter completion and award XP
     */
    suspend fun onChapterCompleted(): BuddyAchievement? {
        // Chapter count is already incremented by TrackReadingProgressUseCase
        // We just need to handle XP and achievements
        val stats = statisticsRepository.getStatistics()
        val chaptersRead = stats.totalChaptersRead
        
        // Award XP for chapter
        addExperience(5)
        
        // Check for achievements
        return when (chaptersRead) {
            1 -> {
                unlockAchievement(BuddyAchievement.FIRST_CHAPTER)
                BuddyAchievement.FIRST_CHAPTER
            }
            100 -> {
                unlockAchievement(BuddyAchievement.CHAPTERS_100)
                BuddyAchievement.CHAPTERS_100
            }
            else -> null
        }
    }
    
    /**
     * Record book completion and award XP
     */
    suspend fun onBookCompleted(): BuddyAchievement? {
        // Book count is already incremented by TrackReadingProgressUseCase
        // We just need to handle XP and achievements
        val stats = statisticsRepository.getStatistics()
        val booksRead = stats.booksCompleted
        
        // Award XP for book
        addExperience(25)
        
        // Check for achievements
        return when (booksRead) {
            1 -> {
                unlockAchievement(BuddyAchievement.FIRST_BOOK)
                BuddyAchievement.FIRST_BOOK
            }
            5 -> {
                unlockAchievement(BuddyAchievement.BOOKS_5)
                BuddyAchievement.BOOKS_5
            }
            10 -> {
                unlockAchievement(BuddyAchievement.BOOKS_10)
                BuddyAchievement.BOOKS_10
            }
            25 -> {
                unlockAchievement(BuddyAchievement.BOOKS_25)
                BuddyAchievement.BOOKS_25
            }
            50 -> {
                unlockAchievement(BuddyAchievement.BOOKS_50)
                BuddyAchievement.BOOKS_50
            }
            else -> null
        }
    }
    
    /**
     * Check and award streak achievements based on current streak
     */
    private suspend fun checkStreakAchievements(streak: Int) {
        when (streak) {
            3 -> unlockAchievement(BuddyAchievement.STREAK_3)
            7 -> unlockAchievement(BuddyAchievement.STREAK_7)
            30 -> unlockAchievement(BuddyAchievement.STREAK_30)
        }
    }
    
    /**
     * Add experience points and handle level up
     */
    private suspend fun addExperience(xp: Int) {
        val stats = statisticsRepository.getStatistics()
        var currentXp = stats.buddyExperience + xp
        var currentLevel = stats.buddyLevel
        
        // XP needed for next level: level * 100
        var xpForNextLevel = currentLevel * 100
        
        // Handle multiple level ups
        while (currentXp >= xpForNextLevel) {
            currentXp -= xpForNextLevel
            currentLevel++
            xpForNextLevel = currentLevel * 100
        }
        
        statisticsRepository.updateBuddyProgress(currentLevel, currentXp)
    }
    
    /**
     * Unlock an achievement
     */
    private suspend fun unlockAchievement(achievement: BuddyAchievement) {
        val unlocked = statisticsRepository.getUnlockedAchievements()
        if (!unlocked.contains(achievement.name)) {
            val newUnlocked = if (unlocked.isEmpty()) achievement.name else "$unlocked,${achievement.name}"
            statisticsRepository.updateUnlockedAchievements(newUnlocked)
            addExperience(achievement.xpReward)
        }
    }
    
    /**
     * Get unlocked achievements
     */
    suspend fun getUnlockedAchievements(): List<BuddyAchievement> {
        val unlocked = statisticsRepository.getUnlockedAchievements()
        if (unlocked.isEmpty()) return emptyList()
        
        return unlocked.split(",").mapNotNull { name ->
            try {
                BuddyAchievement.valueOf(name)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get XP progress to next level (0.0 to 1.0)
     */
    suspend fun getLevelProgress(): Float {
        val stats = statisticsRepository.getStatistics()
        val xpForNextLevel = stats.buddyLevel * 100
        return (stats.buddyExperience.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
    }
    
    private fun getMessageForMood(mood: BuddyMood, streak: Int): String {
        return when (mood) {
            BuddyMood.SLEEPING -> BuddyMessages.sleepy.random()
            BuddyMood.SLEEPY -> BuddyMessages.sleepy.random()
            BuddyMood.SAD -> BuddyMessages.comebacks.random()
            BuddyMood.NEUTRAL -> BuddyMessages.greetings.random()
            BuddyMood.HAPPY -> BuddyMessages.greetings.random()
            BuddyMood.EXCITED -> "${BuddyMessages.encouragements.random()} 🔥 $streak day streak!"
            BuddyMood.PROUD -> "${BuddyMessages.milestones.random()} 🏆 $streak day streak!"
            BuddyMood.CELEBRATING -> BuddyMessages.milestones.random()
            BuddyMood.READING -> "Reading together... 📖"
            BuddyMood.CHEERING -> BuddyMessages.encouragements.random()
        }
    }
    
    private fun getAnimationForMood(mood: BuddyMood): BuddyAnimation {
        return when (mood) {
            BuddyMood.SLEEPING -> BuddyAnimation.SLEEP
            BuddyMood.SLEEPY -> BuddyAnimation.IDLE
            BuddyMood.SAD -> BuddyAnimation.IDLE
            BuddyMood.NEUTRAL -> BuddyAnimation.WAVE
            BuddyMood.HAPPY -> BuddyAnimation.BOUNCE
            BuddyMood.EXCITED -> BuddyAnimation.JUMP
            BuddyMood.PROUD -> BuddyAnimation.CELEBRATE
            BuddyMood.CELEBRATING -> BuddyAnimation.DANCE
            BuddyMood.READING -> BuddyAnimation.READ
            BuddyMood.CHEERING -> BuddyAnimation.CHEER
        }
    }
}
