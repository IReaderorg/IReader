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

    /** Cached Tsundoku filter list for state sync. */
    private val tsundokuFilterList: List<TFilter<*>>
        get() = source.getFilterList()

    override fun getFilters(): List<Filter<*>> {
        val tsundokuFilters = tsundokuFilterList
        if (tsundokuFilters.isEmpty()) return listOf(Filter.Title("Search"))

        val result = mutableListOf<Filter<*>>(Filter.Title("Search"))
        for (tf in tsundokuFilters) {
            val converted = convertTsundokuFilter(tf) ?: continue
            result.add(converted)
        }
        return result
    }

    /** Convert a single Tsundoku filter to an IReader filter. */
    private fun convertTsundokuFilter(tf: TFilter<*>): Filter<*>? {
        return when (tf) {
            is TFilter.Header -> Filter.Note(tf.name)
            is TFilter.Separator -> null // no IReader equivalent, skip gracefully
            is TFilter.Text -> Filter.Text(tf.name, tf.state)
            is TFilter.CheckBox -> Filter.Check(tf.name, value = tf.state)
            is TFilter.TriState -> Filter.Select(
                name = tf.name,
                options = arrayOf("Ignore", "Include", "Exclude"),
                value = tf.state
            )
            is TFilter.Select<*> -> Filter.Select(
                name = tf.name,
                options = tf.values.map { it.toString() }.toTypedArray(),
                value = tf.state
            )
            is TFilter.Sort -> Filter.Sort(
                name = tf.name,
                options = tf.values,
                value = tf.state?.let { Filter.Sort.Selection(it.index, it.ascending) }
            )
            is TFilter.Group<*> -> {
                @Suppress("UNCHECKED_CAST")
                val groupFilters = tf.state as List<TFilter<*>>
                Filter.Group(
                    name = tf.name,
                    filters = groupFilters.mapNotNull { convertTsundokuFilter(it) }
                )
            }
            else -> null
        }
    }

    /** Sync IReader filter values back to the Tsundoku filter list. */
    private fun syncFiltersToTsundoku(ireaderFilters: List<Filter<*>>) {
        val tsundokuFilters = tsundokuFilterList
        if (tsundokuFilters.isEmpty()) return

        // Flatten both lists to align indices (both may contain Group wrappers)
        val flatTsundoku = flattenTsundokuFilters(tsundokuFilters)
        val flatIReader = flattenIReaderFilters(ireaderFilters.drop(1)) // Skip Filter.Title

        for ((index, ireaderFilter) in flatIReader.withIndex()) {
            if (index >= flatTsundoku.size) break
            syncFilterState(flatTsundoku[index], ireaderFilter)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun flattenTsundokuFilters(filters: List<TFilter<*>>): List<TFilter<*>> {
        val result = mutableListOf<TFilter<*>>()
        for (f in filters) {
            when (f) {
                is TFilter.Separator -> continue
                is TFilter.Group<*> -> result.addAll(flattenTsundokuFilters(f.state as List<TFilter<*>>))
                else -> result.add(f)
            }
        }
        return result
    }

    /** Flatten IReader filters by recursing into [Filter.Group] containers. */
    private fun flattenIReaderFilters(filters: List<Filter<*>>): List<Filter<*>> {
        val result = mutableListOf<Filter<*>>()
        for (f in filters) {
            when (f) {
                is Filter.Group -> result.addAll(flattenIReaderFilters(f.filters))
                else -> result.add(f)
            }
        }
        return result
    }

    private fun syncFilterState(tsundoku: TFilter<*>, ireader: Filter<*>) {
        when {
            tsundoku is TFilter.Text && ireader is Filter.Text ->
                tsundoku.state = ireader.value
            tsundoku is TFilter.CheckBox && ireader is Filter.Check ->
                ireader.value?.let { tsundoku.state = it }
            tsundoku is TFilter.TriState && ireader is Filter.Select ->
                tsundoku.state = ireader.value
            tsundoku is TFilter.Select<*> && ireader is Filter.Select ->
                tsundoku.state = ireader.value
            tsundoku is TFilter.Sort && ireader is Filter.Sort ->
                ireader.value?.let {
                    tsundoku.state = TFilter.Sort.Selection(it.index, it.ascending)
                }
        }
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            Log.info { "Tsundoku[$name]: getMangaList(sort=${sort?.let { it::class.simpleName }}, page=$page)" }
            val result = withContext(Dispatchers.IO) {
                when (sort) {
                    is LatestListing -> source.getLatestUpdates(page)
                    is SearchListing -> source.getPopularManga(page) // Show popular until query is typed
                    else -> source.getPopularManga(page)
                }
            }
            Log.info { "Tsundoku[$name]: got ${result.mangas.size} mangas" }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaList(filters: List<ireader.core.source.model.Filter<*>>, page: Int): MangasPageInfo {
        return try {
            // Sync IReader filter values back to Tsundoku filter state
            syncFiltersToTsundoku(filters)

            val query = filters.filterIsInstance<ireader.core.source.model.Filter.Text>()
                .firstOrNull { it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true) }
                ?.value ?: ""
            val result = withContext(Dispatchers.IO) {
                source.getSearchManga(page, query, FilterList(tsundokuFilterList))
            }
            result.toMangasPageInfo()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaList(filters) failed", e)
            MangasPageInfo.empty()
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        // Handle pre-fetched HTML via Command.Detail.Fetch
        commands.filterIsInstance<Command.Detail.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getMangaDetails using Fetch command HTML" }
                val doc = Ksoup.parse(cmd.html)
                // Extract basic manga info — title, cover, description from HTML
                val title = doc.selectFirst("h1, .title, [itemprop=name]")?.text()?.trim() ?: manga.title
                val cover = doc.selectFirst("img.cover, .thumbnail img, [itemprop=image]")?.attr("src")?.trim() ?: ""
                val description = doc.selectFirst(".description, .summary, [itemprop=description]")?.text()?.trim() ?: ""
                return manga.copy(title = title, cover = cover, description = description)
            }
            if (cmd.url.isNotBlank()) {
                // Fallback: fetch from URL
                manga.key.let { /* key is already set by caller */ }
            }
        }

        return try {
            Log.info { "Tsundoku[$name]: getMangaDetails(url=${manga.key})" }
            val result = withContext(Dispatchers.IO) {
                source.getMangaDetails(manga.toSManga())
            }
            val info = result.toMangaInfo()
            Log.info { "Tsundoku[$name]: details: title=${info.title}" }
            info
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getMangaDetails failed", e)
            manga
        }
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        // Handle pre-fetched HTML via Command.Chapter.Fetch
        commands.filterIsInstance<Command.Chapter.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getChapterList using Fetch command HTML" }
                val doc = Ksoup.parse(cmd.html)
                // Extract chapters from HTML — look for link elements
                val chapterElements = doc.select("a[href], li a, .chapter-list a, .chapters a")
                return chapterElements.mapNotNull { el ->
                    val name = el.text().trim()
                    val url = el.attr("href").trim()
                    if (name.isBlank() || url.isBlank()) return@mapNotNull null
                    val fullUrl = if (!url.startsWith("http")) buildAbsoluteUrl(url) else url
                    ChapterInfo(
                        key = fullUrl,
                        name = name,
                        number = ChapterInfo.extractChapterNumber(name),
                        dateUpload = 0L,
                        scanlator = ""
                    )
                }.reversed()
            }
        }

        return try {
            Log.info { "Tsundoku[$name]: getChapterList(manga=${manga.title})" }
            val result = withContext(Dispatchers.IO) {
                source.getChapterList(manga.toSManga())
            }
            Log.info { "Tsundoku[$name]: got ${result.size} chapters" }
            if (result.isNotEmpty()) {
                Log.info { "Tsundoku[$name]: first: name=${result.first().name}, url=${result.first().url}" }
            }
            result.map { it.toChapterInfo() }.reversed()
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getChapterList failed", e)
            emptyList()
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        // Handle pre-fetched HTML via Command.Content.Fetch
        commands.filterIsInstance<Command.Content.Fetch>().firstOrNull()?.let { cmd ->
            if (cmd.html.isNotBlank()) {
                Log.info { "Tsundoku[$name]: getPageList using Fetch command HTML" }
                return parseNovelContent(cmd.html)
            }
        }

        return try {
            Log.info { "Tsundoku[$name]: getPageList(chapter=${chapter.name}, key=${chapter.key})" }
            val result = withContext(Dispatchers.IO) {
                source.getPageList(chapter.toSChapter())
            }
            Log.info { "Tsundoku[$name]: got ${result.size} pages" }

            // Novel sources return URL-only pages — fetch text via fetchPageText
            val isNovel = try { source.isNovelSource } catch (_: Exception) { false }
            if (isNovel && result.isNotEmpty()) {
                Log.info { "Tsundoku[$name]: Novel source, fetching page text..." }
                val html = withContext(Dispatchers.IO) {
                    source.fetchPageText(result.first())
                }
                Log.info { "Tsundoku[$name]: fetchPageText returned ${html.length} chars" }
                if (html.isNotBlank()) {
                    return parseNovelContent(html)
                }
                return emptyList()
            }

            val mapped = result.map { it.toPage() }
            Log.info { "Tsundoku[$name]: mapped ${mapped.size} pages" }
            mapped
        } catch (e: Exception) {
            Log.error("Tsundoku[$name]: getPageList failed", e)
            emptyList()
        }
    }


    // ==================== Model Conversions ====================

    private fun MangaInfo.toSManga(): SManga {
        val url = this.key
        val relativeUrl = if (baseUrl.isNotBlank() && url.startsWith(baseUrl)) url.removePrefix(baseUrl) else url
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
        } else url
        return MangaInfo(
            key = fullUrl,
            title = try { this.title } catch (_: UninitializedPropertyAccessException) { "" },
            artist = try { this.artist } catch (_: UninitializedPropertyAccessException) { null } ?: "",
            author = try { this.author } catch (_: UninitializedPropertyAccessException) { null } ?: "",
            description = try { this.description } catch (_: UninitializedPropertyAccessException) { null } ?: "",
            genres = try { this.getGenres() } catch (_: UninitializedPropertyAccessException) { null } ?: emptyList(),
            status = try { this.status.toLong() } catch (_: UninitializedPropertyAccessException) { 0L },
            cover = try { this.thumbnail_url } catch (_: UninitializedPropertyAccessException) { null } ?: ""
        )
    }

    private fun ChapterInfo.toSChapter(): SChapter {
        val url = this.key
        val relativeUrl = if (baseUrl.isNotBlank() && url.startsWith(baseUrl)) url.removePrefix(baseUrl) else url
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
        } else url
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
