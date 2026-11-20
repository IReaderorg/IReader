package ireader.presentation.ui.reader

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PagedReaderLayoutTest {

    @Test
    fun `bottom system inset reduces usable paged content height`() {
        val withoutInset = pagedContentUsableHeightPx(
            screenHeightPx = 1920,
            topMarginDp = 12,
            bottomMarginDp = 12,
            topInsetDp = 0.dp,
            bottomInsetDp = 0.dp,
            density = 2f,
        )
        val withInset = pagedContentUsableHeightPx(
            screenHeightPx = 1920,
            topMarginDp = 12,
            bottomMarginDp = 12,
            topInsetDp = 0.dp,
            bottomInsetDp = 24.dp,
            density = 2f,
        )

        assertTrue(withInset < withoutInset)
        assertEquals(expected = 48, actual = withoutInset - withInset)
    }

    @Test
    fun `top system inset reduces usable paged content height`() {
        val withoutInset = pagedContentUsableHeightPx(
            screenHeightPx = 1920,
            topMarginDp = 12,
            bottomMarginDp = 12,
            topInsetDp = 0.dp,
            bottomInsetDp = 0.dp,
            density = 2f,
        )
        val withInset = pagedContentUsableHeightPx(
            screenHeightPx = 1920,
            topMarginDp = 12,
            bottomMarginDp = 12,
            topInsetDp = 28.dp,
            bottomInsetDp = 0.dp,
            density = 2f,
        )

        assertEquals(expected = 56, actual = withoutInset - withInset)
    }

    @Test
    fun `usable height never collapses below minimum`() {
        val usable = pagedContentUsableHeightPx(
            screenHeightPx = 100,
            topMarginDp = 12,
            bottomMarginDp = 12,
            topInsetDp = 28.dp,
            bottomInsetDp = 48.dp,
            density = 2f,
        )

        assertEquals(expected = 200, actual = usable)
    }
}
