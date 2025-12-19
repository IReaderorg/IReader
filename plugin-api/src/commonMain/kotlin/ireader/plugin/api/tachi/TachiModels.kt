package ireader.plugin.api.tachi

import kotlinx.serialization.Serializable

/**
 * Tachiyomi-compatible manga model.
 * Mirrors eu.kanade.tachiyomi.source.model.SManga for compatibility.
 */
@Serializable
data class TachiManga(
    /** Relative URL to the manga page */
    val url: String,
    /** Manga title */
    val title: String,
    /** Artist name */
    val artist: String? = null,
    /** Author name */
    val author: String? = null,
    /** Manga description/synopsis */
    val description: String? = null,
    /** Comma-separated genres */
    val genre: String? = null,
    /** Publication status (see STATUS_* constants) */
    val status: Int = STATUS_UNKNOWN,
    /** Thumbnail/cover image URL */
    val thumbnailUrl: String? = null,
    /** Update strategy */
    val updateStrategy: TachiUpdateStrategy = TachiUpdateStrategy.ALWAYS_UPDATE,
    /** Whether manga details have been fetched */
    val initialized: Boolean = false
) {
    companion object {
        const val STATUS_UNKNOWN = 0
        const val STATUS_ONGOING = 1
        const val STATUS_COMPLETED = 2
        const val STATUS_LICENSED = 3
        const val STATUS_PUBLISHING_FINISHED = 4
        const val STATUS_CANCELLED = 5
        const val STATUS_ON_HIATUS = 6
    }
    
    /** Get genres as a list */
    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre.split(", ").map { it.trim() }.filterNot { it.isBlank() }.distinct()
    }
}

/**
 * Update strategy for manga.
 */
@Serializable
enum class TachiUpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE
}

/**
 * Tachiyomi-compatible chapter model.
 * Mirrors eu.kanade.tachiyomi.source.model.SChapter.
 */
@Serializable
data class TachiChapter(
    /** Relative URL to the chapter */
    val url: String,
    /** Chapter name/title */
    val name: String,
    /** Upload date (epoch millis) */
    val dateUpload: Long = 0L,
    /** Chapter number (-1 if unknown) */
    val chapterNumber: Float = -1f,
    /** Scanlator/translator group */
    val scanlator: String? = null
)

/**
 * Tachiyomi-compatible page model.
 * Mirrors eu.kanade.tachiyomi.source.model.Page.
 */
@Serializable
data class TachiPage(
    /** Page index (0-based) */
    val index: Int,
    /** Page URL (for fetching image URL) */
    val url: String = "",
    /** Direct image URL */
    val imageUrl: String? = null
) {
    /** 1-based page number */
    val number: Int get() = index + 1
}

/**
 * Page with a list of manga and pagination info.
 */
@Serializable
data class TachiMangasPage(
    /** List of manga on this page */
    val mangas: List<TachiManga>,
    /** Whether there are more pages */
    val hasNextPage: Boolean
)

/**
 * Filter types for search.
 */
@Serializable
sealed class TachiFilter {
    abstract val name: String
    
    @Serializable
    data class Header(override val name: String) : TachiFilter()
    
    @Serializable
    data class Separator(override val name: String = "") : TachiFilter()
    
    @Serializable
    data class Text(
        override val name: String,
        val state: String = ""
    ) : TachiFilter()
    
    @Serializable
    data class CheckBox(
        override val name: String,
        val state: Boolean = false
    ) : TachiFilter()
    
    @Serializable
    data class TriState(
        override val name: String,
        val state: Int = STATE_IGNORE
    ) : TachiFilter() {
        fun isIgnored() = state == STATE_IGNORE
        fun isIncluded() = state == STATE_INCLUDE
        fun isExcluded() = state == STATE_EXCLUDE
        
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
    ) : TachiFilter()
    
    @Serializable
    data class Sort(
        override val name: String,
        val values: List<String>,
        val selection: SortSelection? = null
    ) : TachiFilter() {
        @Serializable
        data class SortSelection(val index: Int, val ascending: Boolean)
    }
    
    @Serializable
    data class Group(
        override val name: String,
        val filters: List<TachiFilter>
    ) : TachiFilter()
}

/**
 * List of filters.
 */
@Serializable
data class TachiFilterList(
    val filters: List<TachiFilter> = emptyList()
)
