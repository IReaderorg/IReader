package ireader.presentation.ui.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChapterNameDisplayTest {

    private val longName =
        "Chapter 127: The Emperor's Hidden Library and the Forgotten Map of the Northern Isles"

    @Test
    fun `chapter name is kept in full`() {
        val display = chapterNameForDisplay(longName)

        assertEquals(longName, display.text)
        assertTrue(!display.text.contains("..."))
    }

    @Test
    fun `chapter name can wrap over multiple lines`() {
        assertTrue(chapterNameForDisplay(longName).maxLines > 1)
    }

    @Test
    fun `previous chapter title keeps a long name and has a fallback`() {
        assertEquals(longName, previousChapterTitle(longName).text)
        assertEquals("Previous Chapter", previousChapterTitle(null).text)
        assertTrue(previousChapterTitle(longName).maxLines > 1)
    }
}
