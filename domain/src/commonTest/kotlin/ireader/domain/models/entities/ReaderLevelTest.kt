package ireader.domain.models.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReaderLevelTest {

    @Test
    fun `level 1 starts at 0 hours`() {
        val level = ReaderLevel.fromMinutes(0)
        assertEquals(1, level.level)
        assertEquals("Novice Reader", level.title)
        assertEquals(0L, level.currentXp)
        assertEquals(2L, level.xpToNextLevel) // Needs 2 hours for level 2
        assertEquals(0f, level.progress)
    }

    @Test
    fun `level 2 reached at 2 hours`() {
        val level = ReaderLevel.fromMinutes(120) // 2 hours
        assertEquals(2, level.level)
        assertEquals("Novice Reader", level.title)
        assertEquals(0L, level.currentXp)
        assertEquals(8L, level.xpToNextLevel) // Needs 8 hours (10 - 2) for level 3
    }

    @Test
    fun `level 5 reached at 100 hours`() {
        val level = ReaderLevel.fromMinutes(100 * 60)
        assertEquals(5, level.level)
        assertEquals("Curious Reader", level.title)
        assertEquals(0L, level.currentXp)
        assertEquals(900L, level.xpToNextLevel) // Needs 900 hours (1000 - 100) for level 6
    }

    @Test
    fun `level 6 reached at 1000 hours`() {
        val level = ReaderLevel.fromMinutes(1000 * 60)
        assertEquals(6, level.level)
        assertEquals("Avid Reader", level.title)
        assertEquals(0L, level.currentXp)
        assertEquals(1500L, level.xpToNextLevel) // Needs 1500 hours (2500 - 1000) for level 7
    }

    @Test
    fun `xp progression within level 6 is accurate`() {
        val level = ReaderLevel.fromMinutes(1500 * 60) // 1500 hours = 500 hours into level 6
        assertEquals(6, level.level)
        assertEquals("Avid Reader", level.title)
        assertEquals(500L, level.currentXp)
        assertEquals(1500L, level.xpToNextLevel)
        assertTrue(level.progress > 0.33f && level.progress < 0.34f)
    }

    @Test
    fun `level 20 reached at 820000 hours`() {
        val level = ReaderLevel.fromMinutes(820000L * 60)
        assertEquals(20, level.level)
        assertEquals("Reading Deity", level.title)
    }
}
