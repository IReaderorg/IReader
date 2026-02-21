package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncDataTest {

    @Test
    fun `SyncData should be created with valid data`() {
        // Arrange
        val books = listOf(createTestBookSyncData())
        val readingProgress = listOf(createTestReadingProgressData())
        val bookmarks = listOf(createTestBookmarkData())
        val metadata = createTestSyncMetadata()

        // Act
        val syncData = SyncData(
            books = books,
            readingProgress = readingProgress,
            bookmarks = bookmarks,
            metadata = metadata
        )

        // Assert
        assertEquals(books, syncData.books)
        assertEquals(readingProgress, syncData.readingProgress)
        assertEquals(bookmarks, syncData.bookmarks)
        assertEquals(metadata, syncData.metadata)
    }

    @Test
    fun `SyncData should allow empty books list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = emptyList(),
            readingProgress = listOf(createTestReadingProgressData()),
            bookmarks = listOf(createTestBookmarkData()),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.books.isEmpty())
    }

    @Test
    fun `SyncData should allow empty readingProgress list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = listOf(createTestBookSyncData()),
            readingProgress = emptyList(),
            bookmarks = listOf(createTestBookmarkData()),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.readingProgress.isEmpty())
    }

    @Test
    fun `SyncData should allow empty bookmarks list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = listOf(createTestBookSyncData()),
            readingProgress = listOf(createTestReadingProgressData()),
            bookmarks = emptyList(),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.bookmarks.isEmpty())
    }

    @Test
    fun `SyncData should allow all empty lists`() {
        // Arrange & Act
        val syncData = SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.books.isEmpty())
        assertTrue(syncData.readingProgress.isEmpty())
        assertTrue(syncData.bookmarks.isEmpty())
    }

    private fun createTestBookSyncData(): BookSyncData {
        return BookSyncData(
            bookId = 123L,
            title = "Test Book",
            author = "Test Author",
            coverUrl = "https://example.com/cover.jpg",
            sourceId = "source-1",
            sourceUrl = "https://example.com/book/123",
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileHash = "abc123"
        )
    }

    private fun createTestReadingProgressData(): ReadingProgressData {
        return ReadingProgressData(
            bookId = 123L,
            chapterId = 456L,
            chapterIndex = 5,
            offset = 1024,
            progress = 0.75f,
            lastReadAt = System.currentTimeMillis()
        )
    }

    private fun createTestBookmarkData(): BookmarkData {
        return BookmarkData(
            bookmarkId = 789L,
            bookId = 123L,
            chapterId = 456L,
            position = 1024,
            note = "Test note",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTestSyncMetadata(): SyncMetadata {
        return SyncMetadata(
            deviceId = "test-device-123",
            timestamp = System.currentTimeMillis(),
            version = 1,
            checksum = "abc123def456"
        )
    }
}
