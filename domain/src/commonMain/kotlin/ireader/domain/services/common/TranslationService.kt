package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common translation service interface for mass chapter translation.
 * Similar to DownloadService but for translation operations.
 */
interface TranslationService : PlatformService {
    /**
     * Current service state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Translation progress map (chapterId -> progress)
     */
    val translationProgress: StateFlow<Map<Long, TranslationProgress>>
    
    /**
     * Current book being translated (only one book at a time)
     */
    val currentBookId: StateFlow<Long?>
    
    /**
     * Queue chapters for translation
     * @param bookId The book ID (only one book can be translated at a time)
     * @param chapterIds List of chapter IDs to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param engineId Translation engine ID
     * @param bypassWarning Whether to bypass the rate limit warning
     * @param priority If true, add to front of queue (for single chapter in reader)
     */
    suspend fun queueChapters(
        bookId: Long,
        chapterIds: List<Long>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long,
        bypassWarning: Boolean = false,
        priority: Boolean = false
    ): ServiceResult<TranslationQueueResult>
    
    /**
     * Pause translation
     */
    suspend fun pause()
    
    /**
     * Resume translation
     */
    suspend fun resume()
    
    /**
     * Cancel specific chapter translation
     */
    suspend fun cancelTranslation(chapterId: Long): ServiceResult<Unit>
    
    /**
     * Cancel all translations
     */
    suspend fun cancelAll(): ServiceResult<Unit>
    
    /**
     * Retry failed translation
     */
    suspend fun retryTranslation(chapterId: Long): ServiceResult<Unit>
    
    /**
     * Get translation status for a chapter
     */
    fun getTranslationStatus(chapterId: Long): TranslationStatus?
    
    /**
     * Check if engine requires rate limiting (web-based AI engines)
     */
    fun requiresRateLimiting(engineId: Long): Boolean
    
    /**
     * Check if engine is offline/local (no rate limiting needed)
     */
    fun isOfflineEngine(engineId: Long): Boolean
}

/**
 * Translation status enum
 */
enum class TranslationStatus {
    QUEUED,
    DOWNLOADING_CONTENT,  // Downloading chapter content first
    TRANSLATING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    RATE_LIMITED  // Waiting due to rate limiting
}

/**
 * Translation progress data
 */
data class TranslationProgress(
    val chapterId: Long,
    val chapterName: String = "",
    val bookName: String = "",
    val status: TranslationStatus = TranslationStatus.QUEUED,
    val progress: Float = 0f, // 0.0 to 1.0
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val totalRetries: Int = 3,
    val estimatedTimeRemaining: Long? = null, // in milliseconds
    // Chunk-based progress for detailed notification
    val currentChunk: Int = 0,
    val totalChunks: Int = 0,
    val translatedParagraphs: Int = 0,
    val totalParagraphs: Int = 0
)

/**
 * Result of queueing chapters for translation
 */
sealed class TranslationQueueResult {
    data class Success(val queuedCount: Int) : TranslationQueueResult()
    data class RateLimitWarning(
        val chapterCount: Int,
        val estimatedTime: Long, // in milliseconds
        val message: String
    ) : TranslationQueueResult()
    data class PreviousTranslationCancelled(val previousBookId: Long) : TranslationQueueResult()
}

/**
 * Constants for translation service
 */
object TranslationServiceConstants {
    const val TRANSLATION_SERVICE_NAME = "TRANSLATION_SERVICE"
    const val TRANSLATION_CHAPTER_IDS = "chapterIds"
    const val TRANSLATION_BOOK_ID = "bookId"
    const val TRANSLATION_SOURCE_LANG = "sourceLang"
    const val TRANSLATION_TARGET_LANG = "targetLang"
    const val TRANSLATION_ENGINE_ID = "engineId"
    
    // Rate limiting thresholds
    const val RATE_LIMIT_WARNING_THRESHOLD = 10 // chapters
    const val RATE_LIMIT_DELAY_MS = 3000L // 3 seconds between requests
    const val RATE_LIMIT_BURST_SIZE = 5 // requests before enforcing delay
}
