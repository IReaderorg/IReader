package ireader.presentation.ui.reader

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PagedReaderWarmUpTest {

    @Test
    fun `layout is not ready before insets settle`() {
        assertFalse(
            pagedLayoutReady(stableForMs = 299, insetsPrimed = true, settleMs = 300),
        )
    }

    @Test
    fun `layout is not ready before insets are primed`() {
        assertFalse(
            pagedLayoutReady(stableForMs = 1_000, insetsPrimed = false, settleMs = 300),
        )
    }

    @Test
    fun `layout is ready once insets are primed and settled`() {
        assertTrue(
            pagedLayoutReady(stableForMs = 300, insetsPrimed = true, settleMs = 300),
        )
    }
}
