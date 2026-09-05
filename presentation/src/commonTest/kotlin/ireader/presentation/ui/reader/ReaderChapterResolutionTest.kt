package ireader.presentation.ui.reader

import ireader.domain.services.processstate.ReaderProcessState
import ireader.i18n.LAST_CHAPTER
import ireader.i18n.NO_VALUE
import ireader.i18n.NULL_VALUE
import ireader.presentation.core.ui.resolveInitialReaderChapterId
import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderChapterResolutionTest {

    @Test
    fun `explicit chapter selection overrides restored state of previous chapter`() {
        // User read up to chapter 86, which is saved in restoredState
        val restoredState = ReaderProcessState(
            bookId = 1L,
            chapterId = 86L,
            scrollPosition = 120
        )

        // User goes back to chapter list and explicitly taps chapter 74
        val result = resolveInitialReaderChapterId(
            requestedChapterId = 74L,
            bookId = 1L,
            restoredState = restoredState
        )

        // Must open chapter 74, NOT chapter 86!
        assertEquals(74L, result)
    }

    @Test
    fun `explicit forward chapter selection overrides restored state`() {
        val restoredState = ReaderProcessState(
            bookId = 1L,
            chapterId = 86L
        )

        // User skips forward to chapter 150
        val result = resolveInitialReaderChapterId(
            requestedChapterId = 150L,
            bookId = 1L,
            restoredState = restoredState
        )

        assertEquals(150L, result)
    }

    @Test
    fun `resume reading with LAST_CHAPTER uses restored state chapter`() {
        val restoredState = ReaderProcessState(
            bookId = 1L,
            chapterId = 86L
        )

        // User taps Resume / Play button which passes LAST_CHAPTER
        val result = resolveInitialReaderChapterId(
            requestedChapterId = LAST_CHAPTER,
            bookId = 1L,
            restoredState = restoredState
        )

        // Resume should resume the last read chapter (86)
        assertEquals(86L, result)
    }

    @Test
    fun `resume reading with LAST_CHAPTER returns LAST_CHAPTER when no restored state exists`() {
        val result = resolveInitialReaderChapterId(
            requestedChapterId = LAST_CHAPTER,
            bookId = 1L,
            restoredState = null
        )

        assertEquals(LAST_CHAPTER, result)
    }

    @Test
    fun `resume reading with NO_VALUE uses restored state when available`() {
        val restoredState = ReaderProcessState(
            bookId = 1L,
            chapterId = 42L
        )

        val result = resolveInitialReaderChapterId(
            requestedChapterId = NO_VALUE,
            bookId = 1L,
            restoredState = restoredState
        )

        assertEquals(42L, result)
    }

    @Test
    fun `restored state from different book is ignored`() {
        val restoredState = ReaderProcessState(
            bookId = 99L,
            chapterId = 86L
        )

        val result = resolveInitialReaderChapterId(
            requestedChapterId = LAST_CHAPTER,
            bookId = 1L,
            restoredState = restoredState
        )

        assertEquals(LAST_CHAPTER, result)
    }
}
