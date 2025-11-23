package ireader.data.leaderboard

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.LeaderboardRepository
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.entities.UserLeaderboardStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LeaderboardRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : LeaderboardRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class LeaderboardDto(
        @SerialName("id") val id: String? = null,
        @SerialName("user_id") val user_id: String,
        @SerialName("username") val username: String,
        @SerialName("total_reading_time_minutes") val total_reading_time_minutes: Long,
        @SerialName("total_chapters_read") val total_chapters_read: Int = 0,
        @SerialName("books_completed") val books_completed: Int = 0,
        @SerialName("reading_streak") val reading_streak: Int = 0,
        @SerialName("has_badge") val has_badge: Boolean = false,
        @SerialName("badge_type") val badge_type: String? = null,
        @SerialName("updated_at") val updated_at: String? = null
    )
    
    override suspend fun getLeaderboard(limit: Int, offset: Int): Result<List<LeaderboardEntry>> =
        RemoteErrorMapper.withErrorMapping {
            val queryResult = backendService.query(
                table = "leaderboard",
                orderBy = "total_reading_time_minutes",
                ascending = false,
                limit = limit,
                offset = offset
            ).getOrThrow()
            
            val entries = queryResult.map { json.decodeFromJsonElement(LeaderboardDto.serializer(), it) }
            
            entries.mapIndexed { index, dto ->
                dto.toDomain(rank = offset + index + 1)
            }
        }
    
    override suspend fun getUserRank(userId: String): Result<LeaderboardEntry?> =
        RemoteErrorMapper.withErrorMapping {
            // Get user's entry
            val userQueryResult = backendService.query(
                table = "leaderboard",
                filters = mapOf("user_id" to userId)
            ).getOrThrow()
            
            val userEntry = userQueryResult.firstOrNull()?.let {
                json.decodeFromJsonElement(LeaderboardDto.serializer(), it)
            } ?: return@withErrorMapping null
            
            // Calculate rank by counting users with more reading time
            val allUsersResult = backendService.query(
                table = "leaderboard",
                columns = "user_id,total_reading_time_minutes"
            ).getOrThrow()
            
            val rank = allUsersResult.count { entry ->
                val dto = json.decodeFromJsonElement(LeaderboardDto.serializer(), entry)
                dto.total_reading_time_minutes > userEntry.total_reading_time_minutes
            } + 1
            
            userEntry.toDomain(rank = rank)
        }
    
    override suspend fun syncUserStats(stats: UserLeaderboardStats): Result<Unit> =
        RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("user_id", stats.userId)
                put("username", stats.username)
                put("total_reading_time_minutes", stats.totalReadingTimeMinutes)
                put("total_chapters_read", stats.totalChaptersRead)
                put("books_completed", stats.booksCompleted)
                put("reading_streak", stats.readingStreak)
                put("has_badge", stats.hasBadge)
                stats.badgeType?.let { put("badge_type", it) }
            }
            
            backendService.upsert(
                table = "leaderboard",
                data = data,
                onConflict = "user_id",
                returning = false
            ).getOrThrow()
            
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
