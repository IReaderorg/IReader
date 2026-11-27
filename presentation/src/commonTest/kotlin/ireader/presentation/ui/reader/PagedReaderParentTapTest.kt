package ireader.presentation.ui.reader

import ireader.domain.preferences.prefs.ReadingMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PagedReaderParentTapTest {

    @Test
    fun pagedReaderDoesNotToggleMenuFromParentTap() {
        assertFalse(shouldToggleReaderModeOnParentTap(ReadingMode.Page))
    }

    @Test
    fun continuousReaderKeepsParentTapToggle() {
        assertTrue(shouldToggleReaderModeOnParentTap(ReadingMode.Continues))
        assertTrue(shouldToggleReaderModeOnParentTap(ReadingMode.InfiniteScroll))
    }
}
