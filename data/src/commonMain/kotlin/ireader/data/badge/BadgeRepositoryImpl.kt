package ireader.data.badge

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.BadgeType
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentStatus
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
    
    @Serializable
    private data class BadgeDto(
        val id: String,
        val name: String,
        val description: String,
        @SerialName("image_url") val imageUrl: String,
        val price: Double? = null,
        val type: String,
        val rarity: String,
        @SerialName("is_available") val isAvailable: Boolean = true,
        val icon: String? = null,
        val category: String? = null
    )
    
    @Serializable
    private data class PaymentProofDto(
        @SerialName("user_id") val userId: String,
        @SerialName("badge_id") val badgeId: String,
        @SerialName("transaction_id") val transactionId: String,
        @SerialName("payment_method") val paymentMethod: String,
        @SerialName("proof_image_url") val proofImageUrl: String? = null,
        val status: String = "PENDING",
        @SerialName("submitted_at") val submittedAt: String? = null
    )
    
    @Serializable
    private data class UserBadgeUpdateDto(
        @SerialName("user_id") val userId: String,
        @SerialName("badge_id") val badgeId: String,
        @SerialName("is_primary") val isPrimary: Boolean? = null,
        @SerialName("is_featured") val isFeatured: Boolean? = null
    )
    
    @Serializable
    private data class BadgeWithUserInfoDto(
        @SerialName("badge_id") val badgeId: String,
        @SerialName("badge_name") val badgeName: String,
        @SerialName("badge_description") val badgeDescription: String,
        @SerialName("badge_icon") val badgeIcon: String,
        @SerialName("image_url") val imageUrl: String? = null,
        val price: Double? = null,
        val type: String? = null,
        val rarity: String? = null,
        @SerialName("is_primary") val isPrimary: Boolean? = null,
        @SerialName("is_featured") val isFeatured: Boolean? = null
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
    
    override suspend fun getAvailableBadges(): Result<List<Badge>> = 
        RemoteErrorMapper.withErrorMapping {
            val result = supabaseClient.postgrest["badges"]
                .select {
                    filter {
                        eq("is_available", true)
                    }
                }
                .decodeList<BadgeDto>()
            
            result.map { dto ->
                Badge(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    icon = dto.icon ?: dto.imageUrl,
                    category = dto.category ?: "general",
                    rarity = dto.rarity,
                    price = dto.price,
                    type = parseBadgeType(dto.type),
                    badgeRarity = parseBadgeRarity(dto.rarity),
                    imageUrl = dto.imageUrl,
                    isAvailable = dto.isAvailable
                )
            }
        }
    
    override suspend fun submitPaymentProof(badgeId: String, proof: PaymentProof): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val proofDto = PaymentProofDto(
                userId = userId,
                badgeId = badgeId,
                transactionId = proof.transactionId,
                paymentMethod = proof.paymentMethod,
                proofImageUrl = proof.proofImageUrl,
                status = "PENDING"
            )
            
            supabaseClient.postgrest["payment_proofs"].insert(proofDto)
        }
    
    override suspend fun setPrimaryBadge(badgeId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // First, verify user owns the badge
            val userBadges = getUserBadges(userId).getOrThrow()
            if (!userBadges.any { it.badgeId == badgeId }) {
                throw Exception("User does not own this badge")
            }
            
            // Set all badges to not primary
            supabaseClient.postgrest["user_badges"]
                .update({
                    set("is_primary", false)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            // Set selected badge as primary
            supabaseClient.postgrest["user_badges"]
                .update({
                    set("is_primary", true)
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("badge_id", badgeId)
                    }
                }
        }
    
    override suspend fun setFeaturedBadges(badgeIds: List<String>): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Validate max 3 badges
            if (badgeIds.size > 3) {
                throw Exception("Cannot feature more than 3 badges")
            }
            
            // Verify user owns all badges
            val userBadges = getUserBadges(userId).getOrThrow()
            val ownedBadgeIds = userBadges.map { it.badgeId }.toSet()
            if (!badgeIds.all { it in ownedBadgeIds }) {
                throw Exception("User does not own all selected badges")
            }
            
            // Set all badges to not featured
            supabaseClient.postgrest["user_badges"]
                .update({
                    set("is_featured", false)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            // Set selected badges as featured
            if (badgeIds.isNotEmpty()) {
                for (badgeId in badgeIds) {
                    supabaseClient.postgrest["user_badges"]
                        .update({
                            set("is_featured", true)
                        }) {
                            filter {
                                eq("user_id", userId)
                                eq("badge_id", badgeId)
                            }
                        }
                }
            }
        }
    
    override suspend fun getPrimaryBadge(): Result<Badge?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Query user_badges joined with badges table for primary badge
            val result = supabaseClient.postgrest.rpc(
                function = "get_user_badges_with_details",
                parameters = mapOf("p_user_id" to userId)
            ).decodeList<BadgeWithUserInfoDto>()
            
            val primaryBadgeDto = result.firstOrNull { it.isPrimary == true }
            
            primaryBadgeDto?.let { dto ->
                Badge(
                    id = dto.badgeId,
                    name = dto.badgeName,
                    description = dto.badgeDescription,
                    icon = dto.badgeIcon,
                    category = "general",
                    rarity = dto.rarity ?: "COMMON",
                    price = dto.price,
                    type = parseBadgeType(dto.type ?: "ACHIEVEMENT"),
                    badgeRarity = parseBadgeRarity(dto.rarity ?: "COMMON"),
                    imageUrl = dto.imageUrl ?: dto.badgeIcon,
                    isAvailable = true
                )
            }
        }
    
    override suspend fun getFeaturedBadges(): Result<List<Badge>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Query user_badges joined with badges table for featured badges
            val result = supabaseClient.postgrest.rpc(
                function = "get_user_badges_with_details",
                parameters = mapOf("p_user_id" to userId)
            ).decodeList<BadgeWithUserInfoDto>()
            
            result
                .filter { it.isFeatured == true }
                .take(3)
                .map { dto ->
                    Badge(
                        id = dto.badgeId,
                        name = dto.badgeName,
                        description = dto.badgeDescription,
                        icon = dto.badgeIcon,
                        category = "general",
                        rarity = dto.rarity ?: "COMMON",
                        price = dto.price,
                        type = parseBadgeType(dto.type ?: "ACHIEVEMENT"),
                        badgeRarity = parseBadgeRarity(dto.rarity ?: "COMMON"),
                        imageUrl = dto.imageUrl ?: dto.badgeIcon,
                        isAvailable = true
                    )
                }
        }
    
    private fun parseBadgeType(type: String): BadgeType {
        return when (type.uppercase()) {
            "PURCHASABLE" -> BadgeType.PURCHASABLE
            "NFT_EXCLUSIVE" -> BadgeType.NFT_EXCLUSIVE
            "ACHIEVEMENT" -> BadgeType.ACHIEVEMENT
            else -> BadgeType.ACHIEVEMENT
        }
    }
    
    private fun parseBadgeRarity(rarity: String): BadgeRarity {
        return when (rarity.uppercase()) {
            "COMMON" -> BadgeRarity.COMMON
            "RARE" -> BadgeRarity.RARE
            "EPIC" -> BadgeRarity.EPIC
            "LEGENDARY" -> BadgeRarity.LEGENDARY
            else -> BadgeRarity.COMMON
        }
    }
}
