package eu.kanade.tachiyomi.source

/**
 * Minimal SourceFactory interface shim for tsundoku extension compatibility.
 */
interface SourceFactory {
    fun createSources(): List<Source>
}
