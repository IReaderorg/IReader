package ireader.plugin.api.source

import kotlinx.serialization.Serializable

/**
 * Unified source interface for all content sources in IReader.
 * 
 * This interface provides a common API for:
 * - Tachiyomi/Mihon manga sources
 * - LNReader novel sources
 * - Built-in IReader sources
 * - User-defined sources
 * 
 * All sources can be displayed in the same UI and follow the same
 * explore -> detail -> chapter -> reader flow.
 */
interface UnifiedSource {
    /** Unique source ID */
    val id: Long
    
    /** Source display name */
    val name: String
    
    /** ISO 639-1 language code */
    val lang: String
    
    /** Whether source contains NSFW content */
    val isNsfw: Boolean
        get() = false
    
    /** Source icon URL or path */
    val iconUrl: String?
        get() = null
    
    /** Base URL of the source website */
    val baseUrl: String?
        get() = null
    
    /** Loader type that provides this source */
    val loaderType: SourceLoaderType
    
    /** Content type this source provides */
    val contentType: SourceContentType
        get() = SourceContentType.MANGA
    
    /** Whether source supports latest updates */
    val supportsLatest: Boolean
        get() = false
    
    /** Source capabilities */
    val capabilities: Set<SourceCapability>
        get() = setOf(SourceCapability.BROWSE, SourceCapability.SEARCH)
    
    // ==================== Browse Methods ====================
    
    /**
     * Get popular/trending content.
     */
    suspend fun getPopular(page: Int): SourceItemsPage
    
    /**
     * Get latest updates.
     */
    suspend fun getLatest(page: Int): SourceItemsPage
    
    /**
     * Search for content.
     */
    suspend fun search(query: String, page: Int, filters: SourceFilterList = SourceFilterList()): SourceItemsPage
    
    /**
     * Get available filters for search.
     */
    fun getFilterList(): SourceFilterList = SourceFilterList()
    
    // ==================== Detail Methods ====================
    
    /**
     * Get full details for a content item.
     */
    suspend fun getDetails(item: SourceItem): SourceItemDetails
    
    /**
     * Get chapters/episodes for a content item.
     */
    suspend fun getChapters(item: SourceItem): List<SourceChapter>
    
    // ==================== Reader Methods ====================
    
    /**
     * Get content for a chapter.
     * Returns text for novels, image URLs for manga.
     */
    suspend fun getChapterContent(chapter: SourceChapter): SourceChapterContent
    
    /**
     * Get image URL for a page (if not already resolved).
     * Only needed for manga sources where page URLs need additional resolution.
     */
    suspend fun getImageUrl(page: SourcePage): String {
        return page.imageUrl ?: throw IllegalStateException("Image URL not available")
    }
    
    // ==================== Optional Methods ====================
    
    /**
     * Get related/recommended content.
     */
    suspend fun getRelated(item: SourceItem): List<SourceItem> = emptyList()
    
    /**
     * Login to source (if required).
     */
    suspend fun login(credentials: SourceCredentials): Boolean = false
    
    /**
     * Check if user is logged in.
     */
    fun isLoggedIn(): Boolean = true
    
    /**
     * Get source preferences/settings.
     */
    fun getPreferences(): List<SourcePreference> = emptyList()
    
    /**
     * Update a preference value.
     */
    fun setPreference(key: String, value: Any) {}
}

/**
 * Primary content delivery type - determines which reader/viewer to use.
 */
@Serializable
enum class ContentDeliveryType {
    /** Text-based content (novels, light novels, web novels) */
    TEXT,
    /** Image-based content (manga, comics, manhwa, manhua) */
    IMAGE,
    /** Video-based content (anime, movies, TV shows) */
    VIDEO
}

/**
 * Content type provided by a source (more specific categorization).
 */
@Serializable
enum class SourceContentType {
    // Text-based
    NOVEL,
    LIGHT_NOVEL,
    WEB_NOVEL,
    FANFICTION,
    
    // Image-based
    MANGA,
    MANHWA,
    MANHUA,
    COMIC,
    WEBTOON,
    
    // Video-based
    ANIME,
    MOVIE,
    TV_SHOW,
    DONGHUA,
    
    // Mixed or unknown
    MIXED,
    OTHER;
    
