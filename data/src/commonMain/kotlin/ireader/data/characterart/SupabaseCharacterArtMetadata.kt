package ireader.data.characterart

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.CharacterArtStatus
import ireader.domain.models.characterart.SubmitCharacterArtRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase implementation for character art metadata storage.
 * Images are stored in Cloudflare R2, metadata in Supabase.
 */
class SupabaseCharacterArtMetadata(
    private val supabaseClient: SupabaseClient,
    private val getCurrentUserId: suspend () -> String?
) : MetadataStorageProvider {
    
    private val tableName = "character_art"
    private val likesTable = "character_art_likes"
    private val reportsTable = "character_art_reports"
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> {
        return try {
            val userId = getCurrentUserId()
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("status", CharacterArtStatus.APPROVED.name)
                        if (filter != ArtStyleFilter.ALL) {
                            contains("tags", listOf(filter.name))
                        }
                        if (searchQuery.isNotBlank()) {
                            or {
                                ilike("character_name", "%$searchQuery%")
                                ilike("book_title", "%$searchQuery%")
                            }
                        }
                    }
                    order(getSortColumn(sort), if (sort == CharacterArtSort.OLDEST) Order.ASCENDING else Order.DESCENDING)
                    limit(limit.toLong())
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                .decodeList<CharacterArtDto>()
            
            val artList = result.map { dto ->
                val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                dto.toDomain(isLiked)
            }
            
            Result.success(artList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> {
        return try {
            val userId = getCurrentUserId()
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("status", CharacterArtStatus.APPROVED.name)
                        ilike("book_title", "%$bookTitle%")
                    }
                }
                .decodeList<CharacterArtDto>()
            
            val artList = result.map { dto ->
                val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                dto.toDomain(isLiked)
            }
            
            Result.success(artList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> {
        return try {
            val userId = getCurrentUserId()
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("status", CharacterArtStatus.APPROVED.name)
                        ilike("character_name", "%$characterName%")
                    }
                }
                .decodeList<CharacterArtDto>()
            
            val artList = result.map { dto ->
                val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                dto.toDomain(isLiked)
            }
            
            Result.success(artList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> {
        return try {
            val userId = getCurrentUserId()
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("status", CharacterArtStatus.APPROVED.name)
                        eq("is_featured", true)
                    }
                    order("submitted_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<CharacterArtDto>()
            
            val artList = result.map { dto ->
                val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                dto.toDomain(isLiked)
            }
            
            Result.success(artList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("submitter_id", userId)
                    }
                    order("submitted_at", Order.DESCENDING)
                }
                .decodeList<CharacterArtDto>()
            
            val artList = result.map { dto ->
                val isLiked = checkIfLiked(dto.id, userId)
                dto.toDomain(isLiked)
            }
            
            Result.success(artList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> {
        return try {
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("status", CharacterArtStatus.PENDING.name)
                    }
                    order("submitted_at", Order.ASCENDING)
                }
                .decodeList<CharacterArtDto>()
            
            Result.success(result.map { it.toDomain(false) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> {
        return try {
            val userId = getCurrentUserId()
            
            val result = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<CharacterArtDto>()
            
            val isLiked = userId?.let { checkIfLiked(id, it) } ?: false
            Result.success(result.toDomain(isLiked))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createArt(
        request: SubmitCharacterArtRequest,
        imageUrl: String,
        thumbnailUrl: String?
    ): Result<CharacterArt> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            
            val dto = CharacterArtInsertDto(
                characterName = request.characterName,
                bookTitle = request.bookTitle,
                bookAuthor = request.bookAuthor,
                description = request.description,
                imageUrl = imageUrl,
                thumbnailUrl = thumbnailUrl ?: "",
                submitterId = userId,
                aiModel = request.aiModel,
                prompt = request.prompt,
                tags = request.tags,
                status = CharacterArtStatus.PENDING.name
            )
            
            val result = supabaseClient.postgrest[tableName]
                .insert(dto) {
                    select()
                }
                .decodeSingle<CharacterArtDto>()
            
            Result.success(result.toDomain(false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            
            val isCurrentlyLiked = checkIfLiked(artId, userId)
            
            if (isCurrentlyLiked) {
                // Remove like
                supabaseClient.postgrest[likesTable]
                    .delete {
                        filter {
                            eq("art_id", artId)
                            eq("user_id", userId)
                        }
                    }
                
                // Decrement likes count
                supabaseClient.postgrest.rpc("decrement_art_likes", mapOf("art_id" to artId))
                
                Result.success(false)
            } else {
                // Add like
                supabaseClient.postgrest[likesTable]
                    .insert(mapOf("art_id" to artId, "user_id" to userId))
                
                // Increment likes count
                supabaseClient.postgrest.rpc("increment_art_likes", mapOf("art_id" to artId))
                
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun approveArt(artId: String, featured: Boolean, newImageUrl: String): Result<Unit> {
        return try {
            supabaseClient.postgrest[tableName]
                .update({
                    set("status", CharacterArtStatus.APPROVED.name)
                    set("is_featured", featured)
                    set("image_url", newImageUrl)
                }) {
                    filter {
                        eq("id", artId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> {
        return try {
            supabaseClient.postgrest[tableName]
                .update({
                    set("status", CharacterArtStatus.REJECTED.name)
                    set("rejection_reason", reason)
                }) {
                    filter {
                        eq("id", artId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteArt(artId: String): Result<Unit> {
        return try {
            // Delete likes first
            supabaseClient.postgrest[likesTable]
                .delete {
                    filter {
                        eq("art_id", artId)
                    }
                }
            
            // Delete art
            supabaseClient.postgrest[tableName]
                .delete {
                    filter {
                        eq("id", artId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            
            supabaseClient.postgrest[reportsTable]
                .insert(mapOf(
                    "art_id" to artId,
                    "reporter_id" to userId,
                    "reason" to reason
                ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun checkIfLiked(artId: String, userId: String): Boolean {
        return try {
            val result = supabaseClient.postgrest[likesTable]
                .select {
                    filter {
                        eq("art_id", artId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<Map<String, String>>()
            
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getSortColumn(sort: CharacterArtSort): String {
        return when (sort) {
            CharacterArtSort.NEWEST, CharacterArtSort.OLDEST -> "submitted_at"
            CharacterArtSort.MOST_LIKED -> "likes_count"
            CharacterArtSort.BOOK_TITLE -> "book_title"
            CharacterArtSort.CHARACTER_NAME -> "character_name"
        }
    }
}

// DTOs for Supabase

@Serializable
data class CharacterArtDto(
    val id: String,
    @SerialName("character_name") val characterName: String,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("book_author") val bookAuthor: String = "",
    val description: String = "",
    @SerialName("image_url") val imageUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String = "",
    @SerialName("submitter_id") val submitterId: String,
    @SerialName("submitter_username") val submitterUsername: String = "",
    @SerialName("ai_model") val aiModel: String = "",
    val prompt: String = "",
    @SerialName("likes_count") val likesCount: Int = 0,
    val status: String = "PENDING",
    @SerialName("submitted_at") val submittedAt: Long = 0,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    val tags: List<String> = emptyList()
) {
    fun toDomain(isLikedByUser: Boolean): CharacterArt {
        return CharacterArt(
            id = id,
            characterName = characterName,
            bookTitle = bookTitle,
            bookAuthor = bookAuthor,
            description = description,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl,
            submitterId = submitterId,
            submitterUsername = submitterUsername,
            aiModel = aiModel,
            prompt = prompt,
            likesCount = likesCount,
            isLikedByUser = isLikedByUser,
            status = CharacterArtStatus.valueOf(status),
            submittedAt = submittedAt,
            isFeatured = isFeatured,
            tags = tags
        )
    }
}

@Serializable
data class CharacterArtInsertDto(
    @SerialName("character_name") val characterName: String,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("book_author") val bookAuthor: String = "",
    val description: String = "",
    @SerialName("image_url") val imageUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String = "",
    @SerialName("submitter_id") val submitterId: String,
    @SerialName("ai_model") val aiModel: String = "",
    val prompt: String = "",
    val tags: List<String> = emptyList(),
    val status: String = "PENDING"
)
