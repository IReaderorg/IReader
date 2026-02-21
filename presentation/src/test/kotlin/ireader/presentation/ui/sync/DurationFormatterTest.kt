package ireader.presentation.ui.sync

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for duration formatting logic.
 * 
 * These tests verify that durations are formatted correctly in human-readable format.
 */
class DurationFormatterTest {

    @Test
    fun `formatDuration should format seconds only`() {
        assertEquals("5s", formatDuration(5000L))
        assertEquals("30s", formatDuration(30000L))
        assertEquals("59s", formatDuration(59000L))
    }

    @Test
    fun `formatDuration should format minutes and seconds`() {
        assertEquals("1m 5s", formatDuration(65000L))
        assertEquals("2m 5s", formatDuration(125000L))
        assertEquals("5m 30s", formatDuration(330000L))
    }

    @Test
    fun `formatDuration should format minutes only when no remaining seconds`() {
        assertEquals("1m", formatDuration(60000L))
        assertEquals("5m", formatDuration(300000L))
        assertEquals("10m", formatDuration(600000L))
    }

    @Test
    fun `formatDuration should format hours and minutes`() {
        assertEquals("1h 1m", formatDuration(3660000L))
        assertEquals("2h 30m", formatDuration(9000000L))
    }

    @Test
    fun `formatDuration should format hours only when no remaining minutes`() {
        assertEquals("1h", formatDuration(3600000L))
        assertEquals("2h", formatDuration(7200000L))
    }

    @Test
    fun `formatDuration should handle zero duration`() {
        assertEquals("0s", formatDuration(0L))
    }

    @Test
    fun `formatDuration should handle very short durations`() {
        assertEquals("0s", formatDuration(500L)) // Less than 1 second
        assertEquals("1s", formatDuration(1500L)) // 1.5 seconds rounds to 1
    }

    /**
     * Helper function that mimics the private formatDuration method in SyncForegroundService.
     * This allows us to test the logic without making the method public.
     */
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> {
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "${hours}h ${remainingMinutes}m"
                } else {
                    "${hours}h"
                }
            }
            minutes > 0 -> {
                val remainingSeconds = seconds % 60
                if (remainingSeconds > 0) {
                    "${minutes}m ${remainingSeconds}s"
                } else {
                    "${minutes}m"
                }
            }
            else -> "${seconds}s"
        }
    }
}
