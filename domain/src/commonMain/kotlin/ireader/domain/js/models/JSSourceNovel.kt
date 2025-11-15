package ireader.domain.js.models

import ireader.core.source.model.MangaInfo

/**
 * Full novel details returned from JavaScript plugin.
 * Contains complete novel information including chapters.
 */
data class JSSourceNovel(
    val name: String,
    val path: String,
    val cover: String?,
    val genres: String?,
    val summary: String?,
    val author: String?,
    val artist: String?,
    val status: String?,
    val chapters: List<JSChapterItem>
) {
    /**
     * Converts this JavaScript source novel to IReader's MangaInfo domain model.
     * Parses genres from comma-separated string and maps status strings to constants.
     */
    fun toMangaInfo(): MangaInfo {
        return MangaInfo(
            key = path,
            title = name,
            cover = cover ?: "",
            description = summary ?: "",
            author = author ?: "",
            artist = artist ?: "",
            genres = parseGenres(genres),
            status = parseStatus(status)
        )
    }
    
    /**
     * Parses a comma-separated genre string into a list.
     */
    private fun parseGenres(genresString: String?): List<String> {
        if (genresString.isNullOrBlank()) return emptyList()
        
        return genresString
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    
    /**
     * Maps status strings to MangaInfo status constants.
     */
    private fun parseStatus(statusString: String?): Long {
        if (statusString.isNullOrBlank()) return MangaInfo.UNKNOWN
        
        return when (statusString.lowercase()) {
            "ongoing", "publishing" -> MangaInfo.ONGOING
            "completed", "complete", "finished" -> MangaInfo.COMPLETED
            "licensed" -> MangaInfo.LICENSED
            "publishing finished" -> MangaInfo.PUBLISHING_FINISHED
            "cancelled", "canceled" -> MangaInfo.CANCELLED
            "on hiatus", "hiatus" -> MangaInfo.ON_HIATUS
            else -> MangaInfo.UNKNOWN
        }
    }
}
