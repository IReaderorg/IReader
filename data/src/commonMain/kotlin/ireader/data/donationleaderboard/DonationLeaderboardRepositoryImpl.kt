package ireader.data.donationleaderboard

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.DonationLeaderboardRepository
import ireader.domain.models.entities.DonationLeaderboardEntry
import ireader.domain.models.entities.DonorBadgeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ireader.domain.utils.extensions.currentTimeToLong

class DonationLeaderboardRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : DonationLeaderboardRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class DonationLeaderboardDto(
        @SerialName("user_id") val userId: String,
        @SerialName("username") val username: String,
        @SerialName("total_donation_amount") val totalDonationAmount: Double,
        @SerialName("badge_count") val badgeCount: Int,
        @SerialName("highest_badge_rarity") val highestBadgeRarity: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null
    )
    
    @Serializable
    private data class DonorBadgeDto(
        @SerialName("badge_id") val badgeId: String,
        @SerialName("badge_name") val badgeName: String,
        @SerialName("badge_icon") val badgeIcon: String,
        @SerialName("badge_rarity") val badgeRarity: String,
        @SerialName("badge_price") val price: Double,
        @SerialName("earned_at") val earnedAt: String
    )
    
    override suspend fun getDonationLeaderboard(limit: Int, offset: Int): Result<List<DonationLeaderboardEntry>> =
        RemoteErrorMapper.withErrorMapping {
            val resultJson = backendService.rpc(
                function = "get_donation_leaderboard",
                parameters = mapOf(
                    "p_limit" to limit,
                    "p_offset" to offset
                )
            ).getOrThrow()
            
            val entries = json.decodeFromJsonElement(
                ListSerializer(DonationLeaderboardDto.serializer()),
                resultJson
            )
            
            entries.mapIndexed { index, dto ->
                dto.toDomain(rank = offset + index + 1)
            }
        }
    
    override suspend fun getUserDonationRank(userId: String): Result<DonationLeaderboardEntry?> =
        RemoteErrorMapper.withErrorMapping {
            val resultJson = backendService.rpc(
                function = "get_user_donation_rank",
                parameters = mapOf("p_user_id" to userId)
            ).getOrThrow()
            
            val entries = json.decodeFromJsonElement(
                ListSerializer(DonationLeaderboardDto.serializer()),
                resultJson
            )
            
            entries.firstOrNull()?.let { dto ->
                // Get user's badges
                val badgesJson = backendService.rpc(
                    function = "get_user_donation_badges",
                    parameters = mapOf("p_user_id" to userId)
                ).getOrNull()
                
                val badges = badgesJson?.let {
                    json.decodeFromJsonElement(
                        ListSerializer(DonorBadgeDto.serializer()),
                        it
                    ).map { badgeDto -> badgeDto.toDomain() }
                } ?: emptyList()
                
                // Calculate rank
                val allDonorsResult = backendService.rpc(
                    function = "get_donation_leaderboard",
                    parameters = mapOf("p_limit" to 1000, "p_offset" to 0)
                ).getOrThrow()
                
                val allDonors = json.decodeFromJsonElement(
                    ListSerializer(DonationLeaderboardDto.serializer()),
                    allDonorsResult
                )
                
                val rank = allDonors.indexOfFirst { it.userId == userId } + 1
                
                dto.toDomain(rank = if (rank > 0) rank else allDonors.size + 1, badges = badges)
            }
        }
    
    override fun observeDonationLeaderboard(limit: Int): Flow<List<DonationLeaderboardEntry>> = flow {
        // Emit initial data
        val initial = getDonationLeaderboard(limit).getOrNull() ?: emptyList()
        emit(initial)
        
        try {
            val channel = supabaseClient.realtime.channel("donation_leaderboard_updates")
            channel.subscribe()
            
            // Listen for changes on user_badges table (when badges are purchased)
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "user_badges"
            }.collect { action ->
                // Refresh leaderboard on any badge change
                val updated = getDonationLeaderboard(limit).getOrNull() ?: emptyList()
                emit(updated)
            }
        } catch (e: Exception) {
            // Fallback to polling if realtime fails
            while (true) {
                kotlinx.coroutines.delay(30000) // Poll every 30 seconds
                val updated = getDonationLeaderboard(limit).getOrNull() ?: emptyList()
                emit(updated)
            }
        }
    }
    
    override suspend fun getTopDonors(limit: Int): Result<List<DonationLeaderboardEntry>> =
        getDonationLeaderboard(limit = limit, offset = 0)
    
    private fun DonationLeaderboardDto.toDomain(
        rank: Int,
        badges: List<DonorBadgeInfo> = emptyList()
    ): DonationLeaderboardEntry {
        return DonationLeaderboardEntry(
            userId = userId,
            username = username,
            totalDonationAmount = totalDonationAmount,
            badgeCount = badgeCount,
            rank = rank,
            avatarUrl = avatarUrl,
            highestBadgeRarity = highestBadgeRarity,
            badges = badges,
            updatedAt = currentTimeToLong()
        )
    }
    
    private fun DonorBadgeDto.toDomain(): DonorBadgeInfo {
        return DonorBadgeInfo(
            badgeId = badgeId,
            badgeName = badgeName,
            badgeIcon = badgeIcon,
            badgeRarity = badgeRarity,
            price = price,
            earnedAt = parseTimestamp(earnedAt)
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return currentTimeToLong()
        return try {
            currentTimeToLong()
        } catch (e: Exception) {
            currentTimeToLong()
        }
    }
}
