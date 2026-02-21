package ireader.domain.usecases.sync

import ireader.domain.models.sync.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetectConflictsUseCaseTest {

    @Test
    fun `invoke should return empty list when no conflicts exist`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(lastReadAt = 1000L)
        val remoteData = createSyncData(lastReadAt = 1000L)

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `invoke should detect reading progress conflict when timestamps differ`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(lastReadAt = 1000L, chapterIndex = 5)
        val remoteData = createSyncData(lastReadAt = 2000L, chapterIndex = 7)

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(1, conflicts.size)
        assertEquals(ConflictType.READING_PROGRESS, conflicts[0].conflictType)
    }

    @Test
    fun `invoke should not detect conflict when only timestamps differ but data is same`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(lastReadAt = 1000L, chapterIndex = 5)
        val remoteData = createSyncData(lastReadAt = 2000L, chapterIndex = 5)

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `invoke should detect bookmark conflict when positions differ`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = listOf(
                BookmarkData(1L, 123L, 456L, 1000, "Note 1", 1000L)
            ),
            metadata = createMetadata()
        )
        val remoteData = SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = listOf(
                BookmarkData(1L, 123L, 456L, 2000, "Note 1", 2000L)
            ),
            metadata = createMetadata()
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(1, conflicts.size)
        assertEquals(ConflictType.BOOKMARK, conflicts[0].conflictType)
    }

    @Test
    fun `invoke should detect book metadata conflict when update times differ`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = SyncData(
            books = listOf(
                BookSyncData(123L, "Book", "Author", null, "src", "url", 1000L, 1000L, "hash1")
            ),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = createMetadata()
        )
        val remoteData = SyncData(
            books = listOf(
                BookSyncData(123L, "Book Updated", "Author", null, "src", "url", 1000L, 2000L, "hash2")
            ),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = createMetadata()
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(1, conflicts.size)
        assertEquals(ConflictType.BOOK_METADATA, conflicts[0].conflictType)
    }

    @Test
    fun `invoke should detect multiple conflicts across different types`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = SyncData(
            books = listOf(
                BookSyncData(123L, "Book", "Author", null, "src", "url", 1000L, 1000L, "hash1")
            ),
            readingProgress = listOf(
                ReadingProgressData(123L, 456L, 5, 1024, 0.5f, 1000L)
            ),
            bookmarks = listOf(
                BookmarkData(1L, 123L, 456L, 1000, "Note", 1000L)
            ),
            metadata = createMetadata()
        )
        val remoteData = SyncData(
            books = listOf(
                BookSyncData(123L, "Book Updated", "Author", null, "src", "url", 1000L, 2000L, "hash2")
            ),
            readingProgress = listOf(
                ReadingProgressData(123L, 456L, 7, 2048, 0.7f, 2000L)
            ),
            bookmarks = listOf(
                BookmarkData(1L, 123L, 456L, 2000, "Note Updated", 2000L)
            ),
            metadata = createMetadata()
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(3, conflicts.size)
        assertTrue(conflicts.any { it.conflictType == ConflictType.BOOK_METADATA })
        assertTrue(conflicts.any { it.conflictType == ConflictType.READING_PROGRESS })
        assertTrue(conflicts.any { it.conflictType == ConflictType.BOOKMARK })
    }

    private fun createSyncData(
        lastReadAt: Long,
        chapterIndex: Int = 5
    ): SyncData {
        return SyncData(
            books = emptyList(),
            readingProgress = listOf(
                ReadingProgressData(
                    bookId = 123L,
                    chapterId = 456L,
                    chapterIndex = chapterIndex,
                    offset = 1024,
                    progress = 0.75f,
                    lastReadAt = lastReadAt
                )
            ),
            bookmarks = emptyList(),
            metadata = createMetadata()
        )
    }

    private fun createMetadata(): SyncMetadata {
        return SyncMetadata(
            deviceId = "test-device",
            timestamp = System.currentTimeMillis(),
            version = 1,
            checksum = "abc123"
        )
    }
}
