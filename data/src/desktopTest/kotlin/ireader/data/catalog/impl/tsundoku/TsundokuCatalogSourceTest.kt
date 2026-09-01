package ireader.data.catalog.impl.tsundoku

import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import ireader.core.source.model.Filter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import eu.kanade.tachiyomi.source.model.Filter as TFilter

class TsundokuCatalogSourceTest {

    private class ConcreteText(name: String, state: String = "") : TFilter.Text(name, state)
    private class ConcreteCheckBox(name: String, state: Boolean = false) : TFilter.CheckBox(name, state)
    private class ConcreteTriState(name: String, state: Int = STATE_IGNORE) : TFilter.TriState(name, state)
    private class ConcreteSelect<V>(name: String, values: Array<V>, state: Int = 0) : TFilter.Select<V>(name, values, state)
    private class ConcreteSort(name: String, values: Array<String>, state: Selection? = null) : TFilter.Sort(name, values, state)
    private class ConcreteGroup<V>(name: String, state: List<V>) : TFilter.Group<V>(name, state)

    private class FakeCatalogueSource(
        override val supportsLatest: Boolean = true
    ) : CatalogueSource {
        override val id: Long = 1001L
        override val name: String = "Fake Source"
        override val lang: String = "en"

        var lastSearchQuery: String? = null
        var lastSearchFilters: FilterList? = null
        var lastSearchPage: Int? = null
        var popularCalled: Boolean = false
        var latestCalled: Boolean = false

        override fun getFilterList(): FilterList {
            return FilterList(
                TFilter.Header("Metadata"),
                ConcreteText("Author", ""),
                ConcreteCheckBox("Completed", false),
                ConcreteTriState("Action", TFilter.TriState.STATE_IGNORE),
                ConcreteSelect("Content Rating", arrayOf("All", "Safe", "Suggestive", "Erotica"), 0),
                ConcreteSort("Sort By", arrayOf("Title", "Popularity", "Latest"), null),
                ConcreteGroup("Genres", listOf(
                    ConcreteTriState("Comedy", TFilter.TriState.STATE_IGNORE),
                    ConcreteCheckBox("Drama", false)
                ))
            )
        }

        override suspend fun getPopularManga(page: Int): MangasPage {
            popularCalled = true
            return MangasPage(listOf(SManga.create().apply {
                url = "/manga/popular-1"
                title = "Popular Book 1"
            }), false)
        }

        override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage {
            lastSearchPage = page
            lastSearchQuery = query
            lastSearchFilters = filters
            return MangasPage(listOf(SManga.create().apply {
                url = "/manga/search-1"
                title = "Search Book 1"
            }), false)
        }

        override suspend fun getLatestUpdates(page: Int): MangasPage {
            latestCalled = true
            return MangasPage(listOf(SManga.create().apply {
                url = "/manga/latest-1"
                title = "Latest Book 1"
            }), false)
        }

        override suspend fun getMangaDetails(manga: SManga): SManga = manga
        override suspend fun getChapterList(manga: SManga): List<SChapter> = emptyList()
        override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
    }

    @Test
    fun testFilterConversion() {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val filters = catalogSource.getFilters()

        // First filter should always be Filter.Title("Search")
        val titleFilter = filters.firstOrNull() as? Filter.Title
        assertNotNull(titleFilter)
        assertEquals("Search", titleFilter.name)

        // Header -> Filter.Note
        val note = filters.find { it.name == "Metadata" } as? Filter.Note
        assertNotNull(note)

        // Text -> Filter.Text
        val author = filters.find { it.name == "Author" } as? Filter.Text
        assertNotNull(author)

        // CheckBox -> Filter.Check(allowsExclusion = false)
        val completed = filters.find { it.name == "Completed" } as? Filter.Check
        assertNotNull(completed)
        assertFalse(completed.allowsExclusion)
        assertFalse(completed.value == true)

        // TriState -> Filter.Check(allowsExclusion = true)
        val action = filters.find { it.name == "Action" } as? Filter.Check
        assertNotNull(action)
        assertTrue(action.allowsExclusion)
        assertNull(action.value)

        // Select -> Filter.Select
        val rating = filters.find { it.name == "Content Rating" } as? Filter.Select
        assertNotNull(rating)
        assertEquals(4, rating.options.size)
        assertEquals(0, rating.value)

        // Sort -> Filter.Sort
        val sort = filters.find { it.name == "Sort By" } as? Filter.Sort
        assertNotNull(sort)
        assertEquals(3, sort.options.size)

        // Group -> Filter.Group
        val genres = filters.find { it.name == "Genres" } as? Filter.Group
        assertNotNull(genres)
        assertEquals(2, genres.filters.size)
        val comedy = genres.filters.find { it.name == "Comedy" } as? Filter.Check
        assertNotNull(comedy)
        assertTrue(comedy.allowsExclusion)
    }

