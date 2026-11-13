package ireader.domain.usecases.remote

import ireader.domain.models.entities.SearchResultItem
import ireader.domain.models.entities.SourceSearchResult
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSearchOrderTest {

    @Test
    fun `most used sources first, sources missing repeatedly last, ties keep order`() {
        val order = orderGlobalSearchSources(
            sourceIds = listOf(1L, 2L, 3L, 4L, 5L),
            libraryBooksPerSource = mapOf(2L to 1, 3L to 10, 4L to 50),
            misses = mapOf(4L to GLOBAL_SEARCH_MISS_LIMIT, 1L to 1)
        )
        // 4 has the most books but kept missing, so it goes last; 5 (no misses) before 1 (one miss)
        assertEquals(listOf(3L, 2L, 5L, 1L, 4L), order)
    }

    @Test
    fun `a hit clears misses, empty or failed results add one up to the limit`() {
        val hit = SourceSearchResult(1L, "a", results = listOf(SearchResultItem(null, "Book", "", "", "", emptyList(), "k")))
        val empty = SourceSearchResult(2L, "b")
        val failed = SourceSearchResult(3L, "c", error = "timeout")

        val misses = updateGlobalSearchMisses(
            mapOf(1L to 2, 2L to GLOBAL_SEARCH_MISS_LIMIT),
            listOf(hit, empty, failed)
        )

        assertEquals(mapOf(2L to GLOBAL_SEARCH_MISS_LIMIT, 3L to 1), misses)
    }
}
