package ireader.data.admin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.AdminUserRepository
import ireader.domain.models.remote.AdminUser
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.BadgeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AdminUserRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : AdminUserRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class UserDto(
        val id: String,
        val email: String? = null,
        val username: String? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("is_admin") val isAdmin: Boolean? = null,
        @SerialName("is_supporter") val isSupporter: Boolean? = null
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
    
    override suspend fun getAllUsers(
        limit: Int,
        offset: Int,
        searchQuery: String?
    ): Result<List<AdminUser>> = RemoteErrorMapper.withErrorMapping {
        // Verify admin status first
        val isAdmin = isCurrentUserAdmin().getOrThrow()
        if (!isAdmin) {
            throw Exception("Unauthorized: Admin access required")
        }
        
        val resultJson = backendService.rpc(
            function = "admin_get_all_users",
            parameters = buildMap {
                put("p_limit", limit)
                put("p_offset", offset)
                searchQuery?.let { put("p_search", it) }
            }
        ).getOrThrow()
        
        val users = json.decodeFromJsonElement(ListSerializer(UserDto.serializer()), resultJson)
        
        users.map { dto ->
            AdminUser(
                id = dto.id,
                email = dto.email ?: "",
                username = dto.username,
                createdAt = dto.createdAt ?: "",
                isAdmin = dto.isAdmin ?: false,
                isSupporter = dto.isSupporter ?: false
            )
        }
    }
    
    override suspend fun getUserById(userId: String): Result<AdminUser?> = 
        RemoteErrorMapper.withErrorMapping {
            val isAdmin = isCurrentUserAdmin().getOrThrow()
            if (!isAdmin) {
                throw Exception("Unauthorized: Admin access required")
            }
            
            val resultJson = backendService.rpc(
                function = "admin_get_user_by_id",
                parameters = mapOf("p_user_id" to userId)
            ).getOrThrow()
            
            val users = json.decodeFromJsonElement(ListSerializer(UserDto.serializer()), resultJson)
            
            users.firstOrNull()?.let { dto ->
                AdminUser(
                    id = dto.id,
                    email = dto.email ?: "",
                    username = dto.username,
                    createdAt = dto.createdAt ?: "",
                    isAdmin = dto.isAdmin ?: false,
                    isSupporter = dto.isSupporter ?: false
                )
            }
        }
    
    override suspend fun assignBadgeToUser(userId: String, badgeId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val isAdmin = isCurrentUserAdmin().getOrThrow()
            if (!isAdmin) {
                throw Exception("Unauthorized: Admin access required")
            }
            
            backendService.rpc(
                function = "admin_assign_badge_to_user",
                parameters = mapOf(
                    "p_user_id" to userId,
                    "p_badge_id" to badgeId
                )
            ).getOrThrow()
        }
    
    override suspend fun removeBadgeFromUser(userId: String, badgeId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val isAdmin = isCurrentUserAdmin().getOrThrow()
            if (!isAdmin) {
                throw Exception("Unauthorized: Admin access required")
            }
            
            backendService.rpc(
                function = "admin_remove_badge_from_user",
                parameters = mapOf(
                    "p_user_id" to userId,
                    "p_badge_id" to badgeId
                )
            ).getOrThrow()
        }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val isAdmin = isCurrentUserAdmin().getOrThrow()
            if (!isAdmin) {
                throw Exception("Unauthorized: Admin access required")
            }
            
            // Use Supabase Auth admin API to send password reset
            backendService.rpc(
                function = "admin_send_password_reset",
                parameters = mapOf("p_email" to email)
            ).getOrThrow()
        }
    
    override suspend fun getAvailableBadgesForAssignment(): Result<List<Badge>> = 
        RemoteErrorMapper.withErrorMapping {
            val queryResult = backendService.query(
                table = "badges",
                columns = "*"
            ).getOrThrow()
            
            val badges = queryResult.map { json.decodeFromJsonElement(BadgeDto.serializer(), it) }
            
            badges.map { dto ->
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
                    imageUrl = dto.imageUrl ?: "",
                    isAvailable = dto.isAvailable
                )
            }
        }
    
    override suspend fun getUserBadges(userId: String): Result<List<Badge>> = 
        RemoteErrorMapper.withErrorMapping {
            val resultJson = backendService.rpc(
                function = "get_user_badges_with_details",
                parameters = mapOf("p_user_id" to userId)
            ).getOrThrow()
            
            @Serializable
            data class UserBadgeDto(
                @SerialName("badge_id") val badgeId: String,
                @SerialName("badge_name") val badgeName: String,
                @SerialName("badge_description") val badgeDescription: String,
                @SerialName("badge_icon") val badgeIcon: String,
                @SerialName("badge_image_url") val imageUrl: String? = null,
                @SerialName("badge_type") val type: String? = null,
                @SerialName("badge_rarity") val rarity: String? = null
            )
            
            val badges = json.decodeFromJsonElement(ListSerializer(UserBadgeDto.serializer()), resultJson)
            
            badges.map { dto ->
                Badge(
                    id = dto.badgeId,
                    name = dto.badgeName,
                    description = dto.badgeDescription,
                    icon = dto.badgeIcon,
                    category = "general",
                    rarity = dto.rarity ?: "COMMON",
                    type = parseBadgeType(dto.type ?: "ACHIEVEMENT"),
                    badgeRarity = parseBadgeRarity(dto.rarity ?: "COMMON"),
                    imageUrl = dto.imageUrl ?: dto.badgeIcon
                )
            }
        }
    
    override suspend fun isCurrentUserAdmin(): Result<Boolean> = 
        RemoteErrorMapper.withErrorMapping {
            val currentUser = supabaseClient.auth.currentUserOrNull()
                ?: return@withErrorMapping false
            
            val resultJson = backendService.rpc(
                function = "is_user_admin",
                parameters = mapOf("p_user_id" to currentUser.id)
            ).getOrThrow()
            
            resultJson.jsonPrimitive.content.toBoolean()
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
