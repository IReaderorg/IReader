package ireader.domain.plugins.analytics

/**
 * Default achievements for the reading analytics system.
 */
object DefaultAchievements {
    
    fun getAll(): List<ReadingAchievement> = listOf(
        // Reading Time Achievements
        ReadingAchievement(
            id = "time_1h",
            name = "First Steps",
            description = "Read for 1 hour total",
            iconUrl = null,
            category = AchievementCategory.READING_TIME,
            tier = AchievementTier.BRONZE,
            requirement = AchievementRequirement("time_hours", 1, "Read for 1 hour"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 10
        ),
        ReadingAchievement(
            id = "time_10h",
            name = "Bookworm",
            description = "Read for 10 hours total",
            iconUrl = null,
            category = AchievementCategory.READING_TIME,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("time_hours", 10, "Read for 10 hours"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 50
        ),
        ReadingAchievement(
            id = "time_100h",
            name = "Dedicated Reader",
            description = "Read for 100 hours total",
            iconUrl = null,
            category = AchievementCategory.READING_TIME,
            tier = AchievementTier.GOLD,
            requirement = AchievementRequirement("time_hours", 100, "Read for 100 hours"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 200
        ),
        ReadingAchievement(
            id = "time_1000h",
            name = "Reading Master",
            description = "Read for 1000 hours total",
            iconUrl = null,
            category = AchievementCategory.READING_TIME,
            tier = AchievementTier.DIAMOND,
            requirement = AchievementRequirement("time_hours", 1000, "Read for 1000 hours"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 1000
        ),
        
        // Books Completed Achievements
        ReadingAchievement(
            id = "books_1",
            name = "First Book",
            description = "Complete your first book",
            iconUrl = null,
            category = AchievementCategory.BOOKS_COMPLETED,
            tier = AchievementTier.BRONZE,
            requirement = AchievementRequirement("books", 1, "Complete 1 book"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 25
        ),
        ReadingAchievement(
            id = "books_10",
            name = "Avid Reader",
            description = "Complete 10 books",
            iconUrl = null,
            category = AchievementCategory.BOOKS_COMPLETED,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("books", 10, "Complete 10 books"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 100
        ),
        ReadingAchievement(
            id = "books_50",
            name = "Book Collector",
            description = "Complete 50 books",
            iconUrl = null,
            category = AchievementCategory.BOOKS_COMPLETED,
            tier = AchievementTier.GOLD,
            requirement = AchievementRequirement("books", 50, "Complete 50 books"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 300
        ),
        ReadingAchievement(
            id = "books_100",
            name = "Century Reader",
            description = "Complete 100 books",
            iconUrl = null,
            category = AchievementCategory.BOOKS_COMPLETED,
            tier = AchievementTier.PLATINUM,
            requirement = AchievementRequirement("books", 100, "Complete 100 books"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 500
        ),
        
        // Streak Achievements
        ReadingAchievement(
            id = "streak_7",
            name = "Consistent",
            description = "Maintain a 7-day reading streak",
            iconUrl = null,
            category = AchievementCategory.STREAK,
            tier = AchievementTier.BRONZE,
            requirement = AchievementRequirement("streak_days", 7, "7-day streak"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 30
        ),
        ReadingAchievement(
            id = "streak_30",
            name = "Dedicated",
            description = "Maintain a 30-day reading streak",
            iconUrl = null,
            category = AchievementCategory.STREAK,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("streak_days", 30, "30-day streak"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 100
        ),
        ReadingAchievement(
            id = "streak_100",
            name = "Unstoppable",
            description = "Maintain a 100-day reading streak",
            iconUrl = null,
            category = AchievementCategory.STREAK,
            tier = AchievementTier.GOLD,
            requirement = AchievementRequirement("streak_days", 100, "100-day streak"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 300
        ),
        ReadingAchievement(
            id = "streak_365",
            name = "Year of Reading",
            description = "Maintain a 365-day reading streak",
            iconUrl = null,
            category = AchievementCategory.STREAK,
            tier = AchievementTier.DIAMOND,
            requirement = AchievementRequirement("streak_days", 365, "365-day streak"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 1000
        ),
        
        // Speed Achievements
        ReadingAchievement(
            id = "speed_200",
            name = "Quick Reader",
            description = "Reach 200 words per minute",
            iconUrl = null,
            category = AchievementCategory.SPEED,
            tier = AchievementTier.BRONZE,
            requirement = AchievementRequirement("wpm", 200, "200 WPM"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 20
        ),
        ReadingAchievement(
            id = "speed_300",
            name = "Speed Reader",
            description = "Reach 300 words per minute",
            iconUrl = null,
            category = AchievementCategory.SPEED,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("wpm", 300, "300 WPM"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 75
        ),
        ReadingAchievement(
            id = "speed_500",
            name = "Lightning Reader",
            description = "Reach 500 words per minute",
            iconUrl = null,
            category = AchievementCategory.SPEED,
            tier = AchievementTier.GOLD,
            requirement = AchievementRequirement("wpm", 500, "500 WPM"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 200
        ),
        
        // Special Achievements
        ReadingAchievement(
            id = "night_owl",
            name = "Night Owl",
            description = "Read for 2 hours between midnight and 4 AM",
            iconUrl = null,
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("night_reading_hours", 2, "2 hours of night reading"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 50
        ),
        ReadingAchievement(
            id = "early_bird",
            name = "Early Bird",
            description = "Read for 2 hours between 5 AM and 8 AM",
            iconUrl = null,
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.SILVER,
            requirement = AchievementRequirement("morning_reading_hours", 2, "2 hours of morning reading"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 50
        ),
        ReadingAchievement(
            id = "marathon",
            name = "Marathon Reader",
            description = "Read for 4 hours in a single session",
            iconUrl = null,
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.GOLD,
            requirement = AchievementRequirement("session_hours", 4, "4-hour session"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 150
        ),
        ReadingAchievement(
            id = "words_1m",
            name = "Million Words",
            description = "Read 1 million words total",
            iconUrl = null,
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.PLATINUM,
            requirement = AchievementRequirement("words", 1_000_000, "1 million words"),
            progress = 0,
            isUnlocked = false,
            unlockedDate = null,
            points = 500
        )
    )
}
