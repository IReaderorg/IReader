package ireader.domain.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class ReadingStatisticsType1(
    val totalChaptersRead: Int = 0,
    val totalReadingTimeMinutes: Long = 0,
    val averageReadingSpeedWPM: Int = 0,
    val favoriteGenres: List<GenreCount> = emptyList(),
    val readingStreak: Int = 0,
    val booksCompleted: Int = 0,
    val currentlyReading: Int = 0,
    // Reading Buddy fields - unified with statistics
    val longestStreak: Int = 0,
    val buddyLevel: Int = 1,
    val buddyExperience: Int = 0,
    val unlockedAchievements: String = "",
    val lastInteractionTime: Long = 0,
    val lastReadDate: Long = 0
)