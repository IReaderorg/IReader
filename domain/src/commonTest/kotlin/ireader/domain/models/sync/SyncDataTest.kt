package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncDataTest {

    @Test
    fun `SyncData should be created with valid data`() {
        // Arrange
        val books = listOf(createTestBookSyncData())
        val chapters = listOf(createTestChapterSyncData())
        val history = listOf(createTestHistorySyncData())
        val metadata = createTestSyncMetadata()

        // Act
        val syncData = SyncData(
            books = books,
            chapters = chapters,
            history = history,
            metadata = metadata
        )

        // Assert
        assertEquals(books, syncData.books)
        assertEquals(chapters, syncData.chapters)
        assertEquals(history, syncData.history)
        assertEquals(metadata, syncData.metadata)
    }

    @Test
    fun `SyncData should allow empty books list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = emptyList(),
            chapters = listOf(createTestChapterSyncData()),
            history = listOf(createTestHistorySyncData()),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.books.isEmpty())
    }

    @Test
    fun `SyncData should allow empty chapters list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = listOf(createTestBookSyncData()),
            chapters = emptyList(),
            history = listOf(createTestHistorySyncData()),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.chapters.isEmpty())
    }

    @Test
    fun `SyncData should allow empty history list`() {
        // Arrange & Act
        val syncData = SyncData(
            books = listOf(createTestBookSyncData()),
            chapters = listOf(createTestChapterSyncData()),
            history = emptyList(),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.history.isEmpty())
    }

    @Test
    fun `SyncData should allow all empty lists`() {
        // Arrange & Act
        val syncData = SyncData(
            books = emptyList(),
            chapters = emptyList(),
            history = emptyList(),
            metadata = createTestSyncMetadata()
        )

        // Assert
        assertTrue(syncData.books.isEmpty())
        assertTrue(syncData.chapters.isEmpty())
        assertTrue(syncData.history.isEmpty())
    }

    private fun createTestBookSyncData(): BookSyncData {
        return BookSyncData(
            globalId = "1|book1",
            title = "Test Book",
            author = "Test Author",
            coverUrl = "https://example.com/cover.jpg",
            sourceId = "1",
            key = "book1",
            favorite = false,
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            description = "Test description",
            genres = listOf("Test"),
            status = 1L
        )
    }

    private fun createTestChapterSyncData(): ChapterSyncData {
        return ChapterSyncData(
            globalId = "1|ch1",
            bookGlobalId = "1|book1",
            key = "ch1",
            name = "Chapter 1",
            read = false,
            bookmark = false,
            lastPageRead = 0,
            sourceOrder = 1,
            number = 1.0f,
            dateUpload = System.currentTimeMillis(),
            dateFetch = System.currentTimeMillis(),
            translator = "",
            content = "[]"
        )
    }

    private fun createTestHistorySyncData(): HistorySyncData {
        return HistorySyncData(
            chapterGlobalId = "1|ch1",
            lastRead = System.currentTimeMillis(),
            timeRead = 5000L,
            readingProgress = 0.5
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
