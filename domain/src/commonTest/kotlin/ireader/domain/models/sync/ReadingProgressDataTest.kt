package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadingProgressDataTest {

    @Test
    fun `HistorySyncData should be created with valid data`() {
        // Arrange
        val chapterGlobalId = "source-1|chapter-key-123"
        val lastRead = System.currentTimeMillis()
        val timeRead = 60000L
        val readingProgress = 0.75

        // Act
        val historyData = HistorySyncData(
            chapterGlobalId = chapterGlobalId,
            lastRead = lastRead,
            timeRead = timeRead,
            readingProgress = readingProgress
        )

        // Assert
        assertEquals(chapterGlobalId, historyData.chapterGlobalId)
        assertEquals(lastRead, historyData.lastRead)
        assertEquals(timeRead, historyData.timeRead)
        assertEquals(readingProgress, historyData.readingProgress)
    }

    @Test
    fun `HistorySyncData should reject blank chapterGlobalId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            HistorySyncData(
                chapterGlobalId = "",
                lastRead = System.currentTimeMillis(),
                timeRead = 60000L,
                readingProgress = 0.75
            )
        }
    }

    @Test
    fun `HistorySyncData should reject negative lastRead`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            HistorySyncData(
                chapterGlobalId = "source-1|chapter-key",
                lastRead = -1L,
                timeRead = 60000L,
                readingProgress = 0.75
            )
        }
    }

    @Test
    fun `HistorySyncData should reject negative timeRead`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            HistorySyncData(
                chapterGlobalId = "source-1|chapter-key",
                lastRead = System.currentTimeMillis(),
                timeRead = -1L,
                readingProgress = 0.75
            )
        }
    }

    @Test
    fun `HistorySyncData should reject readingProgress below 0`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            HistorySyncData(
                chapterGlobalId = "source-1|chapter-key",
                lastRead = System.currentTimeMillis(),
                timeRead = 60000L,
                readingProgress = -0.1
            )
        }
    }

    @Test
    fun `HistorySyncData should reject readingProgress above 1`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            HistorySyncData(
                chapterGlobalId = "source-1|chapter-key",
                lastRead = System.currentTimeMillis(),
                timeRead = 60000L,
                readingProgress = 1.1
            )
        }
    }

    @Test
    fun `HistorySyncData should accept readingProgress of 0`() {
        // Arrange & Act
        val historyData = HistorySyncData(
            chapterGlobalId = "source-1|chapter-key",
            lastRead = System.currentTimeMillis(),
            timeRead = 0L,
            readingProgress = 0.0
        )

        // Assert
        assertEquals(0.0, historyData.readingProgress)
    }

    @Test
    fun `HistorySyncData should accept readingProgress of 1`() {
        // Arrange & Act
        val historyData = HistorySyncData(
            chapterGlobalId = "source-1|chapter-key",
            lastRead = System.currentTimeMillis(),
            timeRead = 60000L,
            readingProgress = 1.0
        )

        // Assert
        assertEquals(1.0, historyData.readingProgress)
    }

    @Test
    fun `HistorySyncData should accept zero timeRead`() {
        // Arrange & Act
        val historyData = HistorySyncData(
            chapterGlobalId = "source-1|chapter-key",
            lastRead = System.currentTimeMillis(),
            timeRead = 0L,
            readingProgress = 0.0
        )

        // Assert
        assertEquals(0L, historyData.timeRead)
    }

    @Test
    fun `HistorySyncData should accept zero lastRead`() {
        // Arrange & Act
        val historyData = HistorySyncData(
            chapterGlobalId = "source-1|chapter-key",
            lastRead = 0L,
            timeRead = 0L,
            readingProgress = 0.0
        )

        // Assert
        assertEquals(0L, historyData.lastRead)
    }
}
