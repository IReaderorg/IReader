package ireader.data.leaderboard

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.UserLeaderboardStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class LeaderboardRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : LeaderboardRepository {
    
    @Serializable
    private data class LeaderboardDto(
        val id: String? = null,
        val user_id: String,
        val username: String,
        val total_reading_time_minutes: Long,
        val total_chapters_read: Int = 0,
        val books_completed: Int = 0,
        val reading_streak: Int = 0,
        val has_badge: Boolean = false,
        val badge_type: String? = null,
        val updated_at: String? = null
    )
    
    override suspend fun getLeaderboard(limit: Int, offset: Int): Result<List<LeaderboardEntry>> =
        RemoteErrorMapper.withErrorMapping {
            try {
                val entries = supabaseClient.postgrest["leaderboard"]
                    .select {
                        filter {
                            // Order by reading time descending
                        }
                        limit(limit.toLong())
                        range(offset.toLong() until (offset + limit).toLong())
                    }
                    .decodeList<LeaderboardDto>()
                
                entries.mapIndexed { index, dto ->
                    dto.toDomain(rank = offset + index + 1)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    
    override suspend fun getUserRank(userId: String): Result<LeaderboardEntry?> =
        RemoteErrorMapper.withErrorMapping {
            try {
                // Get user's entry
                val userEntry = supabaseClient.postgrest["leaderboard"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeSingle<LeaderboardDto>()
                
                // Calculate rank by counting users with more reading time
                val rankResult = supabaseClient.postgrest["leaderboard"]
                    .select(columns = Columns.list("user_id")) {
                        filter {
                            gt("total_reading_time_minutes", userEntry.total_reading_time_minutes)
                        }
                    }
                    .decodeList<Map<String, String>>()
                
                val rank = rankResult.size + 1
                userEntry.toDomain(rank = rank)
            } catch (e: Exception) {
                null
            }
        }
    
    override suspend fun syncUserStats(stats: UserLeaderboardStats): Result<Unit> =
        RemoteErrorMapper.withErrorMapping {
            val dto = LeaderboardDto(
                user_id = stats.userId,
                username = stats.username,
                total_reading_time_minutes = stats.totalReadingTimeMinutes,
                total_chapters_read = stats.totalChaptersRead,
                books_completed = stats.booksCompleted,
                reading_streak = stats.readingStreak,
                has_badge = stats.hasBadge,
                badge_type = stats.badgeType,
                updated_at = null
            )
            
            supabaseClient.postgrest["leaderboard"]
                .upsert(dto) {
                    select(Columns.ALL)
                }
            
            Unit
        }
    
    override fun observeLeaderboard(limit: Int): Flow<List<LeaderboardEntry>> = flow {
        // Emit initial data
        val initial = getLeaderboard(limit).getOrNull() ?: emptyList()
        emit(initial)
        
        try {
            val channel = supabaseClient.realtime.channel("leaderboard_updates")
            channel.subscribe()
            
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "leaderboard"
            }.collect { action ->
                // Refresh leaderboard on any change
                val updated = getLeaderboard(limit).getOrNull() ?: emptyList()
                emit(updated)
            }
        } catch (e: Exception) {
            // Fallback to polling if realtime fails
            while (true) {
                kotlinx.coroutines.delay(30000) // Poll every 30 seconds
                val updated = getLeaderboard(limit).getOrNull() ?: emptyList()
                emit(updated)
            }
        }
    }
    
    override suspend fun getTopUsers(limit: Int): Result<List<LeaderboardEntry>> =
        getLeaderboard(limit = limit, offset = 0)
    
    override suspend fun getUsersAroundRank(rank: Int, range: Int): Result<List<LeaderboardEntry>> {
        val startRank = (rank - range).coerceAtLeast(1)
        val offset = startRank - 1
        val limit = range * 2 + 1
        return getLeaderboard(limit = limit, offset = offset)
    }
    
    private fun LeaderboardDto.toDomain(rank: Int): LeaderboardEntry {
        return LeaderboardEntry(
            id = id,
            userId = user_id,
            username = username,
            totalReadingTimeMinutes = total_reading_time_minutes,
            rank = rank,
            hasBadge = has_badge,
            badgeType = badge_type,
            updatedAt = parseTimestamp(updated_at)
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        return try {
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