    /** Get the primary delivery type for this content type */
    val deliveryType: ContentDeliveryType
        get() = when (this) {
            NOVEL, LIGHT_NOVEL, WEB_NOVEL, FANFICTION -> ContentDeliveryType.TEXT
            MANGA, MANHWA, MANHUA, COMIC, WEBTOON -> ContentDeliveryType.IMAGE
            ANIME, MOVIE, TV_SHOW, DONGHUA -> ContentDeliveryType.VIDEO
            MIXED, OTHER -> ContentDeliveryType.TEXT // Default to text
        }
}

/**
 * Source capabilities.
 */
@Serializable
enum class SourceCapability {
    /** Can browse popular content */
    BROWSE,
    /** Can search content */
    SEARCH,
    /** Supports latest updates */
    LATEST,
    /** Supports filters */
    FILTERS,
    /** Requires login */
    LOGIN,
    /** Has Cloudflare protection */
    CLOUDFLARE,
    /** Supports downloading */
    DOWNLOAD,
    /** Supports tracking */
    TRACKING,
    /** Has comments/reviews */
    COMMENTS,
    /** Supports bookmarking on source */
    BOOKMARK,
    /** Has configurable preferences */
    PREFERENCES
}

/**
 * A page of content items.
 */
@Serializable
data class SourceItemsPage(
    val items: List<SourceItem>,
    val hasNextPage: Boolean
)

/**
 * A content item (manga, novel, etc.).
 */
@Serializable
data class SourceItem(
    /** Relative URL or unique identifier */
    val url: String,
    /** Content title */
    val title: String,
    /** Cover/thumbnail URL */
    val coverUrl: String? = null,
    /** Brief description */
    val description: String? = null,
    /** Author name */
    val author: String? = null,
    /** Artist name (for manga) */
    val artist: String? = null,
    /** Comma-separated genres */
    val genres: String? = null,
    /** Publication status */
    val status: SourceItemStatus = SourceItemStatus.UNKNOWN,
    /** Whether details have been fetched */
    val initialized: Boolean = false,
    /** Source ID this item belongs to */
    val sourceId: Long = 0
)

/**
 * Publication status.
 */
@Serializable
enum class SourceItemStatus {
    UNKNOWN,
    ONGOING,
    COMPLETED,
    LICENSED,
    PUBLISHING_FINISHED,
    CANCELLED,
    ON_HIATUS
}

/**
 * Detailed content information.
 */
@Serializable
data class SourceItemDetails(
    /** Basic item info (updated) */
    val item: SourceItem,
    /** Full description */
    val fullDescription: String? = null,
    /** Alternative titles */
    val alternativeTitles: List<String> = emptyList(),
    /** Original language */
    val originalLanguage: String? = null,
    /** Year of publication */
    val year: Int? = null,
    /** Rating (0-10) */
    val rating: Float? = null,
    /** Number of ratings */
    val ratingCount: Int? = null,
    /** Chapter count */
    val chapterCount: Int? = null,
    /** Last update timestamp */
    val lastUpdated: Long? = null,
    /** External links (name -> URL) */
    val externalLinks: Map<String, String> = emptyMap(),
    /** Related content URLs */
    val relatedUrls: List<String> = emptyList()
)

/**
 * A chapter/episode.
 */
@Serializable
data class SourceChapter(
    /** Relative URL or unique identifier */
    val url: String,
    /** Chapter name/title */
    val name: String,
    /** Chapter number (-1 if unknown) */
    val number: Float = -1f,
    /** Volume number */
    val volume: Int? = null,
    /** Upload date (epoch millis) */
    val dateUpload: Long = 0L,
    /** Scanlator/translator group */
    val scanlator: String? = null,
    /** Source ID */
    val sourceId: Long = 0,
    /** Parent item URL */
    val itemUrl: String = ""
)

/**
 * Chapter content (text, images, or video).
 */
@Serializable
data class SourceChapterContent(
    /** Chapter URL */
    val chapterUrl: String,
    /** Content delivery type */
    val type: ContentDeliveryType,
    /** Text content (for novels) */
    val text: String? = null,
    /** Pages (for manga) */
    val pages: List<SourcePage> = emptyList(),
    /** Video streams (for anime/movies) */
    val videoStreams: List<SourceVideoStream> = emptyList(),
    /** Subtitles (for video) */
    val subtitles: List<SourceSubtitle> = emptyList()
)

