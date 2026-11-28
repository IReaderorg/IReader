package ireader.presentation.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderFooterFormattingTest {

    @Test
    fun pageFooterCombinesPageTotalAndPercent() {
        assertEquals("3 / 4 (75%)", formatPageProgress(3, 4))
    }

    @Test
    fun pageFooterHandlesMissingTotalPages() {
        assertEquals("0 / 0 (0%)", formatPageProgress(0, 0))
    }

    @Test
    fun batteryFooterShowsPercentOrNothing() {
        assertEquals("82%", formatBatteryLevel(82))
        assertEquals("", formatBatteryLevel(null))
    }

    @Test
    fun batteryIconLevelTracksCharge() {
        assertEquals(-1, batteryBarLevel(null))
        assertEquals(0, batteryBarLevel(0))
        assertEquals(1, batteryBarLevel(10))
        assertEquals(4, batteryBarLevel(50))
        assertEquals(6, batteryBarLevel(80))
        assertEquals(7, batteryBarLevel(100))
    }
}
