package ireader.domain.usecases.quote

import ireader.domain.models.quote.*
import ireader.domain.preferences.prefs.ReadingBuddyPreferences
import kotlin.math.sqrt

/**
 * Use cases for the Reading Buddy feature
 */
class ReadingBuddyUseCases(
    private val preferences: ReadingBuddyPreferences
) {
    
    /**
     * Get current buddy state based on reading activity
     */
    fun getBuddyState(): ReadingBuddyState {
        val lastInteraction = preferences.lastInteractionTime().get()
        val lastReadDate = preferences.lastReadDate().get()
        val currentStreak = preferences.currentStreak().get()
        val level = preferences.buddyLevel().get()
        val experience = preferences.buddyExperience().get()
        
        val now = System.currentTimeMillis()
        val hoursSinceLastRead = (now - lastReadDate) / (1000 * 60 * 60)
        
        val mood = when {
            hoursSinceLastRead > 168 -> BuddyMood.SLEEPING // > 1 week
            hoursSinceLastRead > 72 -> BuddyMood.SLEEPY // > 3 days
            hoursSinceLastRead > 48 -> BuddyMood.SAD // > 2 days
            hoursSinceLastRead > 24 -> BuddyMood.NEUTRAL // > 1 day
            currentStreak >= 7 -> BuddyMood.PROUD
            currentStreak >= 3 -> BuddyMood.EXCITED
            else -> BuddyMood.HAPPY
        }
        
        return ReadingBuddyState(
            mood = mood,
            message = getMessageForMood(mood, currentStreak),
            animation = getAnimationForMood(mood),
            level = level,
            experience = experience,
            totalBooksRead = preferences.totalBooksRead().get(),
            totalChaptersRead = preferences.totalChaptersRead().get(),
            currentStreak = currentStreak,
            longestStreak = preferences.longestStreak().get(),
            lastInteractionTime = lastInteraction
        )
    }
    
    /**
     * Record that user started reading
     */
    suspend fun onReadingStarted() {
        preferences.lastInteractionTime().set(System.currentTimeMillis())
        updateStreak()
    }
    
    /**
     * Record chapter completion and award XP
     */
    suspend fun onChapterCompleted(): BuddyAchievement? {
        val chaptersRead = preferences.totalChaptersRead().get() + 1
        preferences.totalChaptersRead().set(chaptersRead)
        
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
        val booksRead = preferences.totalBooksRead().get() + 1
        preferences.totalBooksRead().set(booksRead)
        
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
     * Update reading streak
     */
    private suspend fun updateStreak() {
        val now = System.currentTimeMillis()
        val lastRead = preferences.lastReadDate().get()
        val currentStreak = preferences.currentStreak().get()
        
        val daysSinceLastRead = (now - lastRead) / (1000 * 60 * 60 * 24)
        
        val newStreak = when {
            daysSinceLastRead <= 1 -> currentStreak + 1
            daysSinceLastRead <= 2 -> currentStreak // Same day or next day
            else -> 1 // Streak broken, start fresh
        }
        
        preferences.currentStreak().set(newStreak)
        preferences.lastReadDate().set(now)
        
        // Update longest streak
        val longestStreak = preferences.longestStreak().get()
        if (newStreak > longestStreak) {
            preferences.longestStreak().set(newStreak)
        }
        
        // Check streak achievements
        checkStreakAchievements(newStreak)
    }
    
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
        val currentXp = preferences.buddyExperience().get() + xp
        val currentLevel = preferences.buddyLevel().get()
        
        // XP needed for next level: level * 100
        val xpForNextLevel = currentLevel * 100
        
        if (currentXp >= xpForNextLevel) {
            preferences.buddyLevel().set(currentLevel + 1)
            preferences.buddyExperience().set(currentXp - xpForNextLevel)
        } else {
            preferences.buddyExperience().set(currentXp)
        }
    }
    
    /**
     * Unlock an achievement
     */
    private suspend fun unlockAchievement(achievement: BuddyAchievement) {
        val unlocked = preferences.unlockedAchievements().get()
        if (!unlocked.contains(achievement.name)) {
            val newUnlocked = if (unlocked.isEmpty()) achievement.name else "$unlocked,${achievement.name}"
            preferences.unlockedAchievements().set(newUnlocked)
            addExperience(achievement.xpReward)
        }
    }
    
    /**
     * Get unlocked achievements
     */
    fun getUnlockedAchievements(): List<BuddyAchievement> {
        val unlocked = preferences.unlockedAchievements().get()
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
    fun getLevelProgress(): Float {
        val currentXp = preferences.buddyExperience().get()
        val currentLevel = preferences.buddyLevel().get()
        val xpForNextLevel = currentLevel * 100
        return (currentXp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
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
