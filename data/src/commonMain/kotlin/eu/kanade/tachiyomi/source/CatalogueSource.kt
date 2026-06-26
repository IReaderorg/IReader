package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate

/**
 * Minimal CatalogueSource interface shim for tsundoku extension compatibility.
 */
interface CatalogueSource : Source {
    override val lang: String

    override suspend fun getMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate {
        val asyncManga = if (fetchDetails) getMangaDetails(manga) else manga
        val asyncChapters = if (fetchChapters) getChapterList(manga) else chapters
        return SMangaUpdate(asyncManga, asyncChapters)
    }

    override suspend fun getPageList(chapter: SChapter): List<Page>
}
