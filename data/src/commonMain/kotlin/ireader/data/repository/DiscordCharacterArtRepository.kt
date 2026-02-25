package ireader.data.repository

import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.*
import ireader.domain.services.discord.DiscordWebhookService
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Discord-based character art repository
 * Posts art directly to Discord instead of storing in R2+Supabase
 * 
 * Benefits:
 * - No storage costs (Discord CDN is free)
 * - No moderation system needed (Discord tools)
 * - Immediate community engagement
 * - Simpler architecture
 */
class DiscordCharacterArtRepository(
    private val discordService: DiscordWebhookService
) : CharacterArtRepository {
    
    // Local cache for recently posted art (optional, for UI feedback)
    private val recentlyPosted = mutableListOf<CharacterArt>()
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> {
        // Discord doesn't have a gallery - art is in Discord channel
        // Return empty list or recently posted for UI feedback
        return Result.success(recentlyPosted.take(limit))
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> {
        // Not applicable for Discord - users browse in Discord channel
        return Result.success(emptyList())
    }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> {
        // Not applicable for Discord
        return Result.success(emptyList())
    }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> {
        // Not applicable for Discord
        return Result.success(emptyList())
    }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> {
        // Return recently posted by this user
        return Result.success(recentlyPosted)
    }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> {
        // No approval system with Discord
        return Result.success(emptyList())
    }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> {
        // Find in recent cache
        val art = recentlyPosted.firstOrNull { it.id == id }
        return if (art != null) {
            Result.success(art)
        } else {
            Result.failure(Exception("Art not found in recent posts"))
        }
    }
    
    /**
     * Submit art - posts directly to Discord
     */
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> {
        val imageBytes = request.imageBytes 
            ?: return Result.failure(Exception("No image data provided"))
        
        // Post to Discord
        return discordService.postCharacterArt(
            imageBytes = imageBytes,
            characterName = request.characterName,
            bookTitle = request.bookTitle,
            bookAuthor = request.bookAuthor,
            aiModel = request.aiModel,
            prompt = request.prompt
        ).map {
            // Create CharacterArt object for UI feedback
            val art = CharacterArt(
                id = currentTimeToLong().toString(),
                characterName = request.characterName,
                bookTitle = request.bookTitle,
                bookAuthor = request.bookAuthor,
                imageUrl = "Posted to Discord", // Discord handles the URL
                thumbnailUrl = "",
                description = request.description,
                tags = request.tags,
                aiModel = request.aiModel,
                prompt = request.prompt,
                submittedAt = currentTimeToLong(),
                isFeatured = false,
                likesCount = 0,
                isLikedByUser = false
            )
            
            // Add to recent cache
            recentlyPosted.add(0, art)
            if (recentlyPosted.size > 50) {
                recentlyPosted.removeLast()
            }
            
            art
        }
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> {
        // Not applicable - users react in Discord
        return Result.success(false)
    }
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> {
        // No approval system - art posts directly to Discord
        return Result.success(Unit)
    }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> {
        // No rejection system - moderation happens in Discord
        return Result.success(Unit)
    }
    
    override suspend fun deleteArt(artId: String): Result<Unit> {
        // Remove from local cache only
        recentlyPosted.removeAll { it.id == artId }
        return Result.success(Unit)
    }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> {
        // Not applicable - users report in Discord
        return Result.success(Unit)
    }
    
    override suspend fun autoApproveArt(artId: String): Result<Unit> {
        // Not applicable
        return Result.success(Unit)
    }
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        // Discord handles image upload via webhook
        return Result.success("Discord CDN")
    }
    
    override suspend fun getPendingArtOlderThan(days: Int): Result<List<CharacterArt>> {
        // No approval system with Discord
        return Result.success(emptyList())
    }
    
    override fun observeArt(filter: ArtStyleFilter): kotlinx.coroutines.flow.Flow<List<CharacterArt>> {
        // Return flow of recently posted art
        return kotlinx.coroutines.flow.flowOf(recentlyPosted)
    }
}
