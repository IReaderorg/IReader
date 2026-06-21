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
        val localData = createSyncData()
        val remoteData = createSyncData()

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `invoke should detect history conflict when timestamps and progress differ`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 1000L,
                    timeRead = 5000L,
                    readingProgress = 0.5
                )
            )
        )
        val remoteData = createSyncData(
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 2000L,
                    timeRead = 6000L,
                    readingProgress = 0.7
                )
            )
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(1, conflicts.size)
        assertEquals(ConflictType.HISTORY, conflicts[0].conflictType)
    }

    @Test
    fun `invoke should not detect conflict when only timestamps differ but data is same`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 1000L,
                    timeRead = 5000L,
                    readingProgress = 0.5
                )
            )
        )
        val remoteData = createSyncData(
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 2000L,
                    timeRead = 6000L,
                    readingProgress = 0.5
                )
            )
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `invoke should detect chapter conflict when read status differs`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(
            chapters = listOf(
                ChapterSyncData(
                    globalId = "1|ch1",
                    bookGlobalId = "1|book1",
                    key = "ch1",
                    name = "Chapter 1",
                    read = true,
                    bookmark = false,
                    lastPageRead = 10,
                    sourceOrder = 1,
                    number = 1.0f,
                    dateUpload = 1000L,
                    dateFetch = 1000L,
                    translator = "",
                    content = "[]"
                )
            )
        )
        val remoteData = createSyncData(
            chapters = listOf(
                ChapterSyncData(
                    globalId = "1|ch1",
                    bookGlobalId = "1|book1",
                    key = "ch1",
                    name = "Chapter 1",
                    read = false,
                    bookmark = false,
                    lastPageRead = 10,
                    sourceOrder = 1,
                    number = 1.0f,
                    dateUpload = 1000L,
                    dateFetch = 2000L,
                    translator = "",
                    content = "[]"
                )
            )
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(1, conflicts.size)
        assertEquals(ConflictType.CHAPTER, conflicts[0].conflictType)
    }

    @Test
    fun `invoke should detect book metadata conflict when update times differ`() {
        // Arrange
        val useCase = DetectConflictsUseCase()
        val localData = createSyncData(
            books = listOf(
                BookSyncData(
                    globalId = "1|book1",
                    title = "Book",
                    author = "Author",
                    coverUrl = null,
                    sourceId = "1",
                    key = "book1",
                    favorite = false,
                    addedAt = 1000L,
                    updatedAt = 1000L,
                    description = "desc",
                    genres = emptyList(),
                    status = 1L
                )
            )
        )
        val remoteData = createSyncData(
            books = listOf(
                BookSyncData(
                    globalId = "1|book1",
                    title = "Book Updated",
                    author = "Author",
                    coverUrl = null,
                    sourceId = "1",
                    key = "book1",
                    favorite = false,
                    addedAt = 1000L,
                    updatedAt = 2000L,
                    description = "desc",
                    genres = emptyList(),
                    status = 1L
                )
            )
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
        val localData = createSyncData(
            books = listOf(
                BookSyncData(
                    globalId = "1|book1",
                    title = "Book",
                    author = "Author",
                    coverUrl = null,
                    sourceId = "1",
                    key = "book1",
                    favorite = false,
                    addedAt = 1000L,
                    updatedAt = 1000L,
                    description = "desc",
                    genres = emptyList(),
                    status = 1L
                )
            ),
            chapters = listOf(
                ChapterSyncData(
                    globalId = "1|ch1",
                    bookGlobalId = "1|book1",
                    key = "ch1",
                    name = "Chapter 1",
                    read = true,
                    bookmark = false,
                    lastPageRead = 10,
                    sourceOrder = 1,
                    number = 1.0f,
                    dateUpload = 1000L,
                    dateFetch = 1000L,
                    translator = "",
                    content = "[]"
                )
            ),
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 1000L,
                    timeRead = 5000L,
                    readingProgress = 0.5
                )
            )
        )
        val remoteData = createSyncData(
            books = listOf(
                BookSyncData(
                    globalId = "1|book1",
                    title = "Book Updated",
                    author = "Author",
                    coverUrl = null,
                    sourceId = "1",
                    key = "book1",
                    favorite = false,
                    addedAt = 1000L,
                    updatedAt = 2000L,
                    description = "desc",
                    genres = emptyList(),
                    status = 1L
                )
            ),
            chapters = listOf(
                ChapterSyncData(
                    globalId = "1|ch1",
                    bookGlobalId = "1|book1",
                    key = "ch1",
                    name = "Chapter 1",
                    read = false,
                    bookmark = false,
                    lastPageRead = 10,
                    sourceOrder = 1,
                    number = 1.0f,
                    dateUpload = 1000L,
                    dateFetch = 2000L,
                    translator = "",
                    content = "[]"
                )
            ),
            history = listOf(
                HistorySyncData(
                    chapterGlobalId = "1|ch1",
                    lastRead = 2000L,
                    timeRead = 6000L,
                    readingProgress = 0.7
                )
            )
        )

        // Act
        val conflicts = useCase(localData, remoteData)

        // Assert
        assertEquals(3, conflicts.size)
        assertTrue(conflicts.any { it.conflictType == ConflictType.BOOK_METADATA })
        assertTrue(conflicts.any { it.conflictType == ConflictType.CHAPTER })
        assertTrue(conflicts.any { it.conflictType == ConflictType.HISTORY })
    }

    private fun createSyncData(
        books: List<BookSyncData> = emptyList(),
        chapters: List<ChapterSyncData> = emptyList(),
        history: List<HistorySyncData> = emptyList()
    ): SyncData {
        return SyncData(
            books = books,
            chapters = chapters,
            history = history,
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
