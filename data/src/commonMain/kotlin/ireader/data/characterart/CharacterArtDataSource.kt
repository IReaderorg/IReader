package ireader.data.characterart

import ireader.data.repository.CharacterArtRemoteDataSource
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.SubmitCharacterArtRequest
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Combined data source for character art.
 * 
 * Architecture:
 * - Image files → Cloudflare R2 (or other object storage)
 * - Metadata → Your database (Supabase, Firebase, local SQLite, etc.)
 * 
 * This separation allows:
 * - Cheap/free image hosting with R2
 * - Flexible metadata storage with any database
 * - Easy migration between storage providers
 */
class CharacterArtDataSource(
    private val imageStorage: ImageStorageProvider,
    private val metadataStorage: MetadataStorageProvider
) : CharacterArtRemoteDataSource {
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> {
        return metadataStorage.getApprovedArt(filter, sort, searchQuery, limit, offset)
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> {
        return metadataStorage.getArtByBook(bookTitle)
    }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> {
        return metadataStorage.getArtByCharacter(characterName)
    }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> {
        return metadataStorage.getFeaturedArt(limit)
    }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> {
        return metadataStorage.getUserSubmissions()
    }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> {
        return metadataStorage.getPendingArt()
    }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> {
        return metadataStorage.getArtById(id)
    }
    
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> {
        // 1. Upload image to R2
        val imageBytes = request.imageBytes ?: return Result.failure(Exception("No image data"))
        val fileName = "art_${currentTimeToLong()}.jpg"
        
        val imageUrlResult = imageStorage.uploadImage(imageBytes, fileName)
        if (imageUrlResult.isFailure) {
            return Result.failure(imageUrlResult.exceptionOrNull() ?: Exception("Upload failed"))
        }
        
        val imageUrl = imageUrlResult.getOrThrow()
        
        // 2. Generate thumbnail (optional)
        val thumbnailUrl = try {
            val thumbnail = imageStorage.generateThumbnail(imageBytes, 300, 300)
            val thumbFileName = "thumb_$fileName"
            imageStorage.uploadImage(thumbnail, thumbFileName).getOrNull()
        } catch (e: Exception) {
            null // Thumbnail generation failed, continue without it
        }
        
        // 3. Save metadata to database
        return metadataStorage.createArt(
            request = request,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl
        )
    }
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return imageStorage.uploadImage(imageBytes, fileName)
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> {
        return metadataStorage.toggleLike(artId)
    }
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> {
        // 1. Get current art to find image path
        val artResult = metadataStorage.getArtById(artId)
        if (artResult.isFailure) return Result.failure(artResult.exceptionOrNull()!!)
        
        val art = artResult.getOrThrow()
        
        // 2. Move image from pending to approved folder (optional)
        val newImageUrl = imageStorage.moveToApproved(art.imageUrl)
        
        // 3. Update metadata
        return metadataStorage.approveArt(artId, featured, newImageUrl)
    }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> {
        // 1. Get art to find image path
        val artResult = metadataStorage.getArtById(artId)
        if (artResult.isFailure) return Result.failure(artResult.exceptionOrNull()!!)
        
        val art = artResult.getOrThrow()
        
        // 2. Delete image from storage
        imageStorage.deleteImage(art.imageUrl)
        art.thumbnailUrl.takeIf { it.isNotBlank() }?.let { imageStorage.deleteImage(it) }
        
        // 3. Update metadata
        return metadataStorage.rejectArt(artId, reason)
    }
    
    override suspend fun deleteArt(artId: String): Result<Unit> {
        // 1. Get art to find image path
        val artResult = metadataStorage.getArtById(artId)
        if (artResult.isFailure) return Result.failure(artResult.exceptionOrNull()!!)
        
        val art = artResult.getOrThrow()
        
        // 2. Delete images
        imageStorage.deleteImage(art.imageUrl)
        art.thumbnailUrl.takeIf { it.isNotBlank() }?.let { imageStorage.deleteImage(it) }
        
        // 3. Delete metadata
        return metadataStorage.deleteArt(artId)
    }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> {
        return metadataStorage.reportArt(artId, reason)
    }
}

/**
 * Interface for image storage providers (R2, S3, Firebase Storage, local, etc.)
 */
interface ImageStorageProvider {
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String>
    suspend fun deleteImage(imageUrl: String): Result<Unit>
    suspend fun moveToApproved(pendingUrl: String): String
    suspend fun generateThumbnail(imageBytes: ByteArray, width: Int, height: Int): ByteArray
}

/**
 * Interface for metadata storage providers (Supabase, Firebase, SQLite, etc.)
 */
interface MetadataStorageProvider {
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
    
    suspend fun createArt(
        request: SubmitCharacterArtRequest,
        imageUrl: String,
        thumbnailUrl: String?
    ): Result<CharacterArt>
    
    suspend fun toggleLike(artId: String): Result<Boolean>
    suspend fun approveArt(artId: String, featured: Boolean, newImageUrl: String): Result<Unit>
    suspend fun rejectArt(artId: String, reason: String): Result<Unit>
    suspend fun deleteArt(artId: String): Result<Unit>
    suspend fun reportArt(artId: String, reason: String): Result<Unit>
    
    /**
     * Get pending art older than specified days for auto-approval
     */
    suspend fun getPendingArtOlderThan(days: Int): Result<List<CharacterArt>>
    
    /**
     * Auto-approve art (marks as auto-approved)
     */
    suspend fun autoApproveArt(artId: String): Result<Unit>
}

/**
 * Cloudflare R2 implementation of ImageStorageProvider
 */
class CloudflareR2ImageStorage(
    private val r2DataSource: CloudflareR2DataSource
) : ImageStorageProvider {
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return r2DataSource.uploadImage(imageBytes, fileName)
    }
    
    override suspend fun deleteImage(imageUrl: String): Result<Unit> {
        val objectKey = extractObjectKey(imageUrl)
        return r2DataSource.deleteImage(objectKey)
    }
    
    override suspend fun moveToApproved(pendingUrl: String): String {
        val pendingKey = extractObjectKey(pendingUrl)
        val approvedKey = pendingKey.replace("pending/", "approved/")
        
        return r2DataSource.approveImage(pendingKey, approvedKey)
            .getOrDefault(pendingUrl)
    }
    
    override suspend fun generateThumbnail(imageBytes: ByteArray, width: Int, height: Int): ByteArray {
        // Basic thumbnail generation - platform-specific implementations can override
        // For now, return original (actual implementation would resize)
        return imageBytes
    }
    
    private fun extractObjectKey(url: String): String {
        // Extract object key from URL like https://pub-xxx.r2.dev/character-art/pending/123.jpg
        return url.substringAfter(".r2.dev/").substringAfter(".com/")
    }
}

