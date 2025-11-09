package ireader.data.statistics

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.GenreCount
import ireader.domain.models.entities.ReadingStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingStatisticsRepositoryImpl(
    private val handler: DatabaseHandler,
) : ReadingStatisticsRepository {

    override fun getStatisticsFlow(): Flow<ReadingStatistics> {
        return handler.subscribeToOne { 
            readingStatisticsQueries.getStatistics(statisticsMapper) 
        }.map { dbStats ->
            ReadingStatistics(
                totalChaptersRead = dbStats.totalChaptersRead.toInt(),
                totalReadingTimeMinutes = dbStats.totalReadingTimeMinutes,
                averageReadingSpeedWPM = calculateWPM(dbStats.totalWordsRead.toInt(), dbStats.totalReadingTimeMinutes),
                favoriteGenres = getFavoriteGenres(),
                readingStreak = dbStats.readingStreak.toInt(),
                booksCompleted = getBooksCompleted(),
                currentlyReading = getCurrentlyReading()
            )
        }
    }

    override suspend fun getStatistics(): ReadingStatistics {
        val dbStats = handler.awaitOne { 
            readingStatisticsQueries.getStatistics(statisticsMapper) 
        }
        
        return ReadingStatistics(
            totalChaptersRead = dbStats.totalChaptersRead.toInt(),
            totalReadingTimeMinutes = dbStats.totalReadingTimeMinutes,
            averageReadingSpeedWPM = calculateWPM(dbStats.totalWordsRead.toInt(), dbStats.totalReadingTimeMinutes),
            favoriteGenres = getFavoriteGenres(),
            readingStreak = dbStats.readingStreak.toInt(),
            booksCompleted = getBooksCompleted(),
            currentlyReading = getCurrentlyReading()
        )
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

    override suspend fun getBooksCompleted(): Int {
        return handler.await {
            // Count books where all chapters are read
            bookQueries.findAllBooks().executeAsList().count { book ->
                val totalChapters = chapterQueries.getChaptersByMangaId(book._id).executeAsList().size
                val readChapters = chapterQueries.getChaptersByMangaId(book._id).executeAsList().count { it.read }
                totalChapters > 0 && totalChapters == readChapters
            }
        }
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
                // Genre is stored as semicolon-separated string
                book.genre?.split(";")?.forEach { genre ->
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
