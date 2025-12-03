package ireader.data.statistics

import ireader.core.log.Log
import ireader.data.remote.MultiSupabaseClientProvider
import ireader.domain.data.repository.ReadingStatisticsRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Service for syncing reading statistics with Supabase Analytics (Project 7)
 * 
 * This service ensures that:
 * - Reading time is synced to the leaderboard
 * - Statistics are never reset (only incremented)
 * - Local and remote data are merged (max values)
 * - Achievements and badges are tracked
 */
class StatisticsSyncService(
    private val multiProvider: MultiSupabaseClientProvider,
    private val statisticsRepository: ReadingStatisticsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val timestamp: Long) : SyncState()
        data class Error(val message: String) : SyncState()
    }
    
    /**
     * Sync local statistics to Supabase leaderboard
     * Uses merge strategy: takes maximum values to prevent data loss
     */
    suspend fun syncStatistics(): Result<Unit> {
        return try {
            _syncState.value = SyncState.Syncing
            
            val userId = multiProvider.getCurrentUserId()
            if (userId == null) {
                Log.warn("Cannot sync statistics: User not authenticated")
                _syncState.value = SyncState.Error("Not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            val username = multiProvider.getCurrentUsername() ?: "User_${userId.take(8)}"
            
            // Get local statistics
            val localStats = statisticsRepository.getStatistics()
            
            // Fetch remote statistics from leaderboard
            val remoteStats = fetchRemoteStatistics(userId)
            
            // Merge statistics (take maximum values to prevent data loss)
            val mergedStats = if (remoteStats != null) {
                LeaderboardEntry(
                    user_id = userId,
                    username = username,
                    total_reading_time_minutes = maxOf(
                        localStats.totalReadingTimeMinutes,
                        remoteStats.total_reading_time_minutes
                    ),
                    total_chapters_read = maxOf(
                        localStats.totalChaptersRead,
                        remoteStats.total_chapters_read
                    ),
                    books_completed = maxOf(
                        localStats.booksCompleted,
                        remoteStats.books_completed
                    ),
                    reading_streak = maxOf(
                        localStats.readingStreak,
                        remoteStats.reading_streak
                    ),
                    has_badge = remoteStats.has_badge, // Preserve badge status
                    badge_type = remoteStats.badge_type
                )
            } else {
                // No remote data, use local data
                LeaderboardEntry(
                    user_id = userId,
                    username = username,
                    total_reading_time_minutes = localStats.totalReadingTimeMinutes,
                    total_chapters_read = localStats.totalChaptersRead,
                    books_completed = localStats.booksCompleted,
                    reading_streak = localStats.readingStreak,
                    has_badge = false,
                    badge_type = null
                )
            }
            
            // Upsert to Supabase (insert or update)
            upsertToLeaderboard(mergedStats)
            
            // Update local statistics if remote had higher values
            if (remoteStats != null) {
                updateLocalStatisticsIfNeeded(localStats, remoteStats)
            }
            
            _syncState.value = SyncState.Success(currentTimeToLong())
            Log.info("Statistics synced successfully for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e, "Failed to sync statistics")
            _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    /**
     * Fetch remote statistics from Supabase leaderboard
     */
    private suspend fun fetchRemoteStatistics(userId: String): LeaderboardEntry? {
        return try {
            val result = multiProvider.analyticsClient
                .from("leaderboard")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<LeaderboardEntry>()
            
            result
        } catch (e: Exception) {
            Log.error(e, "Failed to fetch remote statistics")
            null
        }
    }
    
    /**
     * Upsert statistics to Supabase leaderboard
     */
    private suspend fun upsertToLeaderboard(entry: LeaderboardEntry) {
        multiProvider.analyticsClient
            .from("leaderboard")
            .upsert(entry) {
                onConflict = "user_id"
            }
    }
    
    /**
     * Update local statistics if remote has higher values
     * This ensures data is never lost when syncing from another device
     */
    private suspend fun updateLocalStatisticsIfNeeded(
        local: ireader.domain.models.entities.ReadingStatisticsType1,
        remote: LeaderboardEntry
    ) {
        // Update reading time if remote is higher
        if (remote.total_reading_time_minutes > local.totalReadingTimeMinutes) {
            val diff = remote.total_reading_time_minutes - local.totalReadingTimeMinutes
            statisticsRepository.addReadingTime(diff)
        }
        
        // Update chapters read if remote is higher
        if (remote.total_chapters_read > local.totalChaptersRead) {
            val diff = remote.total_chapters_read - local.totalChaptersRead
            repeat(diff) {
                statisticsRepository.incrementChaptersRead()
            }
        }
        
        // Update books completed if remote is higher
        if (remote.books_completed > local.booksCompleted) {
            val diff = remote.books_completed - local.booksCompleted
            repeat(diff) {
                statisticsRepository.incrementBooksCompleted()
            }
        }
        
        // Update streak if remote is higher
        if (remote.reading_streak > local.readingStreak) {
            val lastReadDate = statisticsRepository.getLastReadDate() ?: currentTimeToLong()
            statisticsRepository.updateStreak(remote.reading_streak, lastReadDate)
        }
    }
    
    /**
     * Sync user badges from Supabase
     * This fetches all badges earned by the user
     */
    suspend fun syncUserBadges(): Result<List<UserBadge>> {
        return try {
            val userId = multiProvider.getCurrentUserId()
            if (userId == null) {
                Log.warn("Cannot sync badges: User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            val badges = multiProvider.badgesClient
                .from("user_badges")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserBadge>()
            
            Log.info("Synced ${badges.size} badges for user $userId")
            Result.success(badges)
        } catch (e: Exception) {
            Log.error(e, "Failed to sync user badges")
            Result.failure(e)
        }
    }
    
    /**
     * Check and award achievement badges based on statistics
     * This should be called after syncing statistics
     */
    suspend fun checkAndAwardAchievements(): Result<List<String>> {
        return try {
            val userId = multiProvider.getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val stats = statisticsRepository.getStatistics()
            val awardedBadges = mutableListOf<String>()
            
            // Check reading progress achievements
            val readingBadges = mapOf(
                10 to "novice_reader",
                100 to "avid_reader",
                500 to "bookworm",
                1000 to "master_reader"
            )
            
            readingBadges.forEach { (threshold, badgeId) ->
                if (stats.totalChaptersRead >= threshold) {
                    if (awardBadge(userId, badgeId)) {
                        awardedBadges.add(badgeId)
                    }
                }
            }
            
            // Check book completion achievements
            val completionBadges = mapOf(
                1 to "first_finish",
                10 to "book_collector",
                50 to "library_master",
                100 to "legendary_collector"
            )
            
            completionBadges.forEach { (threshold, badgeId) ->
                if (stats.booksCompleted >= threshold) {
                    if (awardBadge(userId, badgeId)) {
                        awardedBadges.add(badgeId)
                    }
                }
            }
            
            // Check streak achievements
            val streakBadges = mapOf(
                7 to "week_warrior",
                30 to "month_master",
                365 to "year_legend"
            )
            
            streakBadges.forEach { (threshold, badgeId) ->
                if (stats.readingStreak >= threshold) {
                    if (awardBadge(userId, badgeId)) {
                        awardedBadges.add(badgeId)
                    }
                }
            }
            
            if (awardedBadges.isNotEmpty()) {
                Log.info("Awarded ${awardedBadges.size} new badges: $awardedBadges")
            }
            
            Result.success(awardedBadges)
        } catch (e: Exception) {
            Log.error(e, "Failed to check and award achievements")
            Result.failure(e)
        }
    }
    
    /**
     * Award a badge to the user
     * Returns true if badge was newly awarded, false if already owned
     */
    private suspend fun awardBadge(userId: String, badgeId: String): Boolean {
        return try {
            // Check if user already has this badge
            val existing = multiProvider.badgesClient
                .from("user_badges")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("badge_id", badgeId)
                    }
                }
                .decodeSingleOrNull<UserBadge>()
            
            if (existing != null) {
                return false // Already has badge
            }
            
            // Award the badge
            val newBadge = UserBadge(
                user_id = userId,
                badge_id = badgeId,
                earned_at = null, // Will use server default
                is_primary = false,
                is_featured = false,
                metadata = null
            )
            
            multiProvider.badgesClient
                .from("user_badges")
                .insert(newBadge)
            
            true
        } catch (e: Exception) {
            Log.error(e, "Failed to award badge $badgeId to user $userId")
            false
        }
    }
    
    /**
     * Start automatic periodic sync
     * Syncs statistics every 5 minutes
     */
    fun startPeriodicSync() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(5 * 60 * 1000) // 5 minutes
                syncStatistics()
                checkAndAwardAchievements()
            }
        }
    }
}

/**
 * Leaderboard entry model matching Supabase schema
 */
@Serializable
data class LeaderboardEntry(
    val user_id: String,
    val username: String,
    val total_reading_time_minutes: Long,
    val total_chapters_read: Int,
    val books_completed: Int,
    val reading_streak: Int,
    val has_badge: Boolean = false,
    val badge_type: String? = null
)

/**
 * User badge model matching Supabase schema
 */
@Serializable
data class UserBadge(
    val user_id: String,
    val badge_id: String,
    val earned_at: String? = null,
    val is_primary: Boolean = false,
    val is_featured: Boolean = false,
    val metadata: String? = null
)
