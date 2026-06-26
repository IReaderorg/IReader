package eu.kanade.tachiyomi.source.model

/**
 * Minimal MangasPage class shim for tsundoku extension compatibility.
 */
class MangasPage(val mangas: List<SManga>, val hasNextPage: Boolean) {
    operator fun component1(): List<SManga> = mangas
    operator fun component2(): Boolean = hasNextPage
}
