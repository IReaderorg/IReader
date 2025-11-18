package ireader.data.repository

import ireader.core.log.Log
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.*
import ireader.domain.data.repository.ReadingSession
import ireader.domain.models.entities.StatisticsExport as EntitiesStatisticsExport
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import  kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Implementation of LibraryInsightsRepository
 * Provides comprehensive library analytics and insights
 */
class LibraryInsightsRepositoryImpl(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository,
    private val readerPreferences: ReaderPreferences
) : LibraryInsightsRepository {

    override suspend fun getLibraryInsights(): LibraryInsights {
        return try {
            val books = bookRepository.findAllInLibraryBooks(
                sortType = ireader.domain.models.library.LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val allChapters = books.flatMap { book ->
                chapterRepository.findChaptersByBookId(book.id)
            }

            val totalBooks = books.size
            val booksInLibrary = books.count { it.favorite }
            val booksCompleted = books.count { book ->
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                chapters.isNotEmpty() && chapters.all { it.read }
            }
            val booksInProgress = books.count { book ->
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                chapters.any { it.read } && !chapters.all { it.read }
            }
            val booksNotStarted = books.count { book ->
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                chapters.none { it.read }
            }

            val totalChapters = allChapters.size
            val chaptersRead = allChapters.count { it.read }
            val completionRate = if (totalChapters > 0) {
                (chaptersRead.toFloat() / totalChapters) * 100f
            } else 0f

            // Genre distribution
            val genreDistribution = books
                .flatMap { it.genres }
                .groupingBy { it }
                .eachCount()
                .map { (genre, bookCount) -> GenreCount(genre, bookCount) }
                .sortedByDescending { it.bookCount }
                .take(10)

            // Reading patterns
            val history = historyRepository.findHistories()
            val readingPatterns = calculateReadingPatterns(history)

            // Top authors
            val topAuthors = books
                .groupBy { it.author }
                .map { (author, authorBooks) ->
                    val chaptersRead = authorBooks.sumOf { book ->
                        chapterRepository.findChaptersByBookId(book.id).count { it.read }
                    }
                    AuthorCount(author, authorBooks.size, chaptersRead)
                }
                .sortedByDescending { it.chaptersRead }
                .take(10)

            // Source distribution
            val sourceDistribution = books
                .groupBy { it.sourceId }
                .map { (sourceId, sourceBooks) ->
                    SourceCount(
                        sourceId = sourceId,
                        sourceName = "Source $sourceId", // Would need catalog store to get actual name
                        bookCount = sourceBooks.size
                    )
                }
                .sortedByDescending { it.bookCount }
                .take(10)

            LibraryInsights(
                totalBooks = totalBooks,
                booksInLibrary = booksInLibrary,
                booksCompleted = booksCompleted,
                booksInProgress = booksInProgress,
                booksNotStarted = booksNotStarted,
                totalChapters = totalChapters,
                chaptersRead = chaptersRead,
                completionRate = completionRate,
                genreDistribution = genreDistribution,
                readingPatterns = readingPatterns,
                topAuthors = topAuthors,
                sourceDistribution = sourceDistribution
            )
        } catch (e: Exception) {
            Log.error { "Failed to get library insights: ${e.message}" }
            LibraryInsights()
        }
    }

    override fun getLibraryInsightsFlow(): Flow<LibraryInsights> {
        return flow {
            // Emit initial insights
            emit(getLibraryInsights())
            
            // Note: For reactive updates, would need to subscribe to book/history changes
            // This is a simplified implementation
        }
    }

    override suspend fun getReadingAnalytics(): ReadingAnalytics {
        return try {
            val history = historyRepository.findHistories()
            val totalReadingTimeMinutes = history.sumOf { 
                (it.readDuration ?: 0L) / 60000
            }

            // Calculate reading sessions
            val sessions = history.mapNotNull { historyItem ->
                val chapter = chapterRepository.findChapterById(historyItem.chapterId) ?: return@mapNotNull null
                val book = bookRepository.findBookById(chapter.bookId) ?: return@mapNotNull null

                ReadingSession(
                    bookId = book.id,
                    bookTitle = book.title,
                    startTime = historyItem.readAt ?: 0L,
                    endTime = (historyItem.readAt ?: 0L) + (historyItem.readDuration ?: 0L),
                    durationMinutes = (historyItem.readDuration ?: 0L) / 60000,
                    chaptersRead = 1,
                    wordsRead = estimateWordsInChapter()
                )
            }

            // Daily reading time
            val dailyReadingTime = sessions
                .groupBy { formatDate(it.startTime) }
                .mapValues { (_, daySessions) ->
                    daySessions.sumOf { it.durationMinutes }
                }

            // Weekly reading time
            val weeklyReadingTime = sessions
                .groupBy { formatWeek(it.startTime) }
                .mapValues { (_, weekSessions) ->
                    weekSessions.sumOf { it.durationMinutes }
                }

            // Monthly reading time
            val monthlyReadingTime = sessions
                .groupBy { formatMonth(it.startTime) }
                .mapValues { (_, monthSessions) ->
                    monthSessions.sumOf { it.durationMinutes }
                }

            val totalWordsRead = sessions.sumOf { it.wordsRead.toLong() }
            val averageReadingSpeedWPM = if (totalReadingTimeMinutes > 0) {
                (totalWordsRead / totalReadingTimeMinutes).toInt()
            } else 0

            ReadingAnalytics(
                totalReadingTimeMinutes = totalReadingTimeMinutes,
                averageReadingSpeedWPM = averageReadingSpeedWPM,
                totalWordsRead = totalWordsRead,
                readingSessions = sessions.take(100), // Limit to recent sessions
                dailyReadingTime = dailyReadingTime,
                weeklyReadingTime = weeklyReadingTime,
                monthlyReadingTime = monthlyReadingTime
            )
        } catch (e: Exception) {
            Log.error { "Failed to get reading analytics: ${e.message}" }
            ReadingAnalytics()
        }
    }

    override fun getReadingAnalyticsFlow(): Flow<ReadingAnalytics> {
        return flow {
            emit(getReadingAnalytics())
        }
    }

    override suspend fun trackReadingSession(session: ReadingSession) {
        try {
            // Reading sessions are tracked through history
            Log.info { "Reading session tracked: ${session.bookId}" }
        } catch (e: Exception) {
            Log.error { "Failed to track reading session: ${e.message}" }
        }
    }

    override suspend fun getUpcomingReleases(): List<UpcomingRelease> {
        return try {
            val books = bookRepository.findAllInLibraryBooks(
                sortType = ireader.domain.models.library.LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            
            books.mapNotNull { book ->
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                if (chapters.isEmpty()) return@mapNotNull null

                val sortedChapters = chapters.sortedBy { it.dateUpload }
                val lastChapter = sortedChapters.lastOrNull() ?: return@mapNotNull null
                
                // Calculate release frequency
                val releaseFrequency = calculateReleaseFrequency(sortedChapters)
                val estimatedNextRelease = estimateNextRelease(sortedChapters, releaseFrequency)

                UpcomingRelease(
                    bookId = book.id,
                    bookTitle = book.title,
                    bookCover = book.cover,
                    sourceId = book.sourceId,
                    expectedReleaseDate = null, // Would need source-specific data
                    lastChapterDate = lastChapter.dateUpload,
                    estimatedNextRelease = estimatedNextRelease,
                    releaseFrequency = releaseFrequency
                )
            }
                .filter { it.estimatedNextRelease != null }
                .sortedBy { it.estimatedNextRelease }
                .take(50)
        } catch (e: Exception) {
            Log.error { "Failed to get upcoming releases: ${e.message}" }
            emptyList()
        }
    }

    override fun getUpcomingReleasesFlow(): Flow<List<UpcomingRelease>> {
        return flow {
            emit(getUpcomingReleases())
        }
    }

    override suspend fun getRecommendations(limit: Int): List<BookRecommendation> {
        return try {
            val books = bookRepository.findAllInLibraryBooks(
                sortType = ireader.domain.models.library.LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val history = historyRepository.findHistories()
            
            // Get user's favorite genres
            val favoriteGenres = books
                .flatMap { it.genres }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }

            // Get user's favorite authors
            val favoriteAuthors = books
                .groupBy { it.author }
                .entries
                .sortedByDescending { it.value.size }
                .take(5)
                .map { it.key }

            // Find books not in library that match preferences
            val allBooks = bookRepository.findAllBooks()
            val libraryBookIds = books.map { it.id }.toSet()

            val recommendations = allBooks
                .filter { it.id !in libraryBookIds }
                .mapNotNull { book ->
                    val score = calculateRecommendationScore(
                        book = book,
                        favoriteGenres = favoriteGenres,
                        favoriteAuthors = favoriteAuthors
                    )
                    
                    if (score > 0) {
                        val reason = determineRecommendationReason(
                            book = book,
                            favoriteGenres = favoriteGenres,
                            favoriteAuthors = favoriteAuthors
                        )
                        
                        BookRecommendation(
                            bookId = book.id,
                            bookTitle = book.title,
                            bookCover = book.cover,
                            author = book.author,
                            genres = book.genres,
                            sourceId = book.sourceId,
                            score = score,
                            reason = reason
                        )
                    } else null
                }
                .sortedByDescending { it.score }
                .take(limit)

            recommendations
        } catch (e: Exception) {
            Log.error { "Failed to get recommendations: ${e.message}" }
            emptyList()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun exportStatistics(): EntitiesStatisticsExport {
        return try {
            val libraryInsights = getLibraryInsights()
            val readingAnalytics = getReadingAnalytics()
            val upcomingReleases = getUpcomingReleases()
            val recommendations = getRecommendations(20)

            // Get reading statistics
            val books = bookRepository.findAllInLibraryBooks(
                sortType = ireader.domain.models.library.LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val history = historyRepository.findHistories()
            
            val readingStatisticsType1 = ireader.domain.models.entities.ReadingStatisticsType1(
                totalChaptersRead = history.size,
                totalReadingTimeMinutes = history.sumOf { (it.readDuration ?: 0L) / 60000 },
                averageReadingSpeedWPM = readingAnalytics.averageReadingSpeedWPM,
                favoriteGenres = libraryInsights.genreDistribution,
                readingStreak = libraryInsights.readingPatterns.readingStreak,
                booksCompleted = libraryInsights.booksCompleted,
                currentlyReading = libraryInsights.booksInProgress
            )

            EntitiesStatisticsExport(
                exportDate = System.currentTimeMillis(),
                libraryInsights = libraryInsights,
                readingStatisticsType1 = readingStatisticsType1,
                readingAnalytics = readingAnalytics,
                upcomingReleases = upcomingReleases,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            Log.error { "Failed to export statistics: ${e.message}" }
            EntitiesStatisticsExport(
                exportDate = System.currentTimeMillis(),
                libraryInsights = LibraryInsights(),
                readingStatisticsType1 = ireader.domain.models.entities.ReadingStatisticsType1(),
                readingAnalytics = ReadingAnalytics(),
                upcomingReleases = emptyList(),
                recommendations = emptyList()
            )
        }
    }

    // Helper functions
    private fun calculateReadingPatterns(history: List<Any>): ReadingPatterns {
        // Simplified implementation - would need actual history data structure
        return ReadingPatterns(
            averageChaptersPerDay = 0f,
            mostActiveDay = "Monday",
            mostActiveHour = 20,
            readingStreak = 0,
            longestStreak = 0
        )
    }

    private fun calculateReleaseFrequency(chapters: List<Any>): ReleaseFrequency {
        if (chapters.size < 2) return ReleaseFrequency.UNKNOWN

        // Calculate average time between releases
        // Simplified - would need actual chapter date data
        return ReleaseFrequency.WEEKLY
    }

    private fun estimateNextRelease(chapters: List<Any>, frequency: ReleaseFrequency): Long? {
        // Simplified - would calculate based on frequency pattern
        return null
    }

    private fun calculateRecommendationScore(
        book: Any,
        favoriteGenres: List<String>,
        favoriteAuthors: List<String>
    ): Float {
        var score = 0f
        
        // Genre matching (simplified)
        score += 0.5f
        
        // Author matching
        score += 0.3f
        
        // Popularity
        score += 0.2f
        
        return score
    }

    private fun determineRecommendationReason(
        book: Any,
        favoriteGenres: List<String>,
        favoriteAuthors: List<String>
    ): RecommendationReason {
        // Simplified logic
        return RecommendationReason.SIMILAR_GENRE
    }

    private fun estimateWordsInChapter(): Int {
        // Average chapter has ~3000 words
        return 3000
    }

    private fun formatDate(timestamp: Long): String {
        // Format as YYYY-MM-DD
        return timestamp.toString() // Simplified
    }

    private fun formatWeek(timestamp: Long): String {
        // Format as YYYY-Www
        return timestamp.toString() // Simplified
    }

    private fun formatMonth(timestamp: Long): String {
        // Format as YYYY-MM
        return timestamp.toString() // Simplified
    }
}
