package eu.kanade.tachiyomi.source.model

/**
 * Minimal SMangaUpdate class shim for tsundoku extension compatibility.
 */
data class SMangaUpdate(
    val manga: SManga,
    val chapters: List<SChapter>
)
