package ireader.data.catalog.impl.tsundoku

import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page as TPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.FilterList as IReaderFilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.PageUrl

class TsundokuCatalogSource(
    private val source: CatalogueSource
) : CatalogSource {

    override val id: Long get() = source.id
    override val name: String get() = source.name
    override val lang: String get() = source.lang

    val supportsLatest: Boolean get() = source.supportsLatest

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            Log.info { "Tsundoku[$name]: getMangaList(sort=${sort?.let { it::class.simpleName }}, page=$page)" }
            val result = when {
                sort == null || sort is PopularListing -> source.getPopularManga(page)
                sort is LatestListing -> source.getLatestUpdates(page)
                else -> source.getPopularManga(page)
            }
            Log.info { "Tsundoku[$name]: got ${result.mangas.size} mangas, hasNextPage=${result.hasNextPage}" }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaList(filters: IReaderFilterList, page: Int): MangasPageInfo {
        return try {
            val query = filters.filterIsInstance<ireader.core.source.model.Filter.Text>()
                .firstOrNull { it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true) }
                ?.value ?: ""
            val result = source.getSearchManga(page, query, source.getFilterList())
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList(filters) failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            val result = source.getMangaDetails(manga.toSManga())
            result.toMangaInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaDetails failed", e)
            manga
        }
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            val result = source.getChapterList(manga.toSManga())
            Log.info { "Tsundoku[$name]: got ${result.size} chapters" }
            result.map { it.toChapterInfo() }
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getChapterList failed", e)
            emptyList()
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            val result = source.getPageList(chapter.toSChapter())
            Log.info { "Tsundoku[$name]: got ${result.size} pages" }
            result.map { it.toPage() }
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getPageList failed", e)
            emptyList()
        }
    }

    override fun getFilters(): IReaderFilterList = emptyList()

    override fun getListings(): List<Listing> {
        val listings = mutableListOf<Listing>(PopularListing())
        if (source.supportsLatest) listings.add(LatestListing())
        return listings
    }

    override fun getCommands(): CommandList = emptyList()

    override fun toString(): String = "TsundokuSource($name, $lang, id=$id)"

    // ==================== Model Conversions ====================

    private fun MangaInfo.toSManga(): SManga = SManga.create().also {
        it.url = this.key
        it.title = this.title
        it.artist = this.artist
        it.author = this.author
        it.description = this.description
        it.genre = this.genres.joinToString(", ")
        it.status = this.status.toInt()
        it.thumbnail_url = this.cover.ifBlank { null }
        it.initialized = true
    }

    private fun SManga.toMangaInfo(): MangaInfo = MangaInfo(
        key = this.url,
        title = this.title,
        artist = this.artist ?: "",
        author = this.author ?: "",
        description = this.description ?: "",
        genres = this.getGenres() ?: emptyList(),
        status = this.status.toLong(),
        cover = this.thumbnail_url ?: ""
    )

    private fun ChapterInfo.toSChapter(): SChapter = SChapter.create().also {
        it.url = this.key
        it.name = this.name
        it.chapter_number = this.number
        it.date_upload = this.dateUpload
        it.scanlator = this.scanlator.ifBlank { null }
    }

    private fun SChapter.toChapterInfo(): ChapterInfo = ChapterInfo(
        key = this.url,
        name = this.name,
        number = this.chapter_number.toFloat(),
        dateUpload = this.date_upload,
        scanlator = this.scanlator ?: ""
    )

    private fun TPage.toPage(): Page = when {
        !this.text.isNullOrBlank() -> ireader.core.source.model.Text(this.text!!)
        !this.imageUrl.isNullOrBlank() -> ImageUrl(this.imageUrl!!)
        this.url.isNotBlank() -> PageUrl(this.url)
        else -> PageUrl("")
    }

    private fun MangasPage.toMangasPageInfo(): MangasPageInfo = MangasPageInfo(
        mangas = this.mangas.map { it.toMangaInfo() },
        hasNextPage = this.hasNextPage
    )

    class PopularListing : Listing("Popular")
    class LatestListing : Listing("Latest")
}
