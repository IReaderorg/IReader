package ireader.domain.js.models

import ireader.core.source.model.MangaInfo

/**
 * Novel item returned from JavaScript plugin browse/search operations.
 * Represents a single novel in a list of results.
 */
data class JSNovelItem(
    val name: String,
    val path: String,
    val cover: String? = null
) {
    /**
     * Converts this JavaScript novel item to IReader's MangaInfo domain model.
     */
    fun toMangaInfo(): MangaInfo {
        return MangaInfo(
            key = path,
            title = name,
            cover = cover ?: "",
            description = "",
            author = "",
            artist = "",
            genres = emptyList(),
            status = MangaInfo.UNKNOWN
        )
    }
}
