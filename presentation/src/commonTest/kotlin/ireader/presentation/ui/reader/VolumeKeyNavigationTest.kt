package ireader.presentation.ui.reader

import ireader.domain.preferences.prefs.ReadingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class VolumeKeyNavigationTest {

    @Test
    fun `Page mode volume down emits PAGE_NEXT`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Page,
        )
        assertEquals(VolumeKeyAction.PAGE_NEXT, action)
    }

    @Test
    fun `Page mode volume up emits PAGE_PREV`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = true,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Page,
        )
        assertEquals(VolumeKeyAction.PAGE_PREV, action)
    }

    @Test
    fun `Page mode respects inverted volume keys`() {
        // When inverted, Volume Up advances page
        val actionUp = determineVolumeKeyAction(
            isVolumeUp = true,
            volumeKeyInverted = true,
            readingMode = ReadingMode.Page,
        )
        assertEquals(VolumeKeyAction.PAGE_NEXT, actionUp)

        // When inverted, Volume Down goes to previous page
        val actionDown = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = true,
            readingMode = ReadingMode.Page,
        )
        assertEquals(VolumeKeyAction.PAGE_PREV, actionDown)
    }

    @Test
    fun `Continuous mode scrolls forward when canScrollForward is true`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Continues,
            canScrollForward = true,
            canScrollBackward = true
        )
        assertEquals(VolumeKeyAction.SCROLL_FORWARD, action)
    }

    @Test
    fun `Continuous mode advances chapter when canScrollForward is false`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Continues,
            canScrollForward = false,
            canScrollBackward = true
        )
        assertEquals(VolumeKeyAction.NEXT_CHAPTER, action)
    }

    @Test
    fun `Continuous mode scrolls backward when canScrollBackward is true`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = true,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Continues,
            canScrollForward = true,
            canScrollBackward = true
        )
        assertEquals(VolumeKeyAction.SCROLL_BACKWARD, action)
    }

    @Test
    fun `Continuous mode retreats chapter when canScrollBackward is false`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = true,
            volumeKeyInverted = false,
            readingMode = ReadingMode.Continues,
            canScrollForward = true,
            canScrollBackward = false
        )
        assertEquals(VolumeKeyAction.PREV_CHAPTER, action)
    }

    @Test
    fun `InfiniteScroll mode scrolls forward when canScrollForward is true`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = false,
            readingMode = ReadingMode.InfiniteScroll,
            canScrollForward = true,
            canScrollBackward = false
        )
        assertEquals(VolumeKeyAction.SCROLL_FORWARD, action)
    }

    @Test
    fun `InfiniteScroll mode returns NONE at bottom because chapters concatenate`() {
        val action = determineVolumeKeyAction(
            isVolumeUp = false,
            volumeKeyInverted = false,
            readingMode = ReadingMode.InfiniteScroll,
            canScrollForward = false,
            canScrollBackward = true
        )
        assertEquals(VolumeKeyAction.NONE, action)
    }
}
