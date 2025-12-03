package ireader.data.repository

import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of CharacterArtRepository.
 * Use this when the character art feature is disabled or not configured.
 */
class NoOpCharacterArtRepository : CharacterArtRepository {
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> = Result.success(emptyList())
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> = 
        Result.success(emptyList())
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> = 
        Result.success(emptyList())
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> = 
        Result.success(emptyList())
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> = 
        Result.success(emptyList())
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> = 
        Result.success(emptyList())
    
    override suspend fun getArtById(id: String): Result<CharacterArt> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun toggleLike(artId: String): Result<Boolean> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> = 
        Result.failure(Exception("Character art feature not available"))
    
    override suspend fun deleteArt(artId: String): Result<Unit> = 
        Result.failure(Exception("Character art feature not available"))
    
    override fun observeArt(filter: ArtStyleFilter): Flow<List<CharacterArt>> = 
        flowOf(emptyList())
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> = 
        Result.failure(Exception("Character art feature not available"))
}
