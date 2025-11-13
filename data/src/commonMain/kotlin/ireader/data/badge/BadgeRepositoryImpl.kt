package ireader.data.badge

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class BadgeRepositoryImpl(
    private val handler: DatabaseHandler,
    private val supabaseClient: SupabaseClient
) : BadgeRepository {
    
    @Serializable
    private data class UserBadgeDto(
        @SerialName("badge_id") val badgeId: String,
        @SerialName("badge_name") val badgeName: String,
        @SerialName("badge_description") val badgeDescription: String,
        @SerialName("badge_icon") val badgeIcon: String,
        @SerialName("badge_category") val badgeCategory: String,
        @SerialName("badge_rarity") val badgeRarity: String,
        @SerialName("earned_at") val earnedAt: String,
        val metadata: Map<String, String>? = null
    )
    
    override suspend fun getUserBadges(userId: String?): Result<List<UserBadge>> = 
        RemoteErrorMapper.withErrorMapping {
            val targetUserId = userId ?: supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val result = supabaseClient.postgrest.rpc(
                function = "get_user_badges",
                parameters = mapOf("p_user_id" to targetUserId)
            ).decodeList<UserBadgeDto>()
            
            result.map {
                UserBadge(
                    badgeId = it.badgeId,
                    badgeName = it.badgeName,
                    badgeDescription = it.badgeDescription,
                    badgeIcon = it.badgeIcon,
                    badgeCategory = it.badgeCategory,
                    badgeRarity = it.badgeRarity,
                    earnedAt = it.earnedAt,
                    metadata = it.metadata
                )
            }
        }
    
    override fun observeUserBadges(userId: String?): Flow<List<UserBadge>> = flow {
        val targetUserId = userId ?: supabaseClient.auth.currentUserOrNull()?.id 
            ?: throw Exception("User not authenticated")
        
        getUserBadges(targetUserId).onSuccess { badges ->
            emit(badges)
        }
    }
}
