package ireader.data.repository

import ireader.data.characterart.ImageStorageProvider
import ireader.data.characterart.MetadataStorageProvider
import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Character Art Repository Implementation.
 * 
 * Architecture:
 * - Images → Cloudflare R2 (free, no egress fees)
 * - Metadata → Supabase PostgreSQL (or any database)
 * 
 * This can be configured with different storage providers:
 * - CloudflareR2ImageStorage + SupabaseCharacterArtMetadata (production)
 * - LocalImageStorage + in-memory (testing/offline)
 * 
 * To integrate with a different database:
 * 1. Implement MetadataStorageProvider for your database
 * 2. Implement ImageStorageProvider for your storage (or use R2)
 * 3. Inject via dependency injection
 */
class CharacterArtRepositoryImpl(
    private val imageStorage: ImageStorageProvider? = null,
    private val metadataStorage: MetadataStorageProvider? = null,
    private val remoteDataSource: CharacterArtRemoteDataSource? = null
) : CharacterArtRepository {
    
    // In-memory cache
    private val artCache = MutableStateFlow<List<CharacterArt>>(emptyList())
    private val likedArtIds = mutableSetOf<String>()
    private var currentUserId: String = ""
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> {
        return try {
            // Try remote first if available
            remoteDataSource?.getApprovedArt(filter, sort, searchQuery, limit, offset)
                ?: Result.success(
                    artCache.value
                        .filter { it.status == CharacterArtStatus.APPROVED }
                        .filter { art ->
                            if (filter == ArtStyleFilter.ALL) true
                            else art.tags.contains(filter.name)
                        }
                        .filter { art ->
                            if (searchQuery.isBlank()) true
                            else art.characterName.contains(searchQuery, ignoreCase = true) ||
                                 art.bookTitle.contains(searchQuery, ignoreCase = true)
                        }
                        .sortedWith(getSortComparator(sort))
                        .drop(offset)
                        .take(limit)
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> {
        return try {
            remoteDataSource?.getArtByBook(bookTitle)
                ?: Result.success(
                    artCache.value.filter { 
                        it.bookTitle.equals(bookTitle, ignoreCase = true) &&
                        it.status == CharacterArtStatus.APPROVED
                    }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> {
        return try {
            remoteDataSource?.getArtByCharacter(characterName)
                ?: Result.success(
                    artCache.value.filter { 
                        it.characterName.equals(characterName, ignoreCase = true) &&
                        it.status == CharacterArtStatus.APPROVED
                    }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> {
        return try {
            remoteDataSource?.getFeaturedArt(limit)
                ?: Result.success(
                    artCache.value
                        .filter { it.isFeatured && it.status == CharacterArtStatus.APPROVED }
                        .take(limit)
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> {
        return try {
            remoteDataSource?.getUserSubmissions()
                ?: Result.success(
                    artCache.value.filter { it.submitterId == currentUserId }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> {
        return try {
            remoteDataSource?.getPendingArt()
                ?: Result.success(
                    artCache.value.filter { it.status == CharacterArtStatus.PENDING }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> {
        return try {
            remoteDataSource?.getArtById(id)
                ?: artCache.value.find { it.id == id }
                    ?.let { Result.success(it) }
                ?: Result.failure(Exception("Art not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> {
        return try {
            remoteDataSource?.submitArt(request)
                ?: run {
                    val newArt = CharacterArt(
                        id = generateId(),
                        characterName = request.characterName,
                        bookTitle = request.bookTitle,
                        bookAuthor = request.bookAuthor,
                        description = request.description,
                        imageUrl = request.imagePath,
                        aiModel = request.aiModel,
                        prompt = request.prompt,
                        tags = request.tags,
                        submitterId = currentUserId,
                        status = CharacterArtStatus.PENDING,
                        submittedAt = currentTimeToLong()
                    )
                    artCache.value = artCache.value + newArt
                    Result.success(newArt)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            remoteDataSource?.uploadImage(imageBytes, fileName)
                ?: Result.success("local://$fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> {
        return try {
            remoteDataSource?.toggleLike(artId)
                ?: run {
                    val isLiked = if (likedArtIds.contains(artId)) {
                        likedArtIds.remove(artId)
                        false
                    } else {
                        likedArtIds.add(artId)
                        true
                    }
                    
                    artCache.value = artCache.value.map { art ->
                        if (art.id == artId) {
                            art.copy(
                                isLikedByUser = isLiked,
                                likesCount = if (isLiked) art.likesCount + 1 else art.likesCount - 1
                            )
                        } else art
                    }
                    
                    Result.success(isLiked)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> {
        return try {
            remoteDataSource?.approveArt(artId, featured)
                ?: run {
                    artCache.value = artCache.value.map { art ->
                        if (art.id == artId) {
                            art.copy(
                                status = CharacterArtStatus.APPROVED,
                                isFeatured = featured
                            )
                        } else art
                    }
                    Result.success(Unit)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> {
        return try {
            remoteDataSource?.rejectArt(artId, reason)
                ?: run {
                    artCache.value = artCache.value.map { art ->
                        if (art.id == artId) {
                            art.copy(status = CharacterArtStatus.REJECTED)
                        } else art
                    }
                    Result.success(Unit)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteArt(artId: String): Result<Unit> {
        return try {
            remoteDataSource?.deleteArt(artId)
                ?: run {
                    artCache.value = artCache.value.filter { it.id != artId }
                    Result.success(Unit)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeArt(filter: ArtStyleFilter): Flow<List<CharacterArt>> {
        return artCache.map { list ->
            list.filter { it.status == CharacterArtStatus.APPROVED }
                .filter { art ->
                    if (filter == ArtStyleFilter.ALL) true
                    else art.tags.contains(filter.name)
                }
        }
    }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> {
        return try {
            remoteDataSource?.reportArt(artId, reason)
                ?: Result.success(Unit) // In-memory just acknowledges
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getSortComparator(sort: CharacterArtSort): Comparator<CharacterArt> {
        return when (sort) {
            CharacterArtSort.NEWEST -> compareByDescending { it.submittedAt }
            CharacterArtSort.OLDEST -> compareBy { it.submittedAt }
            CharacterArtSort.MOST_LIKED -> compareByDescending { it.likesCount }
            CharacterArtSort.BOOK_TITLE -> compareBy { it.bookTitle.lowercase() }
            CharacterArtSort.CHARACTER_NAME -> compareBy { it.characterName.lowercase() }
        }
    }
    
    private fun generateId(): String {
        return "art_${currentTimeToLong()}_${(0..9999).random()}"
    }
    
    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }
}

/**
 * Interface for remote data source.
 * Implement this for Supabase, Firebase, custom API, etc.
 */
interface CharacterArtRemoteDataSource {
    suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>>
    
    suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>>
    suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>>
    suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>>
    suspend fun getUserSubmissions(): Result<List<CharacterArt>>
    suspend fun getPendingArt(): Result<List<CharacterArt>>
    suspend fun getArtById(id: String): Result<CharacterArt>
    suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt>
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String>
    suspend fun toggleLike(artId: String): Result<Boolean>
    suspend fun approveArt(artId: String, featured: Boolean): Result<Unit>
    suspend fun rejectArt(artId: String, reason: String): Result<Unit>
    suspend fun deleteArt(artId: String): Result<Unit>
    suspend fun reportArt(artId: String, reason: String): Result<Unit>
}
