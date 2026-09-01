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
}
