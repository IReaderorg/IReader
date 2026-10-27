package ireader.domain.usecases.remote

import ireader.core.source.model.Filter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SearchFiltersForTest {

    @Test
    fun `query goes into the source's own Search text filter and keeps its other filters`() {
        val search = Filter.Text("Search")
        val sort = Filter.Sort("Sort", arrayOf("Popular", "Latest"))
        val result = searchFiltersFor("overlord", listOf(sort, search))

        assertEquals(listOf<Filter<*>>(sort, search), result)
        assertEquals("overlord", search.value)
    }

    @Test
    fun `source title filter instance is reused`() {
        val title = Filter.Title()
        val result = searchFiltersFor("overlord", listOf(title))

        assertSame(title, result.single())
        assertEquals("overlord", title.value)
    }

    @Test
    fun `title filter is prepended when the source has none`() {
        val sort = Filter.Sort("Sort", arrayOf("Popular"))
        val result = searchFiltersFor("overlord", listOf(sort))

        assertEquals(2, result.size)
        assertEquals("overlord", (result.first() as Filter.Title).value)
        assertSame(sort, result[1])
    }
}
