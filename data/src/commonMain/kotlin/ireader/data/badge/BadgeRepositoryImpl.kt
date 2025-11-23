package ireader.data.badge

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.BadgeType
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.UserBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BadgeRepositoryImpl(
    private val handler: DatabaseHandler,
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : BadgeRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class UserBadgeDto(
        @SerialName("badge_id") val badgeId: String,
        @SerialName("badge_name") val badgeName: String,
        @SerialName("badge_description") val badgeDescription: String,
        @SerialName("badge_icon") val badgeIcon: String,
        @SerialName("badge_category") val badgeCategory: String,
        @SerialName("badge_rarity") val badgeRarity: String,
        @SerialName("earned_at") val earnedAt: String,
        val metadata: Map<String, String>? = null,
        @SerialName("badge_image_url") val badgeImageUrl: String? = null,
        @SerialName("badge_type") val badgeType: String? = null,
        @SerialName("is_primary") val isPrimary: Boolean? = null,
        @SerialName("is_featured") val isFeatured: Boolean? = null
    )
    
    @Serializable
    private data class BadgeDto(
        val id: String,
        val name: String,
        val description: String,
        @SerialName("image_url") val imageUrl: String? = null,
        val price: Double? = null,
        val type: String,
        val rarity: String,
        @SerialName("is_available") val isAvailable: Boolean = true,
        val icon: String? = null,
        val category: String? = null
    )
    
    @Serializable
    private data class PaymentProofDto(
        val id: String? = null,
        @SerialName("user_id") val userId: String,
        @SerialName("badge_id") val badgeId: String,
        @SerialName("transaction_id") val transactionId: String,
        @SerialName("payment_method") val paymentMethod: String,
        @SerialName("proof_image_url") val proofImageUrl: String? = null,
        val status: String = "PENDING",
        @SerialName("submitted_at") val submittedAt: String? = null,
        @SerialName("reviewed_at") val reviewedAt: String? = null,
        @SerialName("reviewed_by") val reviewedBy: String? = null
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
        @SerialName("badge_image_url") val imageUrl: String? = null,
        @SerialName("badge_price") val price: Double? = null,
        @SerialName("badge_type") val type: String? = null,
        @SerialName("badge_rarity") val rarity: String? = null,
        @SerialName("is_primary") val isPrimary: Boolean? = null,
        @SerialName("is_featured") val isFeatured: Boolean? = null
    )
    
    override suspend fun getUserBadges(userId: String?): Result<List<UserBadge>> = 
        RemoteErrorMapper.withErrorMapping {
            val targetUserId = userId ?: supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            try {
                // Try using RPC function first
                val resultJson = backendService.rpc(
                    function = "get_user_badges",
                    parameters = mapOf("p_user_id" to targetUserId)
                ).getOrThrow()
                
                val result = json.decodeFromJsonElement(ListSerializer(UserBadgeDto.serializer()), resultJson)
                
                result.map {
                    UserBadge(
                        badgeId = it.badgeId,
                        badgeName = it.badgeName,
                        badgeDescription = it.badgeDescription,
                        badgeIcon = it.badgeIcon,
                        badgeCategory = it.badgeCategory,
                        badgeRarity = it.badgeRarity,
                        earnedAt = it.earnedAt,
                        metadata = it.metadata,
                        imageUrl = it.badgeImageUrl,
                        badgeType = it.badgeType,
                        isPrimary = it.isPrimary ?: false,
                        isFeatured = it.isFeatured ?: false
                    )
                }
            } catch (e: Exception) {
                // Fallback: Query user_badges with join to badges table
                val queryResult = backendService.query(
                    table = "user_badges",
                    filters = mapOf("user_id" to targetUserId),
                    columns = "*, badges!inner(id, name, description, icon, category, rarity, image_url, type)"
                ).getOrThrow()
                
                @Serializable
                data class BadgeInfo(
                    @SerialName("id") val id: String,
                    @SerialName("name") val name: String,
                    @SerialName("description") val description: String,
                    @SerialName("icon") val icon: String,
                    @SerialName("category") val category: String? = null,
                    @SerialName("rarity") val rarity: String? = null,
                    @SerialName("image_url") val imageUrl: String? = null,
                    @SerialName("type") val type: String? = null
                )
                
                @Serializable
                data class UserBadgeWithBadge(
                    @SerialName("badge_id") val badge_id: String,
                    @SerialName("user_id") val user_id: String,
                    @SerialName("earned_at") val earned_at: String? = null,
                    @SerialName("is_primary") val is_primary: Boolean? = null,
                    @SerialName("is_featured") val is_featured: Boolean? = null,
                    @SerialName("badges") val badges: BadgeInfo
                )
                
                queryResult.map { 
                    val userBadge = json.decodeFromJsonElement(UserBadgeWithBadge.serializer(), it)
                    UserBadge(
                        badgeId = userBadge.badges.id,
                        badgeName = userBadge.badges.name,
                        badgeDescription = userBadge.badges.description,
                        badgeIcon = userBadge.badges.icon,
                        badgeCategory = userBadge.badges.category ?: "general",
                        badgeRarity = userBadge.badges.rarity ?: "COMMON",
                        earnedAt = userBadge.earned_at ?: "",
                        metadata = null,
                        imageUrl = userBadge.badges.imageUrl,
                        badgeType = userBadge.badges.type,
                        isPrimary = userBadge.is_primary ?: false,
                        isFeatured = userBadge.is_featured ?: false
                    )
                }
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
            val queryResult = backendService.query(
                table = "badges",
                filters = mapOf("is_available" to true)
            ).getOrThrow()
            
            val result = queryResult.map { json.decodeFromJsonElement(BadgeDto.serializer(), it) }
            
            result.map { dto ->
                Badge(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    icon = dto.icon ?: dto.imageUrl ?: "",
                    category = dto.category ?: "general",
                    rarity = dto.rarity,
                    price = dto.price,
                    type = parseBadgeType(dto.type),
                    badgeRarity = parseBadgeRarity(dto.rarity),
                    imageUrl = dto.imageUrl ?:"",
                    isAvailable = dto.isAvailable
                )
            }
        }
    
    override suspend fun submitPaymentProof(badgeId: String, proof: PaymentProof): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val data = buildJsonObject {
                put("user_id", userId)
                put("badge_id", badgeId)
                put("transaction_id", proof.transactionId)
                put("payment_method", proof.paymentMethod)
                proof.proofImageUrl?.let { put("proof_image_url", it) }
                put("status", "PENDING")
            }
            
            backendService.insert(
                table = "payment_proofs",
                data = data,
                returning = false
            ).getOrThrow()
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
            val updateAllData = buildJsonObject {
                put("is_primary", false)
            }
            backendService.update(
                table = "user_badges",
                filters = mapOf("user_id" to userId),
                data = updateAllData,
                returning = false
            ).getOrThrow()
            
            // Set selected badge as primary
            val updatePrimaryData = buildJsonObject {
                put("is_primary", true)
            }
            backendService.update(
                table = "user_badges",
                filters = mapOf(
                    "user_id" to userId,
                    "badge_id" to badgeId
                ),
                data = updatePrimaryData,
                returning = false
            ).getOrThrow()
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
            val updateAllData = buildJsonObject {
                put("is_featured", false)
            }
            backendService.update(
                table = "user_badges",
                filters = mapOf("user_id" to userId),
                data = updateAllData,
                returning = false
            ).getOrThrow()
            
            // Set selected badges as featured
            if (badgeIds.isNotEmpty()) {
                for (badgeId in badgeIds) {
                    val updateFeaturedData = buildJsonObject {
                        put("is_featured", true)
                    }
                    backendService.update(
                        table = "user_badges",
                        filters = mapOf(
                            "user_id" to userId,
                            "badge_id" to badgeId
                        ),
                        data = updateFeaturedData,
                        returning = false
                    ).getOrThrow()
                }
            }
        }
    
    override suspend fun getPrimaryBadge(): Result<Badge?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Query user_badges joined with badges table for primary badge
            val resultJson = backendService.rpc(
                function = "get_user_badges_with_details",
                parameters = mapOf("p_user_id" to userId)
            ).getOrThrow()
            
            val result = json.decodeFromJsonElement(ListSerializer(BadgeWithUserInfoDto.serializer()), resultJson)
            
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
            val resultJson = backendService.rpc(
                function = "get_user_badges_with_details",
                parameters = mapOf("p_user_id" to userId)
            ).getOrThrow()
            
            val result = json.decodeFromJsonElement(ListSerializer(BadgeWithUserInfoDto.serializer()), resultJson)
            
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
    
    // Admin methods
    override suspend fun getPaymentProofsByStatus(
        status: ireader.domain.models.remote.PaymentProofStatus
    ): Result<List<PaymentProof>> = RemoteErrorMapper.withErrorMapping {
        val queryResult = backendService.query(
            table = "payment_proofs",
            filters = mapOf("status" to status.name)
        ).getOrThrow()
        
        val result = queryResult.map { json.decodeFromJsonElement(PaymentProofDto.serializer(), it) }
        
        result.map { dto ->
            PaymentProof(
                id = dto.id ?: "",
                userId = dto.userId,
                badgeId = dto.badgeId,
                transactionId = dto.transactionId,
                paymentMethod = dto.paymentMethod,
                proofImageUrl = dto.proofImageUrl,
                status = status,
                submittedAt = dto.submittedAt ?: "",
                reviewedAt = dto.reviewedAt,
                reviewedBy = dto.reviewedBy
            )
        }
    }
    
    override suspend fun updatePaymentProofStatus(
        proofId: String,
        status: ireader.domain.models.remote.PaymentProofStatus,
        adminUserId: String
    ): Result<Unit> = RemoteErrorMapper.withErrorMapping {
        // Verify admin permissions (you may want to add a proper admin check)
        val currentUser = supabaseClient.auth.currentUserOrNull()
            ?: throw Exception("User not authenticated")
        
        // Update payment proof status
        val updateData = buildJsonObject {
            put("status", status.name)
            put("reviewed_by", adminUserId)
            put("reviewed_at", java.time.Instant.now().toString())
        }
        backendService.update(
            table = "payment_proofs",
            filters = mapOf("id" to proofId),
            data = updateData,
            returning = false
        ).getOrThrow()
        
        // If approved, grant the badge to the user
        if (status == ireader.domain.models.remote.PaymentProofStatus.APPROVED) {
            // Get the payment proof details
            val proofQueryResult = backendService.query(
                table = "payment_proofs",
                filters = mapOf("id" to proofId)
            ).getOrThrow()
            
            val proofResult = proofQueryResult.firstOrNull()?.let {
                json.decodeFromJsonElement(PaymentProofDto.serializer(), it)
            } ?: throw Exception("Payment proof not found")
            
            // Use RPC function to grant badge (bypasses RLS with SECURITY DEFINER)
            backendService.rpc(
                function = "grant_badge_to_user",
                parameters = mapOf(
                    "p_user_id" to proofResult.userId,
                    "p_badge_id" to proofResult.badgeId
                )
            ).getOrThrow()
        }
    }
}
