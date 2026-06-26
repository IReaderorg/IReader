package eu.kanade.tachiyomi.source.model

/**
 * Minimal RefreshContext class shim for tsundoku extension compatibility.
 */
data class RefreshContext(
    val existingChapters: List<SChapter> = emptyList()
)