/**
 * Local file storage implementation (for offline/testing)
 */
class LocalImageStorage(
    private val basePath: String,
    private val fileSystem: okio.FileSystem = okio.FileSystem.SYSTEM
) : ImageStorageProvider {
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val filePath = "$basePath/pending/$fileName".toOkioPath()
            filePath.parent?.let { parent ->
                fileSystem.createDirectories(parent)
            }
            fileSystem.write(filePath) {
                write(imageBytes)
            }
            Result.success("file://$filePath")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val path = imageUrl.removePrefix("file://").toOkioPath()
            fileSystem.delete(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun moveToApproved(pendingUrl: String): String {
        val pendingPath = pendingUrl.removePrefix("file://")
        val approvedPath = pendingPath.replace("/pending/", "/approved/")
        
        try {
            val sourcePath = pendingPath.toOkioPath()
            val destPath = approvedPath.toOkioPath()
            destPath.parent?.let { parent ->
                fileSystem.createDirectories(parent)
            }
            fileSystem.copy(sourcePath, destPath)
            fileSystem.delete(sourcePath)
            return "file://$approvedPath"
        } catch (e: Exception) {
            return pendingUrl
        }
    }
    
    override suspend fun generateThumbnail(imageBytes: ByteArray, width: Int, height: Int): ByteArray {
        return imageBytes // Simplified - actual implementation would resize
    }
    
    private fun String.toOkioPath(): okio.Path {
        return with(okio.Path.Companion) { this@toOkioPath.toPath() }
    }
}
