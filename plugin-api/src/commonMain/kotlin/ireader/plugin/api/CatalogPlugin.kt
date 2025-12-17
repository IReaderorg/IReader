package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for content catalog sources.
 * Catalog plugins provide novel/manga sources from various providers.
 * 
 * Supports:
 * - LNReader catalog format
 * - UserSource format
 * - Custom source implementations
 * 
 * Example:
 * ```kotlin
 * class LNReaderCatalogPlugin : CatalogPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.lnreader-catalog",
 *         name = "LNReader Catalog",
 *         type = PluginType.CATALOG,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.CATALOG_WRITE),
 *         // ... other manifest fields
 *     )
 *     
 *     override val catalogType = CatalogType.LNREADER
 *     
 *     override suspend fun getSources(): List<CatalogSource> {
 *         // Return available sources
 *     }
 * }
 * ```
 */
interface CatalogPlugin : Plugin {
    /**
     * Type of catalog this plugin provides.
     */
    val catalogType: CatalogType
    
    /**
     * Catalog metadata.
     */
    val catalogInfo: CatalogInfo
    
    /**
     * Get all available sources from this catalog.
     */
    suspend fun getSources(): List<CatalogSource>
    
    /**
     * Search for content across all sources.
     */
    suspend fun search(query: String, filters: SearchFilters = SearchFilters()): CatalogResult<List<CatalogItem>>
    
    /**
     * Get content details by ID.
     */
    suspend fun getDetails(sourceId: String, contentId: String): CatalogResult<CatalogItemDetails>
    
    /**
     * Get chapters/episodes for content.
     */
    suspend fun getChapters(sourceId: String, contentId: String): CatalogResult<List<CatalogChapter>>
    
    /**
     * Get chapter content (text/images).
     */
    suspend fun getChapterContent(
        sourceId: String,
        contentId: String,
        chapterId: String
    ): CatalogResult<ChapterContent>
    
    /**
     * Check if catalog supports a specific feature.
     */
    fun supportsFeature(feature: CatalogFeature): Boolean
    
    /**
     * Get popular/trending content.
     */
    suspend fun getPopular(sourceId: String, page: Int = 1): CatalogResult<List<CatalogItem>>
    
    /**
     * Get latest updates.
     */
    suspend fun getLatest(sourceId: String, page: Int = 1): CatalogResult<List<CatalogItem>>
    
    /**
     * Refresh catalog sources (check for updates).
     */
    suspend fun refresh(): CatalogResult<Unit>
}

/**
 * Type of catalog source.
 */
@Serializable
enum class CatalogType {
    /** LNReader catalog format */
    LNREADER,
    /** UserSource format (user-defined sources) */
    USER_SOURCE,
    /** Tachiyomi/Mihon extension format */
    TACHIYOMI,
    /** Custom implementation */
    CUSTOM
}

/**
 * Catalog metadata.
 */
@Serializable
data class CatalogInfo(
    /** Catalog display name */
    val name: String,
    /** Catalog description */
    val description: String,
    /** Catalog icon URL */
    val iconUrl: String? = null,
    /** Catalog website */
    val website: String? = null,
    /** Supported languages */
    val languages: List<String> = emptyList(),
    /** Content types (novel, manga, etc.) */
    val contentTypes: List<ContentType> = listOf(ContentType.NOVEL),
    /** Whether catalog requires login */
    val requiresLogin: Boolean = false,
    /** Catalog update frequency */
    val updateFrequency: UpdateFrequency = UpdateFrequency.DAILY
)

@Serializable
enum class ContentType {
    NOVEL,
    MANGA,
    COMIC,
    LIGHT_NOVEL,
    WEB_NOVEL,
    FANFICTION
}

@Serializable
enum class UpdateFrequency {
    REALTIME,
    HOURLY,
    DAILY,
    WEEKLY,
    MANUAL
}

/**
 * A source within a catalog.
 */
@Serializable
data class CatalogSource(
    /** Unique source identifier */
    val id: String,
    /** Source display name */
    val name: String,
    /** Source language */
    val language: String,
    /** Source icon URL */
    val iconUrl: String? = null,
    /** Source base URL */
    val baseUrl: String,
    /** Whether source is NSFW */
    val isNsfw: Boolean = false,
    /** Source version */
    val version: Int = 1,
    /** Supported features */
    val features: List<CatalogFeature> = emptyList()
)

