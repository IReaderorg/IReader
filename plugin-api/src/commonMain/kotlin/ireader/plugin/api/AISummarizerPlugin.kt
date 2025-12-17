package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Dedicated plugin interface for AI-powered text summarization.
 * Provides specialized summarization capabilities for novels and reading content.
 * 
 * Example:
 * ```kotlin
 * class GPTSummarizerPlugin : AISummarizerPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.gpt-summarizer",
 *         name = "GPT Summarizer",
 *         type = PluginType.AI,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.READER_CONTEXT),
 *         // ... other manifest fields
 *     )
 *     
 *     override suspend fun summarizeChapter(
 *         chapterContent: String,
 *         options: ChapterSummaryOptions
 *     ): SummaryResult<ChapterSummary> {
 *         // Call GPT API for summarization
 *     }
 * }
 * ```
 */
interface AISummarizerPlugin : Plugin {
    /**
     * Summarization capabilities.
     */
    val summaryCapabilities: List<SummaryCapability>
    
    /**
     * AI provider configuration.
     */
    val providerConfig: SummaryProviderConfig
    
    /**
     * Check if the summarizer is ready.
     */
    suspend fun isReady(): Boolean
    
    /**
     * Summarize a single chapter.
     */
    suspend fun summarizeChapter(
        chapterContent: String,
        options: ChapterSummaryOptions = ChapterSummaryOptions()
    ): SummaryResult<ChapterSummary>
    
    /**
     * Summarize multiple chapters (book summary).
     */
    suspend fun summarizeBook(
        chapters: List<SummaryChapterContent>,
        options: BookSummaryOptions = BookSummaryOptions()
    ): SummaryResult<BookSummary>
    
    /**
     * Generate a "previously on" recap.
     */
    suspend fun generateRecap(
        previousChapters: List<SummaryChapterContent>,
        currentChapterNumber: Int,
        options: RecapOptions = RecapOptions()
    ): SummaryResult<RecapSummary>
    
    /**
     * Summarize selected text.
     */
    suspend fun summarizeSelection(
        selectedText: String,
        context: String? = null,
        options: SelectionSummaryOptions = SelectionSummaryOptions()
    ): SummaryResult<String>
    
    /**
     * Extract key points from text.
     */
    suspend fun extractKeyPoints(
        text: String,
        maxPoints: Int = 5
    ): SummaryResult<List<KeyPoint>>
    
    /**
     * Generate chapter title suggestion.
     */
    suspend fun suggestChapterTitle(
        chapterContent: String
    ): SummaryResult<List<String>>
    
    /**
     * Detect plot points and events.
     */
    suspend fun detectPlotPoints(
        chapterContent: String
    ): SummaryResult<List<PlotPoint>>
    
    /**
     * Stream summary generation.
     */
    suspend fun streamSummary(
        text: String,
        options: ChapterSummaryOptions = ChapterSummaryOptions(),
        onToken: (String) -> Unit
    ): SummaryResult<String>
    
    /**
     * Cancel ongoing summarization.
     */
    fun cancelSummarization()
    
    /**
     * Get estimated token count for text.
     */
    fun estimateTokens(text: String): Int
    
    /**
     * Get maximum context length.
     */
    fun getMaxContextLength(): Int
}

/**
 * Summarization capabilities.
 */
@Serializable
enum class SummaryCapability {
    /** Single chapter summarization */
    CHAPTER_SUMMARY,
    /** Multi-chapter book summary */
    BOOK_SUMMARY,
    /** "Previously on" recaps */
    RECAP,
    /** Selection summarization */
    SELECTION_SUMMARY,
    /** Key point extraction */
    KEY_POINTS,
    /** Plot point detection */
    PLOT_DETECTION,
    /** Character tracking in summaries */
    CHARACTER_TRACKING,
    /** Streaming output */
    STREAMING,
    /** Multi-language support */
    MULTI_LANGUAGE
}

/**
 * Summary provider configuration.
 */
@Serializable
data class SummaryProviderConfig(
    /** Provider name (OpenAI, Claude, Local, etc.) */
    val providerName: String,
    /** Model name */
    val modelName: String,
    /** Maximum context length in tokens */
    val maxContextLength: Int,
    /** Whether streaming is supported */
    val supportsStreaming: Boolean = true,
    /** Whether API key is required */
    val requiresApiKey: Boolean = true,
    /** Default endpoint (for local models) */
    val defaultEndpoint: String? = null,
    /** Cost per 1K tokens (for display) */
    val costPer1kTokens: Float? = null
)

/**
 * Chapter summary options.
 */
@Serializable
data class ChapterSummaryOptions(
    /** Target summary length */
    val length: SummaryLength = SummaryLength.MEDIUM,
    /** Summary style */
    val style: SummaryStyle = SummaryStyle.NARRATIVE,
    /** Include character mentions */
    val includeCharacters: Boolean = true,
    /** Include key events */
    val includeKeyEvents: Boolean = true,
    /** Output language (null = same as input) */
    val outputLanguage: String? = null,
    /** Focus on specific aspects */
    val focusAspects: List<String> = emptyList(),
    /** Spoiler level */
    val spoilerLevel: SpoilerLevel = SpoilerLevel.FULL
)

