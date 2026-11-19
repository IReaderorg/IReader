package ireader.presentation.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for paged-mode reading position persistence.
 *
 * Page mode stores the 1-based content page index in Chapter.lastPageRead so it
 * survives process death and chapter reopening, just like the paragraph index
 * does for continuous mode.
 */
class PagedReaderPositionTest {

    @Test
    fun `no saved position opens first content page`() {
        assertEquals(
            expected = 0,
            actual = resolvePagedInitialPage(
                savedPage = 0,
                startPageOffset = 0,
                totalContentPages = 10,
                scrollToEnd = false,
            ),
        )
        assertEquals(
            expected = 1,
            actual = resolvePagedInitialPage(
                savedPage = 0,
                startPageOffset = 1,
                totalContentPages = 10,
                scrollToEnd = false,
            ),
        )
    }

    @Test
    fun `saved page is restored accounting for previous chapter transition page`() {
        assertEquals(
            expected = 4,
            actual = resolvePagedInitialPage(
                savedPage = 5,
                startPageOffset = 0,
                totalContentPages = 10,
                scrollToEnd = false,
            ),
        )
        assertEquals(
            expected = 5,
            actual = resolvePagedInitialPage(
                savedPage = 5,
                startPageOffset = 1,
                totalContentPages = 10,
                scrollToEnd = false,
            ),
        )
    }

    @Test
    fun `saved page beyond current layout is clamped to last content page`() {
        assertEquals(
            expected = 6,
            actual = resolvePagedInitialPage(
                savedPage = 50,
                startPageOffset = 1,
                totalContentPages = 6,
                scrollToEnd = false,
            ),
        )
    }

    @Test
    fun `backwards chapter navigation overrides saved page and lands on last page`() {
        assertEquals(
            expected = 6,
            actual = resolvePagedInitialPage(
                savedPage = 2,
                startPageOffset = 1,
                totalContentPages = 6,
                scrollToEnd = true,
            ),
        )
    }

    @Test
    fun `empty content resolves safely to first pager page`() {
        assertEquals(
            expected = 0,
            actual = resolvePagedInitialPage(
                savedPage = 3,
                startPageOffset = 1,
                totalContentPages = 0,
                scrollToEnd = false,
            ),
        )
    }

    @Test
    fun `page index round trips through persisted scroll position`() {
        val page = 7
        val persisted = pagedPageToScrollPosition(page)
        assertEquals(expected = page.toLong(), actual = persisted)
        assertEquals(expected = page, actual = scrollPositionToPagedPage(persisted))
        assertEquals(expected = 0, actual = scrollPositionToPagedPage(-5L))
    }
}
