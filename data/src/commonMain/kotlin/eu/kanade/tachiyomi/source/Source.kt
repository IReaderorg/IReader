package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate

/**
 * Minimal Source interface shim for tsundoku extension compatibility.
 */
interface Source {
    val id: Long
    val name: String
    val lang: String get() = ""
    val isNovelSource: Boolean get() = false
    val supportsLatest: Boolean get() = false

    fun getFilterList(): FilterList = FilterList()

    suspend fun getPopularManga(page: Int): MangasPage
    suspend fun getLatestUpdates(page: Int): MangasPage
    suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage

    suspend fun getMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate = SMangaUpdate(manga, chapters)

    suspend fun getMangaDetails(manga: SManga): SManga = manga
    suspend fun getChapterList(manga: SManga): List<SChapter> = emptyList()
    suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
}
