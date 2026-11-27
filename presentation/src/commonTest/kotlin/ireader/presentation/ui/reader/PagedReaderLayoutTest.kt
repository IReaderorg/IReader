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
    fun `immersive paged layout has no top or bottom system padding`() {
        assertEquals(
            PagedLayoutInsets(0.dp, 0.dp),
            pagedLayoutInsets(
                immersiveMode = true,
                liveTop = 28.dp,
                liveBottom = 24.dp,
            ),
        )
    }

    @Test
    fun `non immersive paged layout keeps live system bar insets`() {
        val live = PagedLayoutInsets(28.dp, 24.dp)

        assertEquals(
            live,
            pagedLayoutInsets(
                immersiveMode = false,
                liveTop = live.top,
                liveBottom = live.bottom,
            ),
        )
    }

    @Test
    fun `font metric change triggers repagination only for real changes`() {
        assertTrue(fontMetricsKeyChanged(previous = 0f, current = 42f))
        assertTrue(fontMetricsKeyChanged(previous = 42f, current = 38f))
        assertTrue(fontMetricsKeyChanged(previous = 42f, current = 42.2f).not())
    }

    @Test
    fun `footer stays pinned to bottom of page slot`() {
        assertEquals(expected = 960, actual = pagedFooterTop(1000, 40))
        assertEquals(expected = 0, actual = pagedFooterTop(100, 140))
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
