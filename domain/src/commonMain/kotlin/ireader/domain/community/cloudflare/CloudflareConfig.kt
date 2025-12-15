package ireader.domain.community.cloudflare

/**
 * Configuration for Cloudflare D1 + R2 community translation storage.
 * 
 * D1: SQLite database for metadata and translation index
 * R2: Object storage for compressed translation content
 */
data class CloudflareConfig(
    /** Cloudflare Account ID */
    val accountId: String,
    /** API Token with D1 and R2 permissions */
    val apiToken: String,
    /** D1 Database ID for translation metadata */
    val d1DatabaseId: String,
    /** R2 Bucket name for translation content */
    val r2BucketName: String,
    /** R2 Public URL for reading content (optional, for CDN) */
    val r2PublicUrl: String = "",
    /** Enable compression for translations */
    val enableCompression: Boolean = true,
    /** Minimum text length to compress (bytes) */
    val compressionThreshold: Int = 500
) {
    val d1ApiUrl: String
        get() = "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database/$d1DatabaseId"
    
    val r2ApiUrl: String
        get() = "https://api.cloudflare.com/client/v4/accounts/$accountId/r2/buckets/$r2BucketName"
    
    fun isValid(): Boolean = accountId.isNotBlank() && 
        apiToken.isNotBlank() && 
        d1DatabaseId.isNotBlank() && 
        r2BucketName.isNotBlank()
}

/**
 * Translation metadata stored in D1
 */
data class TranslationMetadata(
    val id: String,
    /** Hash of original content for deduplication */
    val contentHash: String,
    /** Book identifier (title + author hash) */
    val bookHash: String,
    val bookTitle: String,
    val bookAuthor: String,
    val chapterName: String,
    val chapterNumber: Float,
    val sourceLanguage: String,
    val targetLanguage: String,
    /** Translation engine used (e.g., "openai", "gemini", "deepseek") */
    val engineId: String,
    /** R2 object key for compressed content */
    val r2ObjectKey: String,
    /** Original content size in bytes */
    val originalSize: Int,
    /** Compressed content size in bytes */
    val compressedSize: Int,
    /** Compression ratio (compressed/original) */
    val compressionRatio: Float,
    val contributorId: String,
    val contributorName: String,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val downloadCount: Long = 0,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Result of checking for existing translation
 */
data class TranslationLookupResult(
    val found: Boolean,
    val metadata: TranslationMetadata? = null,
    val content: String? = null
)