    @Test
    fun testSearchByQueryOnly() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val searchFilters = listOf(Filter.Title().apply { value = "Solo Leveling" })
        val result = catalogSource.getMangaList(searchFilters, 1)

        assertEquals(1, result.mangas.size)
        assertEquals("Solo Leveling", fakeSource.lastSearchQuery)
        assertEquals(1, fakeSource.lastSearchPage)
        assertNotNull(fakeSource.lastSearchFilters)
    }

    @Test
    fun testFilterSynchronizationPreservesStateWithoutLoss() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val filters = catalogSource.getFilters()

        // Mutate filters
        (filters.find { it.name == "Author" } as? Filter.Text)?.value = "Chugong"
        (filters.find { it.name == "Completed" } as? Filter.Check)?.value = true
        // Exclude Action (TriState: false -> STATE_EXCLUDE)
        (filters.find { it.name == "Action" } as? Filter.Check)?.value = false
        (filters.find { it.name == "Content Rating" } as? Filter.Select)?.value = 2
        (filters.find { it.name == "Sort By" } as? Filter.Sort)?.value = Filter.Sort.Selection(1, true)

        val genresGroup = filters.find { it.name == "Genres" } as? Filter.Group
        assertNotNull(genresGroup)
        // Include Comedy (TriState: true -> STATE_INCLUDE)
        (genresGroup.filters.find { it.name == "Comedy" } as? Filter.Check)?.value = true
        (genresGroup.filters.find { it.name == "Drama" } as? Filter.Check)?.value = true

        val result = catalogSource.getMangaList(filters, 1)
        assertEquals(1, result.mangas.size)

        val synced = fakeSource.lastSearchFilters
        assertNotNull(synced)

        // Verify synced state on the FilterList passed to getSearchManga
        val authorSynced = synced.find { it.name == "Author" } as? TFilter.Text
        assertNotNull(authorSynced)
        assertEquals("Chugong", authorSynced.state)

        val completedSynced = synced.find { it.name == "Completed" } as? TFilter.CheckBox
        assertNotNull(completedSynced)
        assertTrue(completedSynced.state)

        val actionSynced = synced.find { it.name == "Action" } as? TFilter.TriState
        assertNotNull(actionSynced)
        assertEquals(TFilter.TriState.STATE_EXCLUDE, actionSynced.state)

        val ratingSynced = synced.find { it.name == "Content Rating" } as? TFilter.Select<*>
        assertNotNull(ratingSynced)
        assertEquals(2, ratingSynced.state)

        val sortSynced = synced.find { it.name == "Sort By" } as? TFilter.Sort
        assertNotNull(sortSynced)
        assertEquals(1, sortSynced.state?.index)
        assertEquals(true, sortSynced.state?.ascending)

        val groupSynced = synced.find { it.name == "Genres" } as? TFilter.Group<*>
        assertNotNull(groupSynced)
        @Suppress("UNCHECKED_CAST")
        val groupChildren = groupSynced.state as List<TFilter<*>>
        val comedySynced = groupChildren.find { it.name == "Comedy" } as? TFilter.TriState
        assertNotNull(comedySynced)
        assertEquals(TFilter.TriState.STATE_INCLUDE, comedySynced.state)

        val dramaSynced = groupChildren.find { it.name == "Drama" } as? TFilter.CheckBox
        assertNotNull(dramaSynced)
        assertTrue(dramaSynced.state)
    }

    @Test
    fun testCombinedQueryAndFilters() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val filters = catalogSource.getFilters()
        // Set search title
        (filters.find { it is Filter.Title } as? Filter.Title)?.value = "Lord of the Mysteries"
        // Set completed checkbox
        (filters.find { it.name == "Completed" } as? Filter.Check)?.value = true

        catalogSource.getMangaList(filters, 1)

        assertEquals("Lord of the Mysteries", fakeSource.lastSearchQuery)
        val synced = fakeSource.lastSearchFilters
        assertNotNull(synced)
        val completedSynced = synced.find { it.name == "Completed" } as? TFilter.CheckBox
        assertNotNull(completedSynced)
        assertTrue(completedSynced.state)
    }

    @Test
    fun testEmptySearchFallbackToPopular() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        // Passing default filters without query
        val filters = catalogSource.getFilters()
        catalogSource.getMangaList(filters, 1)

        assertTrue(fakeSource.popularCalled, "Expected fallback to getPopularManga when query is empty and filters are default")
    }

    @Test
    fun testLatestUpdatesFallbackWhenNotSupported() = runTest {
        val fakeSource = FakeCatalogueSource(supportsLatest = false)
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val result = catalogSource.getMangaList(BaseTsundokuCatalogSource.LatestListing(), 1)

        assertEquals(1, result.mangas.size)
        assertTrue(fakeSource.popularCalled, "Expected fallback to popular when latest is unsupported")
        assertFalse(fakeSource.latestCalled)
    }

    @Test
    fun testCommandsAvailableForWebViewFetch() {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val commands = catalogSource.getCommands()
        assertTrue(commands.isNotEmpty(), "Tsundoku source must expose commands for WebView fetch")

        assertNotNull(commands.find { it is ireader.core.source.model.Command.Detail.Fetch }, "Command.Detail.Fetch missing")
        assertNotNull(commands.find { it is ireader.core.source.model.Command.Chapter.Fetch }, "Command.Chapter.Fetch missing")
        assertNotNull(commands.find { it is ireader.core.source.model.Command.Content.Fetch }, "Command.Content.Fetch missing")
        assertNotNull(commands.find { it is ireader.core.source.model.Command.Explore.Fetch }, "Command.Explore.Fetch missing")
        assertTrue(catalogSource.hasCommands(), "hasCommands() should be true")
    }

    @Test
    fun testDetailFetchFromWebViewHtml() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta property="og:title" content="Reverend Insanity" />
                <meta property="og:description" content="A demon's path through cultivation." />
                <meta property="og:image" content="https://example.com/cover.jpg" />
            </head>
            <body>
                <h1 class="novel-title">Reverend Insanity</h1>
                <div class="author">Gu Zhen Ren</div>
                <div class="novel-summary">A demon's path through cultivation.</div>
                <img class="cover" src="https://example.com/cover.jpg" />
            </body>
            </html>
        """.trimIndent()

        val manga = ireader.core.source.model.MangaInfo(key = "https://example.com/novel/1", title = "Old Title")
        val command = ireader.core.source.model.Command.Detail.Fetch(
            url = "https://example.com/novel/1",
            html = sampleHtml
        )

        val details = catalogSource.getMangaDetails(manga, listOf(command))

        assertEquals("Reverend Insanity", details.title)
        assertEquals("https://example.com/cover.jpg", details.cover)
        assertTrue(details.description.contains("demon's path"))
    }

    @Test
    fun testChapterListFetchFromWebViewHtml() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <ul class="chapter-list">
                    <li><a href="/novel/1/chapter-1">Chapter 1: The Beginning</a></li>
                    <li><a href="/novel/1/chapter-2">Chapter 2: The Heart of the Demon</a></li>
                    <li><a href="/novel/1/chapter-3">Chapter 3: Moonlight Gu</a></li>
                </ul>
            </body>
            </html>
        """.trimIndent()

        val manga = ireader.core.source.model.MangaInfo(key = "https://example.com/novel/1", title = "Reverend Insanity")
        val command = ireader.core.source.model.Command.Chapter.Fetch(
            url = "https://example.com/novel/1",
            html = sampleHtml
        )

        val chapters = catalogSource.getChapterList(manga, listOf(command))

        assertEquals(3, chapters.size)
        // Ascending order check
        assertEquals("Chapter 1: The Beginning", chapters[0].name)
        assertEquals(1f, chapters[0].number)
        assertEquals("Chapter 3: Moonlight Gu", chapters[2].name)
        assertEquals(3f, chapters[2].number)
    }

    @Test
    fun testContentFetchFromWebViewHtml() = runTest {
        val fakeSource = FakeCatalogueSource()
        val catalogSource = TsundokuCatalogSource(fakeSource)

        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <body>
                <div id="chapter-content">
                    <p>Humans are clever in tens of thousands of ways, Gu are the refined essences of heaven and earth.</p>
                    <p>The three realms are vast, mountains and rivers boundless.</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        val chapter = ireader.core.source.model.ChapterInfo(key = "https://example.com/novel/1/chapter-1", name = "Chapter 1")
        val command = ireader.core.source.model.Command.Content.Fetch(
            url = "https://example.com/novel/1/chapter-1",
            html = sampleHtml
        )

        val pages = catalogSource.getPageList(chapter, listOf(command))

        assertTrue(pages.isNotEmpty())
        val textPage = pages.first() as ireader.core.source.model.Text
        assertTrue(textPage.text.contains("Humans are clever"))
    }

    @Test
    fun testSafeIdGetterWhenSourceThrowsOrReturnsInvalid() {
        val throwingSource = object : CatalogueSource {
            override val id: Long get() = throw RuntimeException("Broken source.id")
            override val name: String get() = "Broken Source"
            override val lang: String get() = "en"
            override val supportsLatest: Boolean get() = false
            override fun getFilterList(): FilterList = FilterList()
            override suspend fun getPopularManga(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getLatestUpdates(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }

        val catalogSource = TsundokuCatalogSource(throwingSource)
        val id = catalogSource.id
        assertTrue(id > 0L, "Fallback id must be a positive Long")

        val invalidIdSource = object : CatalogueSource {
            override val id: Long get() = -1L
            override val name: String get() = "Invalid Source"
            override val lang: String get() = "en"
            override val supportsLatest: Boolean get() = false
            override fun getFilterList(): FilterList = FilterList()
            override suspend fun getPopularManga(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getLatestUpdates(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }

        val catalogSource2 = TsundokuCatalogSource(invalidIdSource)
        val id2 = catalogSource2.id
        assertTrue(id2 > 0L, "Fallback id for -1L must be a positive Long")
    }

    @Test
    fun testRateLimitedInterface() {
        val rateLimitedSource = object : CatalogueSource, eu.kanade.tachiyomi.source.RateLimited {
            override val id: Long = 2002L
            override val name: String = "Rate Limited Source"
            override val lang: String = "en"
            override val supportsLatest: Boolean = false
            override val minimumDelayMillis: Long = 1500L
            override fun getFilterList(): FilterList = FilterList()
            override suspend fun getPopularManga(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getLatestUpdates(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }

        assertTrue(rateLimitedSource is eu.kanade.tachiyomi.source.RateLimited)
        assertEquals(1500L, rateLimitedSource.minimumDelayMillis)
        assertEquals(1500L, rateLimitedSource.recommendedDelayMillis)
        assertEquals(1, rateLimitedSource.recommendedPermits)
    }

    @Test
    fun testMangaUpdateDelegationInTsundokuCatalogSource() = runTest {
        var mangaUpdateCalled = false
        val customSource = object : CatalogueSource {
            override val id: Long = 3003L
            override val name: String = "Update Test Source"
            override val lang: String = "en"
            override val supportsLatest: Boolean = false
            override fun getFilterList(): FilterList = FilterList()
            override suspend fun getPopularManga(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getLatestUpdates(page: Int): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage = MangasPage(emptyList(), false)
            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()

            override suspend fun getMangaUpdate(
                manga: SManga,
                chapters: List<SChapter>,
                fetchDetails: Boolean,
                fetchChapters: Boolean,
            ): eu.kanade.tachiyomi.source.model.SMangaUpdate {
                mangaUpdateCalled = true
                val updatedManga = manga.apply {
                    title = "Overridden Title"
                    author = "Overridden Author"
                }
                val updatedChapters = listOf(
                    SChapter.create().apply {
                        name = "Chapter 1 Overridden"
                        url = "/ch1"
                    }
                )
                return eu.kanade.tachiyomi.source.model.SMangaUpdate(updatedManga, updatedChapters)
            }
        }

        val catalogSource = TsundokuCatalogSource(customSource)
        val mangaInfo = ireader.core.source.model.MangaInfo(key = "/novel/test", title = "Original")
        val details = catalogSource.getMangaDetails(mangaInfo, emptyList())
        assertTrue(mangaUpdateCalled, "getMangaUpdate should have been called for getMangaDetails")
        assertEquals("Overridden Title", details.title)
        assertEquals("Overridden Author", details.author)

        mangaUpdateCalled = false
        val chapters = catalogSource.getChapterList(mangaInfo, emptyList())
        assertTrue(mangaUpdateCalled, "getMangaUpdate should have been called for getChapterList")
        assertEquals(1, chapters.size)
        assertEquals("Chapter 1 Overridden", chapters.first().name)
    }

    @Test
    fun testInspectStorySeedlingApk() {
        val apkFile = java.io.File("../.gradle/tachiyomi-en.storyseedling-v1.6.3.apk")
        if (!apkFile.exists()) {
            val altFile = java.io.File(".gradle/tachiyomi-en.storyseedling-v1.6.3.apk")
            if (!altFile.exists()) {
                println("APK not found at $apkFile or $altFile")
                return
            }
        }
        val fileToUse = if (apkFile.exists()) apkFile else java.io.File(".gradle/tachiyomi-en.storyseedling-v1.6.3.apk")
        val apk = net.dongliu.apk.parser.ApkFile(fileToUse)
        println("=== APK MANIFEST ===")
        println(apk.manifestXml)
        println("=== APK META ===")
        println("packageName: ${apk.apkMeta.packageName}")
        println("name: ${apk.apkMeta.name}")
        println("versionName: ${apk.apkMeta.versionName}")
        println("versionCode: ${apk.apkMeta.versionCode}")
        println("features: ${apk.apkMeta.usesFeatures.map { it.name }}")

        val validated = DesktopTsundokuExtensionLoader.validateMetadata(apk.apkMeta.packageName, apk)
        println("=== VALIDATED DATA ===")
        println(validated)

        val sources = DesktopTsundokuExtensionLoader.loadSources(apk.apkMeta.packageName, fileToUse, validated!!)
        println("=== LOADED SOURCES ===")
        println("count: ${sources.size}")
        sources.forEach {
            println("source: ${it.name}, id: ${it.id}")
        }
        apk.close()

        assertTrue(sources.isNotEmpty(), "Expected at least 1 source to be loaded from StorySeedling")
        assertEquals("StorySeedling", sources.first().name)

        val listings = (sources.first() as ireader.core.source.CatalogSource).getListings()
        println("listings: ${listings.map { it.name }}")
        assertTrue(listings.isNotEmpty())

        val tsundokuSource = sources.first() as TsundokuCatalogSource
        val rawSource = tsundokuSource.source
        println("rawSource class: ${rawSource.javaClass.name}")
        val httpSource = rawSource as? eu.kanade.tachiyomi.source.online.HttpSource
        println("httpSource baseUrl: ${httpSource?.baseUrl}")
        val dummySManga = eu.kanade.tachiyomi.source.model.SManga.create().apply {
            url = "blood-warlock-succubus-partner-in-the-apocalypse"
        }
        val mangaUrl = httpSource?.getMangaUrl(dummySManga)
        println("getMangaUrl for slug: $mangaUrl")
        assertEquals("https://storyseedling.com/series/blood-warlock-succubus-partner-in-the-apocalypse", mangaUrl)

        val fullSManga = eu.kanade.tachiyomi.source.model.SManga.create().apply {
            url = "/series/blood-warlock-succubus-partner-in-the-apocalypse"
        }
        val fullMangaUrl = httpSource?.getMangaUrl(fullSManga)
        println("getMangaUrl for /series/...: $fullMangaUrl")
        assertEquals("https://storyseedling.com/series/blood-warlock-succubus-partner-in-the-apocalypse", fullMangaUrl)

        val coverReq = tsundokuSource.getCoverRequest("https://example.com/cover.jpg")
        assertNotNull(coverReq.first)
        assertNotNull(coverReq.second)
        println("User-Agent from tsundokuSource: ${tsundokuSource.userAgent}")
        assertTrue(tsundokuSource.userAgent.isNotBlank())
    }
}
