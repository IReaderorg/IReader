package ireader.presentation.ui.reader

/**
 * Helpers for persisting paged-reader position in [ireader.domain.models.entities.Chapter.lastPageRead].
 *
 * Continuous mode stores a paragraph index there; page mode stores the 1-based
 * content page index. A value of 0 means "no saved position".
 */

/** Convert a 1-based content page index into the value persisted in lastPageRead. */
fun pagedPageToScrollPosition(page: Int): Long = page.toLong().coerceAtLeast(0L)

/** Convert a persisted lastPageRead value into a 1-based content page index. */
fun scrollPositionToPagedPage(position: Long): Int =
    position.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

/**
 * Resolve the HorizontalPager page index for a chapter.
 *
 * @param savedPage 1-based content page restored from lastPageRead (0 = no saved position)
 * @param startPageOffset 1 when a previous-chapter transition page precedes the content
 * @param totalContentPages number of content pages in the current layout
 * @param scrollToEnd true when navigating backwards to a chapter (land on its last page)
 */
fun resolvePagedInitialPage(
    savedPage: Int,
    startPageOffset: Int,
    totalContentPages: Int,
    scrollToEnd: Boolean,
): Int {
    if (totalContentPages <= 0) return 0
    val lastContentPage = startPageOffset + totalContentPages - 1
    if (scrollToEnd) return lastContentPage
    val page = savedPage.coerceIn(1, totalContentPages)
    return (startPageOffset + page - 1).coerceIn(0, lastContentPage)
}
