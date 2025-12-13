package ireader.domain.data.repository

import ireader.domain.models.characterart.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for character art operations.
 * Database-agnostic - can be implemented with Supabase, Firebase, local DB, etc.
 */
interface CharacterArtRepository {
    
    /**
     * Get all approved character art with optional filtering
     */
    suspend fun getApprovedArt(
        filter: ArtStyleFilter = ArtStyleFilter.ALL,
        sort: CharacterArtSort = CharacterArtSort.NEWEST,
        searchQuery: String = "",
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<CharacterArt>>
    
    /**
     * Get character art by book title
     */
    suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>>
    
    /**
     * Get character art by character name
     */
    suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>>
    
    /**
     * Get featured character art
     */
    suspend fun getFeaturedArt(limit: Int = 10): Result<List<CharacterArt>>
    
    /**
     * Get art submitted by current user
     */
    suspend fun getUserSubmissions(): Result<List<CharacterArt>>
    
    /**
     * Get pending art for admin review
     */
    suspend fun getPendingArt(): Result<List<CharacterArt>>
    
    /**
     * Get single art by ID
     */
    suspend fun getArtById(id: String): Result<CharacterArt>
    
    /**
     * Submit new character art
     */
    suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt>
    
    /**
     * Upload image and get URL
     */
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String>
    
    /**
     * Toggle like on character art
     */
    suspend fun toggleLike(artId: String): Result<Boolean>
    
    /**
     * Approve character art (admin only)
     */
    suspend fun approveArt(artId: String, featured: Boolean = false): Result<Unit>
    
    /**
     * Reject character art (admin only)
     */
    suspend fun rejectArt(artId: String, reason: String = ""): Result<Unit>
    
    /**
     * Delete character art
     */
    suspend fun deleteArt(artId: String): Result<Unit>
    
    /**
     * Observe art updates as Flow
     */
    fun observeArt(filter: ArtStyleFilter = ArtStyleFilter.ALL): Flow<List<CharacterArt>>
    
    /**
     * Report inappropriate art
     */
    suspend fun reportArt(artId: String, reason: String): Result<Unit>
    
    /**
     * Get pending art older than specified days (for auto-approval)
     */
    suspend fun getPendingArtOlderThan(days: Int): Result<List<CharacterArt>>
    
    /**
     * Auto-approve character art (admin only)
     */
    suspend fun autoApproveArt(artId: String): Result<Unit>
}