/**
 * Features a catalog source can support.
 */
@Serializable
enum class CatalogFeature {
    SEARCH,
    POPULAR,
    LATEST,
    FILTERS,
    LOGIN,
    CLOUDFLARE_BYPASS,
    DOWNLOAD,
    TRACKING,
    COMMENTS
}

/**
 * Search filters for catalog queries.
 */
@Serializable
data class SearchFilters(
    val sourceIds: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val status: ContentStatus? = null,
    val contentType: ContentType? = null,
    val language: String? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val page: Int = 1,
    val pageSize: Int = 20
)

@Serializable
enum class ContentStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    CANCELLED,
    UNKNOWN
}

@Serializable
enum class SortOption {
    RELEVANCE,
    LATEST,
    POPULAR,
    RATING,
    TITLE,
    CHAPTERS
}

/**
 * A content item from catalog (novel, manga, etc.).
 */
@Serializable
data class CatalogItem(
    /** Unique content identifier */
    val id: String,
    /** Source this item belongs to */
    val sourceId: String,
    /** Content title */
    val title: String,
    /** Cover image URL */
    val coverUrl: String? = null,
    /** Brief description */
    val description: String? = null,
    /** Author name */
    val author: String? = null,
    /** Content status */
    val status: ContentStatus = ContentStatus.UNKNOWN,
    /** Genres/tags */
    val genres: List<String> = emptyList(),
    /** Rating (0-10) */
    val rating: Float? = null,
    /** Chapter count */
    val chapterCount: Int? = null,
    /** Last update timestamp (epoch millis) */
    val lastUpdated: Long? = null
)

/**
 * Detailed content information.
 */
@Serializable
data class CatalogItemDetails(
    /** Basic item info */
    val item: CatalogItem,
    /** Full description */
    val fullDescription: String? = null,
    /** Alternative titles */
    val alternativeTitles: List<String> = emptyList(),
    /** Artist (for manga) */
    val artist: String? = null,
    /** Original language */
    val originalLanguage: String? = null,
    /** Year of publication */
    val year: Int? = null,
    /** Related content IDs */
    val relatedIds: List<String> = emptyList(),
    /** External links */
    val externalLinks: Map<String, String> = emptyMap()
)

/**
 * A chapter/episode in content.
 */
@Serializable
data class CatalogChapter(
    /** Unique chapter identifier */
    val id: String,
    /** Chapter title */
    val title: String,
    /** Chapter number */
    val number: Float,
    /** Volume number (optional) */
    val volume: Int? = null,
    /** Upload/release date (epoch millis) */
    val dateUpload: Long? = null,
    /** Scanlator/translator group */
    val scanlator: String? = null,
    /** Chapter URL */
    val url: String? = null
)

/**
 * Chapter content (text or images).
 */
@Serializable
data class ChapterContent(
    /** Chapter ID */
    val chapterId: String,
    /** Text content (for novels) */
    val text: String? = null,
    /** Image URLs (for manga) */
    val imageUrls: List<String> = emptyList(),
    /** Content type */
    val type: ChapterContentType
)

@Serializable
enum class ChapterContentType {
    TEXT,
    IMAGES,
    MIXED
}

/**
 * Result wrapper for catalog operations.
 */
sealed class CatalogResult<out T> {
    data class Success<T>(val data: T) : CatalogResult<T>()
    data class Error(val error: CatalogError) : CatalogResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): CatalogResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Catalog operation errors.
 */
@Serializable
sealed class CatalogError {
    data class NetworkError(val message: String) : CatalogError()
    data class ParseError(val message: String) : CatalogError()
    data class SourceNotFound(val sourceId: String) : CatalogError()
    data class ContentNotFound(val contentId: String) : CatalogError()
    data class RateLimited(val retryAfterMs: Long?) : CatalogError()
    data class AuthRequired(val message: String) : CatalogError()
    data class CloudflareBlocked(val message: String) : CatalogError()
    data class Unknown(val message: String) : CatalogError()
}
