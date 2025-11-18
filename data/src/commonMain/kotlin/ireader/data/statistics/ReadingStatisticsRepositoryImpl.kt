package ireader.data.statistics

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.GenreCount
import ireader.domain.models.entities.ReadingStatisticsType1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingStatisticsRepositoryImpl(
    private val handler: DatabaseHandler,
) : ReadingStatisticsRepository {

    /**
     * Ensures the reading_statistics table has the required row initialized
     */
    private suspend fun ensureInitialized() {
        handler.await {
            readingStatisticsQueries.initializeIfNeeded()
        }
    }

    override fun getStatisticsFlow(): Flow<ReadingStatisticsType1> {
        return handler.subscribeToOne { 
            // Ensure initialized before querying
            readingStatisticsQueries.initializeIfNeeded()
            readingStatisticsQueries.getStatistics() 
        }.map { dbStats ->
            ReadingStatisticsType1(
                totalChaptersRead = dbStats.total_chapters_read.toInt(),
                totalReadingTimeMinutes = dbStats.total_reading_time_minutes,
                averageReadingSpeedWPM = calculateWPM(dbStats.total_words_read.toInt(), dbStats.total_reading_time_minutes),
                favoriteGenres = getFavoriteGenres(),
                readingStreak = dbStats.reading_streak.toInt(),
                booksCompleted = dbStats.books_completed.toInt(),
                currentlyReading = getCurrentlyReading()
            )
        }
    }

    override suspend fun getStatistics(): ReadingStatisticsType1 {
        ensureInitialized()
        
        val dbStats = handler.awaitOne { 
            readingStatisticsQueries.getStatistics() 
        }
        
        return ReadingStatisticsType1(
            totalChaptersRead = dbStats.total_chapters_read.toInt(),
            totalReadingTimeMinutes = dbStats.total_reading_time_minutes,
            averageReadingSpeedWPM = calculateWPM(dbStats.total_words_read.toInt(), dbStats.total_reading_time_minutes),
            favoriteGenres = getFavoriteGenres(),
            readingStreak = dbStats.reading_streak.toInt(),
            booksCompleted = dbStats.books_completed.toInt(),
            currentlyReading = getCurrentlyReading()
        )
    }

    override suspend fun getLastReadDate(): Long? {
        ensureInitialized()
        
        val dbStats = handler.awaitOne { 
            readingStatisticsQueries.getStatistics() 
        }
        return dbStats.last_read_date
    }

    override suspend fun getCurrentStreak(): Int {
        ensureInitialized()
        
        val dbStats = handler.awaitOne { 
            readingStatisticsQueries.getStatistics() 
        }
        return dbStats.reading_streak.toInt()
    }

    override suspend fun incrementChaptersRead() {
        handler.await {
            readingStatisticsQueries.incrementChaptersRead()
        }
    }

    override suspend fun addReadingTime(minutes: Long) {
        handler.await {
            readingStatisticsQueries.addReadingTime(minutes)
        }
    }

    override suspend fun updateStreak(streak: Int, lastReadDate: Long) {
        handler.await {
            readingStatisticsQueries.updateStreak(
                streak = streak.toLong(),
                lastReadDate = lastReadDate
            )
        }
    }

    override suspend fun addWordsRead(words: Int) {
        handler.await {
            readingStatisticsQueries.addWordsRead(words.toLong())
        }
    }

    override suspend fun incrementBooksCompleted() {
        handler.await {
            readingStatisticsQueries.incrementBooksCompleted()
        }
    }

    override suspend fun getBooksCompleted(): Int {
        ensureInitialized()
        
        val dbStats = handler.awaitOne { 
            readingStatisticsQueries.getStatistics() 
        }
        return dbStats.books_completed.toInt()
    }

    override suspend fun getCurrentlyReading(): Int {
        return handler.await {
            // Count books with at least one read chapter but not all chapters read
            bookQueries.findAllBooks().executeAsList().count { book ->
                val chapters = chapterQueries.getChaptersByMangaId(book._id).executeAsList()
                val readChapters = chapters.count { it.read }
                readChapters > 0 && readChapters < chapters.size
            }
        }
    }

    private suspend fun getFavoriteGenres(): List<GenreCount> {
        return handler.await {
            // Get all books in library and count genres
            val books = bookQueries.findAllBooks().executeAsList()
            val genreMap = mutableMapOf<String, Int>()
            
            books.filter { it.favorite }.forEach { book ->
                // Genre is now a List<String>
                book.genre?.forEach { genre ->
                    val trimmedGenre = genre.trim()
                    if (trimmedGenre.isNotBlank()) {
                        genreMap[trimmedGenre] = (genreMap[trimmedGenre] ?: 0) + 1
                    }
                }
            }
            
            genreMap.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { GenreCount(it.key, it.value) }
        }
    }

    private fun calculateWPM(totalWords: Int, totalMinutes: Long): Int {
        return if (totalMinutes > 0) {
            (totalWords / totalMinutes).toInt()
        } else {
            0
        }
    }
}
