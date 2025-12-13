package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.*
import kotlinx.coroutines.flow.Flow

/**
 * No-op implementation of CharacterArtRepository.
 * Use this when the character art feature is disabled or not configured.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpCharacterArtRepository : NoOpRepositoryBase(), CharacterArtRepository {
    
    private const val FEATURE_NAME = "Character art"
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> = emptyListResult()
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun getArtById(id: String): Result<CharacterArt> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun toggleLike(artId: String): Result<Boolean> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun deleteArt(artId: String): Result<Unit> =
        featureNotAvailable(FEATURE_NAME)
    
    override fun observeArt(filter: ArtStyleFilter): Flow<List<CharacterArt>> =
        emptyListFlow()
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> =
        featureNotAvailable(FEATURE_NAME)
    
    override suspend fun getPendingArtOlderThan(days: Int): Result<List<CharacterArt>> =
        emptyListResult()
    
    override suspend fun autoApproveArt(artId: String): Result<Unit> =
        featureNotAvailable(FEATURE_NAME)
}
