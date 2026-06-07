package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Genre count data
 */
@Serializable
data class GenreCount(
    val genre: String,
    val bookCount: Int
)

/**
 * Comprehensive library insights and analytics
 */
@Serializable
data class LibraryInsights(
    val totalBooks: Int = 0,
    val booksInLibrary: Int = 0,
    val booksCompleted: Int = 0,
    val booksInProgress: Int = 0,
    val booksNotStarted: Int = 0,
    val totalChapters: Int = 0,
    val chaptersRead: Int = 0,
    val completionRate: Float = 0f,
    val genreDistribution: List<GenreCount> = emptyList(),
    val readingPatterns: ReadingPatterns = ReadingPatterns(),
    val topAuthors: List<AuthorCount> = emptyList(),
    val sourceDistribution: List<SourceCount> = emptyList()
)

@Serializable
data class ReadingPatterns(
    val averageChaptersPerDay: Float = 0f,
    val mostActiveDay: String = "",
    val mostActiveHour: Int = 0,
    val readingStreak: Int = 0,
    val longestStreak: Int = 0
)

@Serializable
data class AuthorCount(
    val author: String,
    val bookCount: Int,
    val chaptersRead: Int
)

@Serializable
data class SourceCount(
    val sourceId: Long,
    val sourceName: String,
    val bookCount: Int
)

/**
 * Advanced reading analytics
 */
@Serializable
data class ReadingAnalytics(
    val totalReadingTimeMinutes: Long = 0,
    val averageReadingSpeedWPM: Int = 0,
    val totalWordsRead: Long = 0,
    val readingSessions: List<ReadingSession> = emptyList(),
    val dailyReadingTime: Map<String, Long> = emptyMap(),
    val weeklyReadingTime: Map<String, Long> = emptyMap(),
    val monthlyReadingTime: Map<String, Long> = emptyMap()
)

@Serializable
data class ReadingSession(
    val bookId: Long,
    val bookTitle: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val chaptersRead: Int,
    val wordsRead: Int
)

/**
 * Upcoming releases tracking
 */
@Serializable
data class UpcomingRelease(
    val bookId: Long,
    val bookTitle: String,
    val bookCover: String,
    val sourceId: Long,
    val expectedReleaseDate: Long?,
    val lastChapterDate: Long,
    val estimatedNextRelease: Long?,
    val releaseFrequency: ReleaseFrequency = ReleaseFrequency.UNKNOWN
)

@Serializable
enum class ReleaseFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    IRREGULAR,
    UNKNOWN
}

/**
 * Book recommendations
 */
@Serializable
data class BookRecommendation(
    val bookId: Long,
    val bookTitle: String,
    val bookCover: String,
    val author: String,
    val genres: List<String>,
    val sourceId: Long,
    val score: Float,
    val reason: RecommendationReason
)

@Serializable
enum class RecommendationReason {
    SIMILAR_GENRE,
    SAME_AUTHOR,
    POPULAR_IN_CATEGORY,
    READING_HISTORY,
    TRENDING,
    HIGHLY_RATED
}

/**
 * Global search result with multi-source support
 */
@Serializable
data class GlobalSearchResult(
    val query: String,
    val sourceResults: List<SourceSearchResult> = emptyList(),
    val totalResults: Int = 0,
    val searchDuration: Long = 0
)

@Serializable
data class SourceSearchResult(
    val sourceId: Long,
    val sourceName: String,
    val results: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Serializable
data class SearchResultItem(
    val bookId: Long?,
    val title: String,
    val author: String,
    val cover: String,
    val description: String,
    val genres: List<String>,
    val key: String,
    val inLibrary: Boolean = false
)

/**
 * Advanced filter state
 */
@Serializable
data class AdvancedFilterState(
    val genres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),
    val status: List<Long> = emptyList(),
    val sources: List<Long> = emptyList(),
    val authors: List<String> = emptyList(),
    val minChapters: Int? = null,
    val maxChapters: Int? = null,
    val completionStatus: List<CompletionStatus> = emptyList(),
    val sortBy: SortOption = SortOption.TITLE,
    val sortAscending: Boolean = true,
    val searchQuery: String = ""
)

@Serializable
enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

@Serializable
enum class SortOption {
    TITLE,
    AUTHOR,
    LAST_READ,
    DATE_ADDED,
    CHAPTERS_READ,
    TOTAL_CHAPTERS,
    COMPLETION_RATE
}

/**
 * Reading statistics summary
 */
@Serializable
data class ReadingStatistics(
    val totalBooksRead: Int = 0,
    val totalChaptersRead: Int = 0,
    val totalReadingTimeMinutes: Long = 0,
    val averageChaptersPerBook: Float = 0f,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

/**
 * Statistics export data
 */
@Serializable
data class StatisticsExport(
    val exportDate: Long,
    val libraryInsights: LibraryInsights,
    val readingStatisticsType1: ReadingStatisticsType1,
    val readingAnalytics: ReadingAnalytics,
    val upcomingReleases: List<UpcomingRelease>,
    val recommendations: List<BookRecommendation>
)
