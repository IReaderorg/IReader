package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for History entity
 */
class HistoryTest {

    // ==================== History Creation Tests ====================

    @Test
    fun `History with all fields`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 3600L,
            progress = 0.75f
        )
        
        assertEquals(1L, history.id)
        assertEquals(100L, history.chapterId)
        assertEquals(1234567890L, history.readAt)
        assertEquals(3600L, history.readDuration)
        assertEquals(0.75f, history.progress)
    }

    @Test
    fun `History with null readAt`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = null,
            readDuration = 0L
        )
        
        assertNull(history.readAt)
    }

    @Test
    fun `History default progress is zero`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L
        )
        
        assertEquals(0f, history.progress)
    }

    // ==================== Progress Tests ====================

    @Test
    fun `History progress at start`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 0f
        )
        
        assertEquals(0f, history.progress)
    }

    @Test
    fun `History progress at middle`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 0.5f
        )
        
        assertEquals(0.5f, history.progress)
    }

    @Test
    fun `History progress at end`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 1f
        )
        
        assertEquals(1f, history.progress)
    }

    // ==================== Read Duration Tests ====================

    @Test
    fun `History with zero read duration`() {
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 0L
        )
        
        assertEquals(0L, history.readDuration)
    }

    @Test
    fun `History with long read duration`() {
        val oneHourInSeconds = 3600L
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = oneHourInSeconds
        )
        
        assertEquals(oneHourInSeconds, history.readDuration)
    }

    // ==================== Equality Tests ====================

    @Test
    fun `History equality with same values`() {
        val history1 = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 0.5f
        )
        val history2 = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 0.5f
        )
        
        assertEquals(history1, history2)
    }

    @Test
    fun `History inequality with different id`() {
        val history1 = History(id = 1L, chapterId = 100L, readAt = null, readDuration = 0L)
        val history2 = History(id = 2L, chapterId = 100L, readAt = null, readDuration = 0L)
        
        assertNotEquals(history1, history2)
    }

    @Test
    fun `History inequality with different chapterId`() {
        val history1 = History(id = 1L, chapterId = 100L, readAt = null, readDuration = 0L)
        val history2 = History(id = 1L, chapterId = 200L, readAt = null, readDuration = 0L)
        
        assertNotEquals(history1, history2)
    }

    // ==================== Copy Tests ====================

    @Test
    fun `History copy creates new instance`() {
        val original = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L,
            progress = 0.5f
        )
        
        val copy = original.copy(progress = 0.75f)
        
        assertEquals(0.5f, original.progress)
        assertEquals(0.75f, copy.progress)
        assertEquals(original.id, copy.id)
        assertEquals(original.chapterId, copy.chapterId)
    }

    @Test
    fun `History copy with updated readDuration`() {
        val original = History(
            id = 1L,
            chapterId = 100L,
            readAt = 1234567890L,
            readDuration = 100L
        )
        
        val copy = original.copy(readDuration = 200L)
        
        assertEquals(100L, original.readDuration)
        assertEquals(200L, copy.readDuration)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `History with maximum Long values`() {
        val history = History(
            id = Long.MAX_VALUE,
            chapterId = Long.MAX_VALUE,
            readAt = Long.MAX_VALUE,
            readDuration = Long.MAX_VALUE
        )
        
        assertEquals(Long.MAX_VALUE, history.id)
        assertEquals(Long.MAX_VALUE, history.chapterId)
        assertEquals(Long.MAX_VALUE, history.readAt)
        assertEquals(Long.MAX_VALUE, history.readDuration)
    }

    @Test
    fun `History with minimum Long values`() {
        val history = History(
            id = Long.MIN_VALUE,
            chapterId = Long.MIN_VALUE,
            readAt = Long.MIN_VALUE,
            readDuration = Long.MIN_VALUE
        )
        
        assertEquals(Long.MIN_VALUE, history.id)
    }

    @Test
    fun `History progress can exceed 1`() {
        // This tests that the model doesn't enforce bounds
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = null,
            readDuration = 0L,
            progress = 1.5f
        )
        
        assertEquals(1.5f, history.progress)
    }

    @Test
    fun `History progress can be negative`() {
        // This tests that the model doesn't enforce bounds
        val history = History(
            id = 1L,
            chapterId = 100L,
            readAt = null,
            readDuration = 0L,
            progress = -0.5f
        )
        
        assertEquals(-0.5f, history.progress)
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `calculate total reading time from history list`() {
        val histories = listOf(
            History(id = 1L, chapterId = 1L, readAt = null, readDuration = 300L),
            History(id = 2L, chapterId = 2L, readAt = null, readDuration = 600L),
            History(id = 3L, chapterId = 3L, readAt = null, readDuration = 450L)
        )
        
        val totalDuration = histories.sumOf { it.readDuration }
        
        assertEquals(1350L, totalDuration)
    }

    @Test
    fun `filter completed chapters from history`() {
        val histories = listOf(
            History(id = 1L, chapterId = 1L, readAt = null, readDuration = 100L, progress = 1f),
            History(id = 2L, chapterId = 2L, readAt = null, readDuration = 100L, progress = 0.5f),
            History(id = 3L, chapterId = 3L, readAt = null, readDuration = 100L, progress = 1f),
            History(id = 4L, chapterId = 4L, readAt = null, readDuration = 100L, progress = 0.25f)
        )
        
        val completed = histories.filter { it.progress >= 1f }
        
        assertEquals(2, completed.size)
    }

    @Test
    fun `sort history by readAt`() {
        val histories = listOf(
            History(id = 1L, chapterId = 1L, readAt = 300L, readDuration = 100L),
            History(id = 2L, chapterId = 2L, readAt = 100L, readDuration = 100L),
            History(id = 3L, chapterId = 3L, readAt = 200L, readDuration = 100L)
        )
        
        val sorted = histories.sortedBy { it.readAt }
        
        assertEquals(100L, sorted[0].readAt)
        assertEquals(200L, sorted[1].readAt)
        assertEquals(300L, sorted[2].readAt)
    }

    @Test
    fun `find most recent history entry`() {
        val histories = listOf(
            History(id = 1L, chapterId = 1L, readAt = 1000L, readDuration = 100L),
            History(id = 2L, chapterId = 2L, readAt = 3000L, readDuration = 100L),
            History(id = 3L, chapterId = 3L, readAt = 2000L, readDuration = 100L)
        )
        
        val mostRecent = histories.maxByOrNull { it.readAt ?: 0L }
        
        assertEquals(2L, mostRecent?.id)
        assertEquals(3000L, mostRecent?.readAt)
    }
}
