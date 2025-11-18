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
    val currentlyReading: Int = 0
)