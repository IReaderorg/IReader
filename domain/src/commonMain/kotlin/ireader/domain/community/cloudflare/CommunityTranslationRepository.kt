package ireader.domain.community.cloudflare

import ireader.core.log.Log
import ireader.core.util.randomUUID
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Repository for community translation storage using Cloudflare D1 + R2.
 * 
 * Features:
 * - Deduplication via content hash
 * - Text compression for storage optimization
 * - Metadata in D1 (fast queries)
 * - Content in R2 (cheap storage)
 */
class CommunityTranslationRepository(
    private val d1Client: CloudflareD1Client,
    private val r2Client: CloudflareR2Client,
    private val config: CloudflareConfig
) {
    
    /**
     * Initialize the repository (create schema if needed).
     */
    suspend fun initialize(): Result<Unit> {
        return d1Client.initializeSchema()
    }
    
    /**
     * Check if a translation already exists for the given content.
     * Uses content hash for deduplication.
     */
    suspend fun findExistingTranslation(
        originalContent: String,
        targetLanguage: String,
        engineId: String
    ): TranslationLookupResult {
        val contentHash = TextCompressor.contentHash(originalContent)
        
        val metadata = d1Client.findByContentHash(contentHash, targetLanguage, engineId)
        
        if (metadata != null) {
            // Found existing translation, fetch content
            val contentResult = r2Client.downloadTranslation(metadata.r2ObjectKey)
            
            return if (contentResult.isSuccess) {
                val compressedContent = contentResult.getOrNull()!!
                val decompressedContent = TextCompressor.decompress(compressedContent)
                
                // Increment download count
                d1Client.incrementDownloadCount(metadata.id)
                
                TranslationLookupResult(
                    found = true,
                    metadata = metadata,
                    content = decompressedContent
                )
            } else {
                // Metadata exists but content missing - return not found
                Log.warn { "Translation metadata exists but R2 content missing: ${metadata.r2ObjectKey}" }
                TranslationLookupResult(found = false)
            }
        }
        
        return TranslationLookupResult(found = false)
    }
    
    /**
     * Find translation by book and chapter.
     */
    suspend fun findByBookChapter(
        bookTitle: String,
        bookAuthor: String,
        chapterNumber: Float,
        targetLanguage: String
    ): TranslationLookupResult {
        val bookHash = TextCompressor.bookHash(bookTitle, bookAuthor)
        
        val translations = d1Client.findByBookChapter(bookHash, chapterNumber, targetLanguage)
        
        if (translations.isNotEmpty()) {
            // Get the best rated translation
            val best = translations.first()
            val contentResult = r2Client.downloadTranslation(best.r2ObjectKey)
            
            return if (contentResult.isSuccess) {
                val compressedContent = contentResult.getOrNull()!!
                val decompressedContent = TextCompressor.decompress(compressedContent)
                
                d1Client.incrementDownloadCount(best.id)
                
                TranslationLookupResult(
                    found = true,
                    metadata = best,
                    content = decompressedContent
                )
            } else {
                TranslationLookupResult(found = false)
            }
        }
        
        return TranslationLookupResult(found = false)
    }
    
    /**
     * Submit a new translation to the community.
     * 
     * @param originalContent Original text (for hash calculation)
     * @param translatedContent Translated text to store
     * @param bookTitle Book title
     * @param bookAuthor Book author
     * @param chapterName Chapter name
     * @param chapterNumber Chapter number
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param engineId Translation engine ID (e.g., "openai", "gemini")
     * @param contributorId User ID of contributor
     * @param contributorName Display name of contributor
     * @return Result with translation ID or error
     */
    suspend fun submitTranslation(
        originalContent: String,
        translatedContent: String,
        bookTitle: String,
        bookAuthor: String,
        chapterName: String,
        chapterNumber: Float,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: String,
        contributorId: String,
        contributorName: String
    ): Result<String> {
        // Check for duplicate first
        val contentHash = TextCompressor.contentHash(originalContent)
        val existing = d1Client.findByContentHash(contentHash, targetLanguage, engineId)
        
        if (existing != null) {
            Log.info { "Translation already exists for content hash: $contentHash" }
            return Result.success(existing.id)
        }
        
        // Compress the translated content
        val originalBytes = translatedContent.encodeToByteArray()
        val compressedBytes = if (config.enableCompression && originalBytes.size >= config.compressionThreshold) {
            TextCompressor.compress(translatedContent)
        } else {
            originalBytes
        }
        
        val compressionRatio = compressedBytes.size.toFloat() / originalBytes.size.toFloat()
        val bookHash = TextCompressor.bookHash(bookTitle, bookAuthor)
        
        // Upload to R2
        val uploadResult = r2Client.uploadTranslation(
            compressedContent = compressedBytes,
            bookHash = bookHash,
            chapterNumber = chapterNumber,
            targetLanguage = targetLanguage,
            engineId = engineId
        )
        
        if (uploadResult.isFailure) {
            return Result.failure(uploadResult.exceptionOrNull() ?: Exception("R2 upload failed"))
        }
        
        val r2ObjectKey = uploadResult.getOrNull()!!
        val translationId = randomUUID()
        val now = currentTimeToLong()
        
        // Create metadata
        val metadata = TranslationMetadata(
            id = translationId,
            contentHash = contentHash,
            bookHash = bookHash,
            bookTitle = bookTitle,
            bookAuthor = bookAuthor,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            engineId = engineId,
            r2ObjectKey = r2ObjectKey,
            originalSize = originalBytes.size,
            compressedSize = compressedBytes.size,
            compressionRatio = compressionRatio,
            contributorId = contributorId,
            contributorName = contributorName,
            createdAt = now,
            updatedAt = now
        )
        
        // Insert metadata to D1
        val insertResult = d1Client.insertTranslation(metadata)
        
        if (insertResult.isFailure) {
            // Cleanup R2 object on failure
            r2Client.deleteTranslation(r2ObjectKey)
            return Result.failure(insertResult.exceptionOrNull() ?: Exception("D1 insert failed"))
        }
        
        Log.info { "Translation submitted: $translationId (${originalBytes.size} -> ${compressedBytes.size} bytes, ratio: $compressionRatio)" }
        
        return Result.success(translationId)
    }
    
    /**
     * Get all translations for a book.
     */
    suspend fun getBookTranslations(
        bookTitle: String,
        bookAuthor: String,
        targetLanguage: String? = null
    ): List<TranslationMetadata> {
        val bookHash = TextCompressor.bookHash(bookTitle, bookAuthor)
        return d1Client.findByBook(bookHash, targetLanguage)
    }
    
    /**
     * Get translation content by ID.
     */
    suspend fun getTranslationContent(metadata: TranslationMetadata): Result<String> {
        val contentResult = r2Client.downloadTranslation(metadata.r2ObjectKey)
        
        return if (contentResult.isSuccess) {
            val compressedContent = contentResult.getOrNull()!!
            val decompressedContent = TextCompressor.decompress(compressedContent)
            d1Client.incrementDownloadCount(metadata.id)
            Result.success(decompressedContent)
        } else {
            Result.failure(contentResult.exceptionOrNull() ?: Exception("Failed to download content"))
        }
    }
    
    /**
     * Rate a translation.
     */
    suspend fun rateTranslation(translationId: String, rating: Int): Result<Unit> {
        if (rating < 1 || rating > 5) {
            return Result.failure(Exception("Rating must be between 1 and 5"))
        }
        
        // For simplicity, we just update the rating directly
        // In production, you'd want to track individual user ratings
        return d1Client.updateRating(translationId, rating.toFloat(), 1)
    }
    
    /**
     * Search translations by book title.
     */
    suspend fun searchTranslations(
        query: String,
        targetLanguage: String? = null,
        limit: Int = 50
    ): List<TranslationMetadata> {
        return d1Client.searchByTitle(query, targetLanguage, limit)
    }
    
    /**
     * Get popular translations.
     */
    suspend fun getPopularTranslations(
        targetLanguage: String,
        limit: Int = 50
    ): List<TranslationMetadata> {
        return d1Client.getPopularTranslations(targetLanguage, limit)
    }
    
    /**
     * Calculate storage statistics.
     */
    fun calculateStorageSaved(metadata: TranslationMetadata): StorageStats {
        val saved = metadata.originalSize - metadata.compressedSize
        val percentage = ((1 - metadata.compressionRatio) * 100).toInt()
        return StorageStats(
            originalSize = metadata.originalSize,
            compressedSize = metadata.compressedSize,
            savedBytes = saved,
            savedPercentage = percentage
        )
    }
}

/**
 * Storage statistics for a translation.
 */
data class StorageStats(
    val originalSize: Int,
    val compressedSize: Int,
    val savedBytes: Int,
    val savedPercentage: Int
)
