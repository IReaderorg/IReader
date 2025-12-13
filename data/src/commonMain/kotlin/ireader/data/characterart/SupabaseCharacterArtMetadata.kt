package ireader.data.characterart

import io.github.jan.supabase.SupabaseClient
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.CharacterArtStatus
import ireader.domain.models.characterart.SubmitCharacterArtRequest
import kotlinx.serialization.json.*

/**
 * Supabase implementation using BackendService for pure HTTP requests.
 * Avoids Kotlin reflection issues on Android.
 * 
 * Note: supabaseClient is kept for potential future use but all queries
 * go through backendService which routes to the correct Supabase project.
 */
class SupabaseCharacterArtMetadata(
    @Suppress("unused") private val supabaseClient: SupabaseClient,
    private val backendService: BackendService,
    private val getCurrentUserId: suspend () -> String?,
    private val getCurrentUsername: suspend () -> String? = { null }
) : MetadataStorageProvider {
    
    private val tableName = "character_art"
    private val likesTable = "character_art_likes"
    private val reportsTable = "character_art_reports"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Parse tags from JSON response - handles both array and string formats
     */
    private fun parseTagsFromJson(jsonElement: JsonElement?): List<String> {
        if (jsonElement == null) return emptyList()
        return when (jsonElement) {
            is JsonArray -> jsonElement.mapNotNull { 
                (it as? JsonPrimitive)?.contentOrNull 
            }
            is JsonPrimitive -> {
                val content = jsonElement.contentOrNull ?: return emptyList()
                // Handle PostgreSQL array format: {tag1,tag2}
                if (content.startsWith("{") && content.endsWith("}")) {
                    content.removeSurrounding("{", "}").split(",").map { it.trim('"') }
                } else {
                    try { json.decodeFromString<List<String>>(content) } catch (e: Exception) { emptyList() }
                }
            }
            else -> emptyList()
        }
    }
    
    /**
     * Convert JSON element to CharacterArt domain model
     */
    private fun JsonElement.toDomain(isLikedByUser: Boolean): CharacterArt {
        val obj = this.jsonObject
        val tagsList = parseTagsFromJson(obj["tags"])
        val statusStr = obj["status"]?.jsonPrimitive?.contentOrNull ?: "PENDING"
        
        return CharacterArt(
            id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
            characterName = obj["character_name"]?.jsonPrimitive?.contentOrNull ?: "",
            bookTitle = obj["book_title"]?.jsonPrimitive?.contentOrNull ?: "",
            bookAuthor = obj["book_author"]?.jsonPrimitive?.contentOrNull ?: "",
            description = obj["description"]?.jsonPrimitive?.contentOrNull ?: "",
            imageUrl = obj["image_url"]?.jsonPrimitive?.contentOrNull ?: "",
            thumbnailUrl = obj["thumbnail_url"]?.jsonPrimitive?.contentOrNull ?: "",
            submitterId = obj["submitter_id"]?.jsonPrimitive?.contentOrNull ?: "",
            submitterUsername = obj["submitter_username"]?.jsonPrimitive?.contentOrNull ?: "",
            aiModel = obj["ai_model"]?.jsonPrimitive?.contentOrNull ?: "",
            prompt = obj["prompt"]?.jsonPrimitive?.contentOrNull ?: "",
            likesCount = obj["likes_count"]?.jsonPrimitive?.intOrNull ?: 0,
            isLikedByUser = isLikedByUser,
            status = try { CharacterArtStatus.valueOf(statusStr) } catch (e: Exception) { CharacterArtStatus.PENDING },
            submittedAt = obj["submitted_at"]?.jsonPrimitive?.longOrNull ?: 0L,
            isFeatured = obj["is_featured"]?.jsonPrimitive?.booleanOrNull ?: false,
            tags = tagsList
        )
    }
    
    /** Get ID from JSON element */
    private fun JsonElement.getId(): String = jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: ""
    
    /** Get book_title from JSON element */
    private fun JsonElement.getBookTitle(): String = jsonObject["book_title"]?.jsonPrimitive?.contentOrNull ?: ""
    
    /** Get character_name from JSON element */
    private fun JsonElement.getCharacterName(): String = jsonObject["character_name"]?.jsonPrimitive?.contentOrNull ?: ""

    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> = RemoteErrorMapper.withErrorMapping {
        val userId = getCurrentUserId()
        
        // First, try to get approved art
        val approvedResult = backendService.query(
            table = tableName,
            filters = mapOf("status" to CharacterArtStatus.APPROVED.name),
            orderBy = getSortColumn(sort),
            ascending = sort == CharacterArtSort.OLDEST,
            limit = limit,
            offset = offset
        ).getOrNull() ?: emptyList()
        
        // Also get user's own pending submissions so they can see their uploads
        val userPendingResult = if (userId != null) {
            backendService.query(
                table = tableName,
                filters = mapOf("submitter_id" to userId),
                orderBy = getSortColumn(sort),
                ascending = sort == CharacterArtSort.OLDEST
            ).getOrNull() ?: emptyList()
        } else {
            emptyList()
        }
        
        // Combine results, removing duplicates (approved art that user also submitted)
        val approvedIds = approvedResult.map { it.getId() }.toSet()
        val combinedResults = approvedResult + userPendingResult.filter { it.getId() !in approvedIds }
        
        var artList = combinedResults.map { jsonElement ->
            val isLiked = userId?.let { checkIfLiked(jsonElement.getId(), it) } ?: false
            jsonElement.toDomain(isLiked)
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
        
        // Sort combined results
        artList = when (sort) {
            CharacterArtSort.NEWEST -> artList.sortedByDescending { it.submittedAt }
            CharacterArtSort.OLDEST -> artList.sortedBy { it.submittedAt }
            CharacterArtSort.MOST_LIKED -> artList.sortedByDescending { it.likesCount }
            CharacterArtSort.BOOK_TITLE -> artList.sortedBy { it.bookTitle.lowercase() }
            CharacterArtSort.CHARACTER_NAME -> artList.sortedBy { it.characterName.lowercase() }
        }
        
        // Apply pagination
        artList.drop(offset).take(limit)
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            val searchLower = bookTitle.lowercase()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("status" to CharacterArtStatus.APPROVED.name)
            ).getOrThrow()
            
            queryResult
                .filter { it.getBookTitle().lowercase().contains(searchLower) }
                .map { jsonElement ->
                    val isLiked = userId?.let { checkIfLiked(jsonElement.getId(), it) } ?: false
                    jsonElement.toDomain(isLiked)
                }
        }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            val searchLower = characterName.lowercase()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("status" to CharacterArtStatus.APPROVED.name)
            ).getOrThrow()
            
            queryResult
                .filter { it.getCharacterName().lowercase().contains(searchLower) }
                .map { jsonElement ->
                    val isLiked = userId?.let { checkIfLiked(jsonElement.getId(), it) } ?: false
                    jsonElement.toDomain(isLiked)
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
                val isLiked = userId?.let { checkIfLiked(jsonElement.getId(), it) } ?: false
                jsonElement.toDomain(isLiked)
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
                val isLiked = checkIfLiked(jsonElement.getId(), userId)
                jsonElement.toDomain(isLiked)
            }
        }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            // Note: This requires admin RLS policy to be set up in Supabase
            // The policy should allow admins to view pending art
            // 
            // If you're seeing empty results, make sure you've run:
            // supabase/migrations/character_art_admin_policies.sql
            
            // First try to get pending art directly
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("status" to CharacterArtStatus.PENDING.name),
                orderBy = "submitted_at",
                ascending = true
            )
            
            val results = queryResult.getOrElse { error ->
                println("CharacterArt: getPendingArt direct query failed - ${error.message}")
                
                // Fallback: Try to get all art and filter in memory
                // This works if the user has access to view all art (e.g., via service role key)
                val allArtResult = backendService.query(
                    table = tableName,
                    orderBy = "submitted_at",
                    ascending = true
                )
                
                allArtResult.getOrElse { fallbackError ->
                    println("CharacterArt: getPendingArt fallback also failed - ${fallbackError.message}")
                    println("CharacterArt: Make sure admin RLS policies are set up in Supabase")
                    emptyList()
                }
            }
            
            // Filter for pending status in case we got all art
            results
                .filter { 
                    val status = it.jsonObject["status"]?.jsonPrimitive?.contentOrNull
                    status == CharacterArtStatus.PENDING.name
                }
                .map { it.toDomain(false) }
        }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId()
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("id" to id)
            ).getOrThrow()
            
            val jsonElement = queryResult.firstOrNull() ?: throw Exception("Art not found")
            val isLiked = userId?.let { checkIfLiked(id, it) } ?: false
            jsonElement.toDomain(isLiked)
        }

    
    override suspend fun createArt(
        request: SubmitCharacterArtRequest,
        imageUrl: String,
        thumbnailUrl: String?
    ): Result<CharacterArt> = RemoteErrorMapper.withErrorMapping {
        val userId = getCurrentUserId() ?: throw Exception("Not logged in")
        val username = getCurrentUsername() ?: "User_${userId.take(8)}"
        
        // Convert tags list to JSON array for PostgreSQL
        val tagsArray = buildJsonArray {
            request.tags.forEach { add(it) }
        }
        
        val data = buildJsonObject {
            put("character_name", request.characterName)
            put("book_title", request.bookTitle)
            put("book_author", request.bookAuthor)
            put("description", request.description)
            put("image_url", imageUrl)
            put("thumbnail_url", thumbnailUrl ?: "")
            put("submitter_id", userId)
            put("submitter_username", username)
            put("ai_model", request.aiModel)
            put("prompt", request.prompt)
            put("tags", tagsArray)
            put("status", CharacterArtStatus.PENDING.name)
        }
        
        val insertResult = backendService.insert(
            table = tableName,
            data = data,
            returning = true
        ).getOrThrow()
        
        insertResult?.toDomain(false) ?: throw Exception("No result returned from insert")
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId() ?: throw Exception("Not logged in")
            val isCurrentlyLiked = checkIfLiked(artId, userId)
            
            if (isCurrentlyLiked) {
                backendService.delete(
                    table = likesTable,
                    filters = mapOf("art_id" to artId, "user_id" to userId)
                ).getOrThrow()
                backendService.rpc("decrement_art_likes", mapOf("art_id" to artId))
                false
            } else {
                val likeData = buildJsonObject {
                    put("art_id", artId)
                    put("user_id", userId)
                }
                backendService.insert(table = likesTable, data = likeData, returning = false).getOrThrow()
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
            backendService.update(table = tableName, filters = mapOf("id" to artId), data = data, returning = false).getOrThrow()
        }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("status", CharacterArtStatus.REJECTED.name)
                put("rejection_reason", reason)
            }
            backendService.update(table = tableName, filters = mapOf("id" to artId), data = data, returning = false).getOrThrow()
        }
    
    override suspend fun deleteArt(artId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            backendService.delete(table = likesTable, filters = mapOf("art_id" to artId))
            backendService.delete(table = tableName, filters = mapOf("id" to artId)).getOrThrow()
        }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = getCurrentUserId() ?: throw Exception("Not logged in")
            val data = buildJsonObject {
                put("art_id", artId)
                put("reporter_id", userId)
                put("reason", reason)
            }
            backendService.insert(table = reportsTable, data = data, returning = false).getOrThrow()
        }
    
    private suspend fun checkIfLiked(artId: String, userId: String): Boolean {
        return try {
            val result = backendService.query(
                table = likesTable,
                filters = mapOf("art_id" to artId, "user_id" to userId)
            ).getOrNull()
            !result.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getSortColumn(sort: CharacterArtSort): String = when (sort) {
        CharacterArtSort.NEWEST, CharacterArtSort.OLDEST -> "submitted_at"
        CharacterArtSort.MOST_LIKED -> "likes_count"
        CharacterArtSort.BOOK_TITLE -> "book_title"
        CharacterArtSort.CHARACTER_NAME -> "character_name"
    }
    
    override suspend fun getPendingArtOlderThan(days: Int): Result<List<CharacterArt>> = 
        RemoteErrorMapper.withErrorMapping {
            val cutoffTime = ireader.domain.utils.extensions.currentTimeToLong() - (days * 24 * 60 * 60 * 1000L)
            
            val queryResult = backendService.query(
                table = tableName,
                filters = mapOf("status" to CharacterArtStatus.PENDING.name),
                orderBy = "submitted_at",
                ascending = true
            ).getOrThrow()
            
            queryResult
                .filter { 
                    val submittedAt = it.jsonObject["submitted_at"]?.jsonPrimitive?.longOrNull ?: Long.MAX_VALUE
                    submittedAt < cutoffTime
                }
                .map { it.toDomain(false) }
        }
    
    override suspend fun autoApproveArt(artId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("status", CharacterArtStatus.APPROVED.name)
                put("is_featured", false)
                put("auto_approved", true)
            }
            backendService.update(table = tableName, filters = mapOf("id" to artId), data = data, returning = false).getOrThrow()
        }
}
