package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DataConflictTest {

    @Test
    fun `DataConflict should be created with valid data`() {
        // Arrange
        val conflictType = ConflictType.READING_PROGRESS
        val localData = createTestReadingProgressData(lastReadAt = 1000L)
        val remoteData = createTestReadingProgressData(lastReadAt = 2000L)
        val conflictField = "chapterIndex"

        // Act
        val conflict = DataConflict(
            conflictType = conflictType,
            localData = localData,
            remoteData = remoteData,
            conflictField = conflictField
        )

        // Assert
        assertEquals(conflictType, conflict.conflictType)
        assertEquals(localData, conflict.localData)
        assertEquals(remoteData, conflict.remoteData)
        assertEquals(conflictField, conflict.conflictField)
    }

    @Test
    fun `DataConflict should reject empty conflictField`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DataConflict(
                conflictType = ConflictType.READING_PROGRESS,
                localData = createTestReadingProgressData(),
                remoteData = createTestReadingProgressData(),
                conflictField = ""
            )
        }
    }

    @Test
    fun `ConflictType should have all required types`() {
        // Arrange & Act & Assert
        assertTrue(ConflictType.values().contains(ConflictType.READING_PROGRESS))
        assertTrue(ConflictType.values().contains(ConflictType.BOOKMARK))
        assertTrue(ConflictType.values().contains(ConflictType.BOOK_METADATA))
    }

    @Test
    fun `ConflictResolutionStrategy should have all required strategies`() {
        // Arrange & Act & Assert
        assertTrue(ConflictResolutionStrategy.values().contains(ConflictResolutionStrategy.LATEST_TIMESTAMP))
        assertTrue(ConflictResolutionStrategy.values().contains(ConflictResolutionStrategy.LOCAL_WINS))
        assertTrue(ConflictResolutionStrategy.values().contains(ConflictResolutionStrategy.REMOTE_WINS))
        assertTrue(ConflictResolutionStrategy.values().contains(ConflictResolutionStrategy.MERGE))
        assertTrue(ConflictResolutionStrategy.values().contains(ConflictResolutionStrategy.MANUAL))
    }

    private fun createTestReadingProgressData(lastReadAt: Long = System.currentTimeMillis()): ReadingProgressData {
        return ReadingProgressData(
            bookId = 123L,
            chapterId = 456L,
            chapterIndex = 5,
            offset = 1024,
            progress = 0.75f,
            lastReadAt = lastReadAt
        )
    }
}
