package ireader.data.catalog.impl.tsundoku

import com.fleeksoft.ksoup.Ksoup
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import ireader.core.log.Log
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.Filter
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.PageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import eu.kanade.tachiyomi.source.model.Filter as TFilter
import eu.kanade.tachiyomi.source.model.Page as TPage

class TsundokuCatalogSource(
    private val source: CatalogueSource
) : BaseTsundokuCatalogSource() {

    override val id: Long get() = source.id
    override val name: String get() = source.name
    override val lang: String get() = source.lang
    override val supportsLatest: Boolean get() = source.supportsLatest

    // ── Filter conversion ──────────────────────────────────────────

    override fun getFilters(): List<Filter<*>> {
        val tsundokuFilters = try {
            source.getFilterList()
        } catch (e: Exception) {
            Log.warn { "Tsundoku[$name]: getFilterList() failed: ${e.message}" }
            emptyList()
        }
        return TsundokuFilterBridge.toIReaderFilters(tsundokuFilters)
    }

    // ── CatalogSource overrides ────────────────────────────────────

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo = withContext(Dispatchers.IO) {
        try {
            Log.info { "Tsundoku[$name]: getMangaList(sort=${sort?.let { it::class.simpleName }}, page=$page)" }
            val result = when {
                sort is LatestListing -> {
                    if (supportsLatest) {
                        source.getLatestUpdates(page)
                    } else {
                        Log.info { "Tsundoku[$name]: Source does not support latest updates, falling back to popular" }
                        source.getPopularManga(page)
                    }
                }
                sort is SearchListing -> source.getPopularManga(page)
                else -> source.getPopularManga(page)
            }
            Log.info { "Tsundoku[$name]: got ${result.mangas.size} mangas, hasNextPage=${result.hasNextPage}" }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList(sort) failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaList(filters: List<Filter<*>>, page: Int): MangasPageInfo = withContext(Dispatchers.IO) {
        try {
            // Check for WebView Explore fetch command
            filters.filterIsInstance<Command.Explore.Fetch>().firstOrNull()?.let { cmd ->
                if (cmd.html.isNotBlank()) {
                    Log.info { "Tsundoku[$name]: getMangaList using Explore Fetch HTML" }
                    val doc = Ksoup.parse(cmd.html)
                    val mangaCards = doc.select(".novel-item, .book-item, .manga-item, .list-novel .row, .col-novel, a[href*=/novel/], a[href*=/book/], a[href*=/manga/]")
                    val mangas = mangaCards.mapNotNull { card ->
                        val link = if (card.tagName() == "a") card else card.selectFirst("a[href]")
                        val title = card.selectFirst(".title, h2, h3, h4, [itemprop=name]")?.text()?.trim()
                            ?: link?.attr("title")?.trim()
                            ?: link?.text()?.trim()
                        val href = link?.attr("href")?.trim()
                        val cover = card.selectFirst("img")?.let { it.attr("src").ifBlank { it.attr("data-src") } }?.trim() ?: ""
                        if (!title.isNullOrBlank() && !href.isNullOrBlank()) {
                            val fullUrl = if (!href.startsWith("http")) buildAbsoluteUrl(href) else href
                            MangaInfo(
                                key = fullUrl,
                                title = title,
                                cover = if (cover.isNotBlank()) buildAbsoluteUrl(cover) else ""
                            )
                        } else null
                    }.distinctBy { it.key }

                    if (mangas.isNotEmpty()) {
                        return@withContext MangasPageInfo(mangas, hasNextPage = false)
                    }
                }
            }

            // 1. Extract search query from Filter.Title or Filter.Text("Search" / "Title")
            val titleFilter = filters.filterIsInstance<Filter.Title>().firstOrNull()
                ?: filters.filterIsInstance<Filter.Text>().firstOrNull {
                    it.name.equals("Search", ignoreCase = true) || it.name.equals("Title", ignoreCase = true)
                }
            val query = titleFilter?.value?.trim().orEmpty()

            // 2. Fetch a fresh FilterList instance from the source
            val freshFilterList = try {
                source.getFilterList()
            } catch (e: Exception) {
                Log.warn { "Tsundoku[$name]: getFilterList() failed: ${e.message}" }
                FilterList()
            }

            // 3. Synchronize modified filter values onto the fresh FilterList
            val syncedFilterList = TsundokuFilterBridge.syncToTsundoku(freshFilterList, filters)

            Log.info { "Tsundoku[$name]: getSearchManga(page=$page, query='$query', filters=${syncedFilterList.size})" }

            // 4. If query is blank and all filters remain at default, fallback to popular
            val result = if (query.isBlank() && TsundokuFilterBridge.isFilterListDefault(syncedFilterList)) {
                Log.info { "Tsundoku[$name]: Blank query and default filters -> falling back to popular manga" }
                source.getPopularManga(page)
            } else {
                source.getSearchManga(page, query, syncedFilterList)
            }

            Log.info { "Tsundoku[$name]: got ${result.mangas.size} search results, hasNextPage=${result.hasNextPage}" }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList(filters) failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        commands.filterIsInstance<Command.Detail.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getMangaDetails using Fetch command HTML" }
                val targetUrl = cmd.url.ifBlank { manga.key }

                // 1. Try invoking extension's mangaDetailsParse method via reflection
                val fromExtension = tryExtensionMangaDetailsParse(cmd.html, targetUrl)
                if (fromExtension != null) {
                    val converted = fromExtension.toMangaInfo()
                    return manga.copy(
                        title = converted.title.ifBlank { manga.title },
                        cover = converted.cover.ifBlank { manga.cover },
                        description = converted.description.ifBlank { manga.description },
                        author = converted.author.ifBlank { manga.author },
                        artist = converted.artist.ifBlank { manga.artist },
                        genres = if (converted.genres.isNotEmpty()) converted.genres else manga.genres,
                        status = if (converted.status != MangaInfo.UNKNOWN) converted.status else manga.status
                    )
                }

                // 2. Resilient DOM & Metadata Fallback
                val doc = Ksoup.parse(cmd.html)
                val title = doc.selectFirst("h1, .title, .novel-title, .book-title, [itemprop=name]")?.text()?.trim()
                    ?: doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim()
                    ?: manga.title
                val cover = doc.selectFirst("img.cover, .thumbnail img, [itemprop=image], .novel-cover img")?.let {
                    it.attr("src").ifBlank { it.attr("data-src") }
                }?.trim()
                    ?: doc.selectFirst("meta[property=og:image]")?.attr("content")?.trim()
                    ?: manga.cover
                val description = doc.selectFirst(".description, .summary, [itemprop=description], .novel-summary, #novel-summary")?.text()?.trim()
                    ?: doc.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
                    ?: manga.description
                val author = doc.selectFirst(".author, [itemprop=author], .novel-author, a[href*=/author/]")?.text()?.trim()
                    ?: manga.author
                val genres = doc.select(".genre a, .genres a, .tags a, .tag a, [itemprop=genre]").map { it.text().trim() }.filter { it.isNotBlank() }

                return manga.copy(
                    title = title.ifBlank { manga.title },
                    cover = if (cover.isNotBlank()) buildAbsoluteUrl(cover) else manga.cover,
                    description = description.ifBlank { manga.description },
                    author = author.ifBlank { manga.author },
                    genres = if (genres.isNotEmpty()) genres else manga.genres
                )
            }
        }

        return try {
            val result = withContext(Dispatchers.IO) { source.getMangaDetails(manga.toSManga()) }
            result.toMangaInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaDetails failed", e)
            manga
        }
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        commands.filterIsInstance<Command.Chapter.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getChapterList using Fetch command HTML" }
                val targetUrl = cmd.url.ifBlank { manga.key }

                // 1. Try invoking extension's chapterListParse method via reflection
                val fromExtension = tryExtensionChapterListParse(cmd.html, targetUrl)
                if (!fromExtension.isNullOrEmpty()) {
                    return fromExtension.map { it.toChapterInfo() }.reversed()
                }

                // 2. Resilient DOM Fallback
                val doc = Ksoup.parse(cmd.html)
                val chapterElements = doc.select(
                    "a[href*=/chapter], .chapter-list a, .chapters a, .wp-manga-chapter a, #chapter-list a, li.chapter a, .chapter a, .list-chapter a, a[href]"
                )
                val chapters = mutableListOf<ChapterInfo>()
                val seenKeys = mutableSetOf<String>()
                for (el in chapterElements) {
                    val name = el.text().trim()
                    val url = el.attr("href").trim()
                    if (name.isBlank() || url.isBlank()) continue
                    val lower = url.lowercase()
                    if (lower.startsWith("javascript:") || lower.startsWith("#") || lower.startsWith("mailto:")) continue
                    val fullUrl = if (!url.startsWith("http")) buildAbsoluteUrl(url) else url
                    if (seenKeys.add(fullUrl)) {
                        chapters.add(
                            ChapterInfo(
                                key = fullUrl,
                                name = name,
                                number = ChapterInfo.extractChapterNumber(name),
                                dateUpload = 0L,
                                scanlator = ""
                            )
                        )
                    }
                }
                return if (chapters.size >= 2 && chapters.first().number > chapters.last().number) {
                    chapters.reversed()
                } else {
                    chapters
                }
            }
        }

        return try {
            val result = withContext(Dispatchers.IO) { source.getChapterList(manga.toSManga()) }
            Log.info { "Tsundoku[$name]: got ${result.size} chapters" }
            result.map { it.toChapterInfo() }.reversed()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getChapterList failed", e)
            emptyList()
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        commands.filterIsInstance<Command.Content.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getPageList using Fetch command HTML" }
                val novelPages = parseNovelContent(cmd.html)
                if (novelPages.isNotEmpty()) return novelPages

                // If no paragraphs detected, check for comic/manga image pages
                val doc = Ksoup.parse(cmd.html)
                val images = doc.select("img[src], img[data-src], img[data-lazy-src]")
                    .mapNotNull { el ->
                        val src = el.attr("src").ifBlank { el.attr("data-src").ifBlank { el.attr("data-lazy-src") } }.trim()
                        if (src.isNotBlank() && !src.startsWith("data:") && !src.contains("logo") && !src.contains("icon")) {
                            if (!src.startsWith("http")) buildAbsoluteUrl(src) else src
                        } else null
                    }
                if (images.isNotEmpty()) {
                    return images.map { imgUrl -> ImageUrl(imgUrl) }
                }
                return emptyList()
            }
        }

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

    // ==================== Synthetic Responses & Reflection ====================

    private fun createSyntheticResponse(url: String, html: String): Response {
        val targetUrl = if (url.isNotBlank()) url else baseUrl.ifBlank { "https://localhost" }
        val mediaType = "text/html; charset=utf-8".toMediaTypeOrNull()
        return Response.Builder()
            .request(Request.Builder().url(targetUrl).build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(html.toResponseBody(mediaType))
            .build()
    }

    private fun tryExtensionMangaDetailsParse(html: String, url: String): SManga? {
        val methods = source.javaClass.methods + source.javaClass.declaredMethods
        val method = methods.firstOrNull {
            it.name == "mangaDetailsParse" &&
            it.parameterTypes.size == 1 &&
            Response::class.java.isAssignableFrom(it.parameterTypes[0])
        } ?: return null

        return try {
            method.isAccessible = true
            val response = createSyntheticResponse(url, html)
            method.invoke(source, response) as? SManga
        } catch (e: Throwable) {
            Log.warn { "Tsundoku[$name]: Reflection mangaDetailsParse failed, using DOM fallback: ${e.message}" }
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryExtensionChapterListParse(html: String, url: String): List<SChapter>? {
        val methods = source.javaClass.methods + source.javaClass.declaredMethods
        val method = methods.firstOrNull {
            it.name == "chapterListParse" &&
            it.parameterTypes.size == 1 &&
            Response::class.java.isAssignableFrom(it.parameterTypes[0])
        } ?: return null

        return try {
            method.isAccessible = true
            val response = createSyntheticResponse(url, html)
            method.invoke(source, response) as? List<SChapter>
        } catch (e: Throwable) {
            Log.warn { "Tsundoku[$name]: Reflection chapterListParse failed, using DOM fallback: ${e.message}" }
            null
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

    /** Build an absolute URL from a possibly relative path. */
    private fun buildAbsoluteUrl(path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = baseUrl.trimEnd('/')
        return if (path.startsWith("/")) "$base$path" else "$base/$path"
    }

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
            number = this.chapter_number,
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
