package ireader.data.statistics

import ireader.core.log.Log
import ireader.data.core.DatabaseHandler
import ireader.data.remote.MultiSupabaseClientProvider
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.GenreCount
import ireader.domain.models.entities.ReadingStatisticsType1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ReadingStatisticsRepositoryImpl(
    private val handler: DatabaseHandler,
    private val multiProvider: MultiSupabaseClientProvider? = null
) : ReadingStatisticsRepository {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncService: StatisticsSyncService? = multiProvider?.let {
        StatisticsSyncService(it, this)
    }

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
        // Trigger sync in background
        triggerSync()
    }

    override suspend fun addReadingTime(minutes: Long) {
        handler.await {
            readingStatisticsQueries.addReadingTime(minutes)
        }
        // Trigger sync in background
        triggerSync()
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
        // Trigger sync in background
        triggerSync()
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
            // Optimized: Use single SQL query instead of N+1 queries
            // This counts books that have at least one read chapter but not all chapters read
            val result = bookQueries.getCurrentlyReadingCount().executeAsList()
            result.size
        }
    }

    private suspend fun getFavoriteGenres(): List<GenreCount> {
        return handler.await {
            // Optimized: Only fetch favorite books' genres from database
            val genreMap = mutableMapOf<String, Int>()
            
            bookQueries.getFavoriteGenres().executeAsList()
                .flatMap { genreList -> genreList ?: emptyList() }
                .forEach { genre ->
                    val trimmedGenre = genre.trim()
                    if (trimmedGenre.isNotBlank()) {
                        genreMap[trimmedGenre] = (genreMap[trimmedGenre] ?: 0) + 1
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
    
    /**
     * Trigger background sync to Supabase
     * This is called after any statistics update
     */
    private fun triggerSync() {
        if (syncService != null) {
            scope.launch {
                try {
                    syncService.syncStatistics()
                    syncService.checkAndAwardAchievements()
                } catch (e: Exception) {
                    Log.error(e, "Background sync failed")
                }
            }
        }
    }
    
    /**
     * Initialize sync service and start periodic sync
     * Should be called when the app starts
     */
    fun initializeSync() {
        if (syncService != null) {
            scope.launch {
                try {
                    // Initial sync on startup
                    syncService.syncStatistics()
                    syncService.checkAndAwardAchievements()
                    
                    // Start periodic sync
                    syncService.startPeriodicSync()
                    
                    Log.info("Statistics sync initialized")
                } catch (e: Exception) {
                    Log.error(e, "Failed to initialize statistics sync")
                }
            }
        }
    }
    
    /**
     * Manually trigger a full sync
     * Can be called from UI (e.g., pull-to-refresh)
     */
    suspend fun manualSync(): Result<Unit> {
        return syncService?.syncStatistics() ?: Result.failure(Exception("Sync service not available"))
    }
    
    /**
     * Get user badges from Supabase
     */
    suspend fun getUserBadges(): Result<List<UserBadge>> {
        return syncService?.syncUserBadges() ?: Result.failure(Exception("Sync service not available"))
    }
}
