package ireader.data.characterart

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.CharacterArtStatus
import ireader.domain.models.characterart.SubmitCharacterArtRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Supabase implementation using BackendService for pure HTTP requests.
 * Avoids Kotlin reflection issues on Android.
 */
class SupabaseCharacterArtMetadata(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService,
    private val getCurrentUserId: suspend () -> String?
) : MetadataStorageProvider {
    
    private val tableName = "character_art"
    private val likesTable = "character_art_likes"
    private val reportsTable = "character_art_reports"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class CharacterArtDto(
        val id: String = "",
        @SerialName("character_name") val characterName: String = "",
        @SerialName("book_title") val bookTitle: String = "",
        @SerialName("book_author") val bookAuthor: String = "",
        val description: String = "",
        @SerialName("image_url") val imageUrl: String = "",
        @SerialName("thumbnail_url") val thumbnailUrl: String = "",
        @SerialName("submitter_id") val submitterId: String = "",
        @SerialName("submitter_username") val submitterUsername: String = "",
        @SerialName("ai_model") val aiModel: String = "",
        val prompt: String = "",
        @SerialName("likes_count") val likesCount: Int = 0,
        val status: String = "PENDING",
        @SerialName("submitted_at") val submittedAt: Long = 0,
        @SerialName("is_featured") val isFeatured: Boolean = false,
        val tags: String = "[]" // Store as JSON string to avoid reflection
    )

    @Serializable
    private data class CharacterArtInsertDto(
        @SerialName("character_name") val characterName: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("book_author") val bookAuthor: String = "",
        val description: String = "",
        @SerialName("image_url") val imageUrl: String,
        @SerialName("thumbnail_url") val thumbnailUrl: String = "",
        @SerialName("submitter_id") val submitterId: String,
        @SerialName("ai_model") val aiModel: String = "",
        val prompt: String = "",
        val tags: String = "[]", // Store as JSON string
        val status: String = "PENDING"
    )
    
    private fun CharacterArtDto.toDomain(isLikedByUser: Boolean): CharacterArt {
        val tagsList = try {
            json.decodeFromString<List<String>>(tags)
        } catch (e: Exception) {
            emptyList()
        }
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
            status = try { CharacterArtStatus.valueOf(status) } catch (e: Exception) { CharacterArtStatus.PENDING },
            submittedAt = submittedAt,
            isFeatured = isFeatured,
            tags = tagsList
        )
    }
    
    private fun JsonElement.toDto(): CharacterArtDto {
        return json.decodeFromJsonElement(CharacterArtDto.serializer(), this)
    }
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> = RemoteErrorMapper.withErrorMapping {
        val userId = getCurrentUserId()
        
        val filters = mutableMapOf<String, Any>(
            "status" to CharacterArtStatus.APPROVED.name
        )
        
        // Note: Complex filters like tags contains and OR search need raw query
        // For now, we filter in memory for tags and search
        
        val queryResult = backendService.query(
            table = tableName,
            filters = filters,
            orderBy = getSortColumn(sort),
            ascending = sort == CharacterArtSort.OLDEST,
            limit = limit,
            offset = offset
        ).getOrThrow()
        
        var artList = queryResult.map { jsonElement ->
            val dto = jsonElement.toDto()
            val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
            dto.toDomain(isLiked)
        }
        
        // Apply tag filter in memory
        if (filter != ArtStyleFilter.ALL) {
            artList = artList.filter { it.tags.contains(filter.name) }
        }
        
        // Apply search filter in memory
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            artList = artList.filter { 
                it.characterName.lowercase().contains(query) || 
                it.bookTitle.lowercase().contains(query) 
            }
        }
        
        artList
    }

    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf(
                    "status" to CharacterArtStatus.APPROVED.name
                )
            ).getOrThrow()
            
            // Filter by book title in memory (case-insensitive partial match)
            val searchLower = bookTitle.lowercase()
            queryResult
                .map { it.toDto() }
                .filter { it.bookTitle.lowercase().contains(searchLower) }
                .map { dto ->
                    val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                    dto.toDomain(isLiked)
                }
        }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf(
                    "status" to CharacterArtStatus.APPROVED.name
                )
            ).getOrThrow()
            
            // Filter by character name in memory (case-insensitive partial match)
            val searchLower = characterName.lowercase()
            queryResult
                .map { it.toDto() }
                .filter { it.characterName.lowercase().contains(searchLower) }
                .map { dto ->
                    val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                    dto.toDomain(isLiked)
                }
        }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf(
                    "status" to CharacterArtStatus.APPROVED.name,
                    "is_featured" to true
                ),
                orderBy = "submitted_at",
                ascending = false,
                limit = limit
            ).getOrThrow()
            
            queryResult.map { jsonElement ->
                val dto = jsonElement.toDto()
                val isLiked = userId?.let { checkIfLiked(dto.id, it) } ?: false
                dto.toDomain(isLiked)
            }
        }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId() ?: throw Exception("Not logged in")
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("submitter_id" to userId),
                orderBy = "submitted_at",
                ascending = false
            ).getOrThrow()
            
            queryResult.map { jsonElement ->
                val dto = jsonElement.toDto()
                val isLiked = checkIfLiked(dto.id, userId)
                dto.toDomain(isLiked)
            }
        }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("status" to CharacterArtStatus.PENDING.name),
                orderBy = "submitted_at",
                ascending = true
            ).getOrThrow()
            
            queryResult.map { it.toDto().toDomain(false) }
        }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("id" to id)
            ).getOrThrow()
            
            val dto = queryResult.firstOrNull()?.toDto() 
                ?: throw Exception("Art not found")
            val isLiked = userId?.let { checkIfLiked(id, it) } ?: false
            dto.toDomain(isLiked)
        }

    
    override suspend fun createArt(
        request: SubmitCharacterArtRequest,
        imageUrl: String,
        thumbnailUrl: String?
    ): Result<CharacterArt> = RemoteErrorMapper.withErrorMapping {
        val userId = getCurrentUserId() ?: throw Exception("Not logged in")
        
        val tagsJson = json.encodeToString(request.tags)
        
        val data = buildJsonObject {
            put("character_name", request.characterName)
            put("book_title", request.bookTitle)
            put("book_author", request.bookAuthor)
            put("description", request.description)
            put("image_url", imageUrl)
            put("thumbnail_url", thumbnailUrl ?: "")
            put("submitter_id", userId)
            put("ai_model", request.aiModel)
            put("prompt", request.prompt)
            put("tags", tagsJson)
            put("status", CharacterArtStatus.PENDING.name)
        }
        
        val insertResult = backendService.insert(
            table = tableName,
            data = data,
            returning = true
        ).getOrThrow()
        
        val dto = insertResult?.toDto() ?: throw Exception("No result returned from insert")
        dto.toDomain(false)
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId() ?: throw Exception("Not logged in")
            
            val isCurrentlyLiked = checkIfLiked(artId, userId)
            
            if (isCurrentlyLiked) {
                // Remove like
                backendService.delete(
                    table = likesTable,
                    filters = mapOf(
                        "art_id" to artId,
                        "user_id" to userId
                    )
                ).getOrThrow()
                
                // Decrement likes count
                backendService.rpc("decrement_art_likes", mapOf("art_id" to artId))
                
                false
            } else {
                // Add like
                val likeData = buildJsonObject {
                    put("art_id", artId)
                    put("user_id", userId)
                }
                backendService.insert(
                    table = likesTable,
                    data = likeData,
                    returning = false
                ).getOrThrow()
                
                // Increment likes count
                backendService.rpc("increment_art_likes", mapOf("art_id" to artId))
                
                true
            }
        }
    
    override suspend fun approveArt(artId: String, featured: Boolean, newImageUrl: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("status", CharacterArtStatus.APPROVED.name)
                put("is_featured", featured)
                put("image_url", newImageUrl)
            }
            
            backendService.update(
                table = tableName,
                filters = mapOf("id" to artId),
                data = data,
                returning = false
            ).getOrThrow()
        }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("status", CharacterArtStatus.REJECTED.name)
                put("rejection_reason", reason)
            }
            
            backendService.update(
                table = tableName,
                filters = mapOf("id" to artId),
                data = data,
                returning = false
            ).getOrThrow()
        }
    
    override suspend fun deleteArt(artId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            // Delete likes first
            backendService.delete(
                table = likesTable,
                filters = mapOf("art_id" to artId)
            )
            
            // Delete art
            backendService.delete(
                table = tableName,
                filters = mapOf("id" to artId)
            ).getOrThrow()
        }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId() ?: throw Exception("Not logged in")
            
            val data = buildJsonObject {
                put("art_id", artId)
                put("reporter_id", userId)
                put("reason", reason)
            }
            
            backendService.insert(
                table = reportsTable,
                data = data,
                returning = false
            ).getOrThrow()
        }
    
    private suspend fun checkIfLiked(artId: String, userId: String): Boolean {
        return try {
            val result = backendService.query(
                table = likesTable,
                filters = mapOf(
                    "art_id" to artId,
                    "user_id" to userId
                )
            ).getOrNull()
            
            !result.isNullOrEmpty()
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
