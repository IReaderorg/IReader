package ireader.data.catalog.impl.tsundoku

import com.fleeksoft.ksoup.Ksoup
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import ireader.core.log.Log
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.PageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import eu.kanade.tachiyomi.source.model.Page as TPage

class TsundokuCatalogSource(
    private val source: CatalogueSource
) : BaseTsundokuCatalogSource() {

    override val id: Long get() = source.id
    override val name: String get() = source.name
    override val lang: String get() = source.lang
    override val supportsLatest: Boolean get() = source.supportsLatest

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

    override suspend fun getMangaList(filters: List<ireader.core.source.model.Filter<*>>, page: Int): MangasPageInfo {
        return try {
            val query = filters.filterIsInstance<ireader.core.source.model.Filter.Text>()
                .firstOrNull { it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true) }
                ?.value ?: ""
            val result = withContext(Dispatchers.IO) {
                source.getSearchManga(page, query, source.getFilterList())
            }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList(filters) failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            val result = withContext(Dispatchers.IO) { source.getMangaDetails(manga.toSManga()) }
            result.toMangaInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaDetails failed", e)
            manga
        }
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            val result = withContext(Dispatchers.IO) { source.getChapterList(manga.toSManga()) }
            Log.info { "Tsundoku[$name]: got ${result.size} chapters" }
            result.map { it.toChapterInfo() }
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getChapterList failed", e)
            emptyList()
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            val result = withContext(Dispatchers.IO) { source.getPageList(chapter.toSChapter()) }
            Log.info { "Tsundoku[$name]: got ${result.size} pages" }

            val isNovel = try { source.isNovelSource } catch (_: Exception) { false }
            if (isNovel && result.isNotEmpty()) {
                val html = withContext(Dispatchers.IO) { source.fetchPageText(result.first()) }
                if (html.isNotBlank()) return parseNovelContent(html)
                return emptyList()
            }

            result.map { it.toPage() }
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getPageList failed", e)
            emptyList()
        }
    }


    // ==================== Model Conversions ====================

    private fun MangaInfo.toSManga(): SManga {
        val url = this.key
        val relativeUrl = if (baseUrl.isNotBlank() && url.startsWith(baseUrl)) {
            url.removePrefix(baseUrl)
        } else {
            url
        }
        return SManga.create().also {
            it.url = relativeUrl
            it.title = this.title
            it.artist = this.artist
            it.author = this.author
            it.description = this.description
            it.genre = this.genres.joinToString(", ")
            it.status = this.status.toInt()
            it.thumbnail_url = this.cover.ifBlank { null }
            it.initialized = true
        }
    }

    private fun SManga.toMangaInfo(): MangaInfo {
        val url = try { this.url } catch (_: UninitializedPropertyAccessException) { "" }
        val fullUrl = if (url.isNotBlank() && !url.startsWith("http") && baseUrl.isNotBlank()) {
            baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        } else {
            url
        }
        val title = try { this.title } catch (_: UninitializedPropertyAccessException) { "" }
        val artist = try { this.artist } catch (_: UninitializedPropertyAccessException) { null }
        val author = try { this.author } catch (_: UninitializedPropertyAccessException) { null }
        val description = try { this.description } catch (_: UninitializedPropertyAccessException) { null }
        val genre = try { this.genre } catch (_: UninitializedPropertyAccessException) { null }
        val status = try { this.status } catch (_: UninitializedPropertyAccessException) { 0 }
        val thumbnail = try { this.thumbnail_url } catch (_: UninitializedPropertyAccessException) { null }
        return MangaInfo(
            key = fullUrl,
            title = title,
            artist = artist ?: "",
            author = author ?: "",
            description = description ?: "",
            genres = this.getGenres() ?: emptyList(),
            status = status.toLong(),
            cover = thumbnail ?: ""
        )
    }

    private fun ChapterInfo.toSChapter(): SChapter {
        val url = this.key
        // Strip baseUrl prefix since HttpSource.pageListRequest() prepends it internally
        val relativeUrl = if (baseUrl.isNotBlank() && url.startsWith(baseUrl)) {
            url.removePrefix(baseUrl)
        } else {
            url
        }
        return SChapter.create().also {
            it.url = relativeUrl
            it.name = this.name
            it.chapter_number = this.number
            it.date_upload = this.dateUpload
            it.scanlator = this.scanlator.ifBlank { null }
        }
    }

    private val baseUrl: String
        get() = (source as? HttpSource)?.baseUrl ?: ""

    private fun SChapter.toChapterInfo(): ChapterInfo {
        val url = this.url
        val fullUrl = if (url.isNotBlank() && !url.startsWith("http")) {
            baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        } else {
            url
        }
        return ChapterInfo(
            key = fullUrl,
            name = this.name,
            number = this.chapter_number.toFloat(),
            dateUpload = this.date_upload,
            scanlator = this.scanlator ?: ""
        )
    }

    private fun TPage.toPage(): Page = when {
        !this.text.isNullOrBlank() -> {
            val textRes = Ksoup.parse(this.text ?: "").text()
            ireader.core.source.model.Text(textRes)
        }
        !this.imageUrl.isNullOrBlank() -> ImageUrl(this.imageUrl!!)
        this.url.isNotBlank() -> PageUrl(this.url)
        else -> PageUrl("")
    }

    private fun MangasPage.toMangasPageInfo(): MangasPageInfo = MangasPageInfo(
        mangas = this.mangas.map { it.toMangaInfo() },
        hasNextPage = this.hasNextPage
    )
}