@Serializable
enum class SpoilerLevel {
    /** No spoilers - vague summary */
    NONE,
    /** Minor spoilers - general events */
    MINOR,
    /** Full spoilers - complete summary */
    FULL
}

/**
 * Book summary options.
 */
@Serializable
data class BookSummaryOptions(
    /** Target summary length */
    val length: SummaryLength = SummaryLength.LONG,
    /** Include chapter-by-chapter breakdown */
    val includeChapterBreakdown: Boolean = false,
    /** Include character arcs */
    val includeCharacterArcs: Boolean = true,
    /** Include themes analysis */
    val includeThemes: Boolean = false,
    /** Output language */
    val outputLanguage: String? = null
)

/**
 * Recap options.
 */
@Serializable
data class RecapOptions(
    /** Number of previous chapters to consider */
    val chaptersToConsider: Int = 5,
    /** Recap length */
    val length: SummaryLength = SummaryLength.SHORT,
    /** Focus on specific characters */
    val focusCharacters: List<String> = emptyList(),
    /** Include cliffhangers */
    val includeCliffhangers: Boolean = true,
    /** Style (narrative, bullet points, etc.) */
    val style: SummaryStyle = SummaryStyle.NARRATIVE
)

/**
 * Selection summary options.
 */
@Serializable
data class SelectionSummaryOptions(
    /** Summary length */
    val length: SummaryLength = SummaryLength.SHORT,
    /** Include context explanation */
    val includeContext: Boolean = false
)

/**
 * Chapter summary result.
 */
@Serializable
data class ChapterSummary(
    /** Summary text */
    val summary: String,
    /** Characters mentioned */
    val characters: List<String> = emptyList(),
    /** Key events */
    val keyEvents: List<String> = emptyList(),
    /** Mood/tone */
    val mood: String? = null,
    /** Word count of original */
    val originalWordCount: Int,
    /** Word count of summary */
    val summaryWordCount: Int,
    /** Compression ratio */
    val compressionRatio: Float
)

/**
 * Book summary result.
 */
@Serializable
data class BookSummary(
    /** Overall summary */
    val overallSummary: String,
    /** Chapter summaries (if requested) */
    val chapterSummaries: List<ChapterSummary> = emptyList(),
    /** Main characters */
    val mainCharacters: List<CharacterSummary> = emptyList(),
    /** Major plot points */
    val majorPlotPoints: List<String> = emptyList(),
    /** Themes (if requested) */
    val themes: List<String> = emptyList(),
    /** Total chapters summarized */
    val totalChapters: Int
)

/**
 * Character summary in book summary.
 */
@Serializable
data class CharacterSummary(
    val name: String,
    val role: String,
    val description: String,
    val arc: String? = null
)

/**
 * Recap summary result.
 */
@Serializable
data class RecapSummary(
    /** Recap text */
    val recap: String,
    /** Key events to remember */
    val keyEvents: List<String>,
    /** Characters involved */
    val characters: List<String>,
    /** Cliffhangers/unresolved points */
    val cliffhangers: List<String> = emptyList(),
    /** First chapter covered */
    val chaptersCoveredStart: Int,
    /** Last chapter covered */
    val chaptersCoveredEnd: Int
)

/**
 * Key point from text.
 */
@Serializable
data class KeyPoint(
    /** Key point text */
    val point: String,
    /** Importance (1-5) */
    val importance: Int,
    /** Category (plot, character, setting, etc.) */
    val category: String? = null
)

/**
 * Plot point detected in text.
 */
@Serializable
data class PlotPoint(
    /** Plot point description */
    val description: String,
    /** Type (introduction, conflict, resolution, etc.) */
    val type: PlotPointType,
    /** Characters involved */
    val characters: List<String> = emptyList(),
    /** Significance (1-5) */
    val significance: Int
)

@Serializable
enum class PlotPointType {
    INTRODUCTION,
    RISING_ACTION,
    CONFLICT,
    CLIMAX,
    FALLING_ACTION,
    RESOLUTION,
    TWIST,
    FORESHADOWING,
    FLASHBACK,
    CHARACTER_DEVELOPMENT
}

/**
 * Chapter content for summarization.
 * Note: Uses different name to avoid conflict with CatalogPlugin.ChapterContent
 */
@Serializable
data class SummaryChapterContent(
    val chapterNumber: Int,
    val title: String?,
    val content: String
)

/**
 * Result wrapper for summary operations.
 */
sealed class SummaryResult<out T> {
    data class Success<T>(val data: T) : SummaryResult<T>()
    data class Error(val error: SummaryError) : SummaryResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): SummaryResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Summary errors.
 */
@Serializable
sealed class SummaryError {
    data class ModelNotReady(val reason: String) : SummaryError()
    data class ContextTooLong(val maxLength: Int, val actualLength: Int) : SummaryError()
    data class ApiError(val statusCode: Int, val message: String) : SummaryError()
    data class RateLimited(val retryAfterMs: Long?) : SummaryError()
    data class AuthenticationFailed(val reason: String) : SummaryError()
    data class NetworkError(val reason: String) : SummaryError()
    data object Cancelled : SummaryError()
    data class Unknown(val message: String) : SummaryError()
}