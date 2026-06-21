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
        val conflictType = ConflictType.HISTORY
        val localData = createTestHistorySyncData(lastRead = 1000L)
        val remoteData = createTestHistorySyncData(lastRead = 2000L)
        val conflictField = "lastRead"

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
                conflictType = ConflictType.HISTORY,
                localData = createTestHistorySyncData(),
                remoteData = createTestHistorySyncData(),
                conflictField = ""
            )
        }
    }

    @Test
    fun `ConflictType should have all required types`() {
        // Arrange & Act & Assert
        assertTrue(ConflictType.values().contains(ConflictType.HISTORY))
        assertTrue(ConflictType.values().contains(ConflictType.CHAPTER))
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

    private fun createTestHistorySyncData(lastRead: Long = System.currentTimeMillis()): HistorySyncData {
        return HistorySyncData(
            chapterGlobalId = "source-1|chapter-key-123",
            lastRead = lastRead,
            timeRead = 60000L,
            readingProgress = 0.75
        )
    }
}