/**
 * Content type of a chapter.
 */
@Serializable
enum class SourceChapterContentType {
    TEXT,
    IMAGES,
    VIDEO,
    MIXED
}

/**
 * Video stream information.
 */
@Serializable
data class SourceVideoStream(
    /** Stream URL */
    val url: String,
    /** Quality label (e.g., "1080p", "720p", "480p") */
    val quality: String,
    /** Video format (e.g., "mp4", "m3u8", "webm") */
    val format: String = "mp4",
    /** Whether this is the default/preferred stream */
    val isDefault: Boolean = false,
    /** Audio language (if separate) */
    val audioLang: String? = null,
    /** Headers required for playback */
    val headers: Map<String, String> = emptyMap(),
    /** Subtitles embedded in stream */
    val hasEmbeddedSubs: Boolean = false
)

/**
 * Subtitle track information.
 */
@Serializable
data class SourceSubtitle(
    /** Subtitle URL */
    val url: String,
    /** Language code (e.g., "en", "ja") */
    val lang: String,
    /** Language display name */
    val label: String,
    /** Subtitle format (e.g., "srt", "vtt", "ass") */
    val format: String = "srt",
    /** Whether this is the default subtitle */
    val isDefault: Boolean = false
)

/**
 * A page in a chapter (for manga).
 */
@Serializable
data class SourcePage(
    /** Page index (0-based) */
    val index: Int,
    /** Page URL (for fetching image URL) */
    val url: String = "",
    /** Direct image URL */
    val imageUrl: String? = null
) {
    val number: Int get() = index + 1
}

/**
 * Login credentials.
 */
@Serializable
data class SourceCredentials(
    val username: String,
    val password: String,
    val twoFactorCode: String? = null,
    val rememberMe: Boolean = true
)

/**
 * Source preference definition.
 */
@Serializable
sealed class SourcePreference {
    abstract val key: String
    abstract val title: String
    abstract val summary: String?
    
    @Serializable
    data class EditText(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val defaultValue: String = "",
        val currentValue: String = ""
    ) : SourcePreference()
    
    @Serializable
    data class Switch(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val defaultValue: Boolean = false,
        val currentValue: Boolean = false
    ) : SourcePreference()
    
    @Serializable
    data class ListSelection(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val entries: List<String>,
        val entryValues: List<String>,
        val defaultValue: String = "",
        val currentValue: String = ""
    ) : SourcePreference()
    
    @Serializable
    data class MultiSelect(
        override val key: String,
        override val title: String,
        override val summary: String? = null,
        val entries: List<String>,
        val entryValues: List<String>,
        val defaultValues: Set<String> = emptySet(),
        val currentValues: Set<String> = emptySet()
    ) : SourcePreference()
}

/**
 * Filter list for search.
 */
@Serializable
data class SourceFilterList(
    val filters: List<SourceFilter> = emptyList()
)

/**
 * Search filter types.
 */
@Serializable
sealed class SourceFilter {
    abstract val name: String
    
    @Serializable
    data class Header(override val name: String) : SourceFilter()
    
    @Serializable
    data class Separator(override val name: String = "") : SourceFilter()
    
    @Serializable
    data class Text(
        override val name: String,
        val state: String = ""
    ) : SourceFilter()
    
    @Serializable
    data class CheckBox(
        override val name: String,
        val state: Boolean = false
    ) : SourceFilter()
    
    @Serializable
    data class TriState(
        override val name: String,
        val state: Int = STATE_IGNORE
    ) : SourceFilter() {
        companion object {
            const val STATE_IGNORE = 0
            const val STATE_INCLUDE = 1
            const val STATE_EXCLUDE = 2
        }
    }
    
    @Serializable
    data class Select(
        override val name: String,
        val values: List<String>,
        val state: Int = 0
    ) : SourceFilter()
    
    @Serializable
    data class Sort(
        override val name: String,
        val values: List<String>,
        val state: SortState? = null
    ) : SourceFilter() {
        @Serializable
        data class SortState(val index: Int, val ascending: Boolean)
    }
    
    @Serializable
    data class Group(
        override val name: String,
        val filters: List<SourceFilter>
    ) : SourceFilter()
}
