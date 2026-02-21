package ireader.domain.usecases.sync

import ireader.domain.models.sync.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolveConflictsUseCaseTest {

    @Test
    fun `invoke with LATEST_TIMESTAMP should choose data with most recent timestamp`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localProgress = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5)
        val remoteProgress = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7)
        val conflict = DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = localProgress,
            remoteData = remoteProgress,
            conflictField = "chapterIndex"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(remoteProgress, resolved[0])
    }

    @Test
    fun `invoke with LOCAL_WINS should always choose local data`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localProgress = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5)
        val remoteProgress = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7)
        val conflict = DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = localProgress,
            remoteData = remoteProgress,
            conflictField = "chapterIndex"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.LOCAL_WINS)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(localProgress, resolved[0])
    }

    @Test
    fun `invoke with REMOTE_WINS should always choose remote data`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localProgress = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7)
        val remoteProgress = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5)
        val conflict = DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = localProgress,
            remoteData = remoteProgress,
            conflictField = "chapterIndex"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.REMOTE_WINS)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(remoteProgress, resolved[0])
    }

    @Test
    fun `invoke with MERGE should merge reading progress by choosing furthest chapter`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localProgress = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5)
        val remoteProgress = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7)
        val conflict = DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = localProgress,
            remoteData = remoteProgress,
            conflictField = "chapterIndex"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.MERGE)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        val mergedProgress = resolved[0] as ReadingProgressData
        assertEquals(7, mergedProgress.chapterIndex) // Furthest chapter
    }

    @Test
    fun `invoke with MANUAL should return failure requiring user intervention`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localProgress = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5)
        val remoteProgress = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7)
        val conflict = DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = localProgress,
            remoteData = remoteProgress,
            conflictField = "chapterIndex"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.MANUAL)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with empty conflicts should return empty list`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()

        // Act
        val result = useCase(emptyList(), ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `invoke should resolve multiple conflicts`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val conflicts = listOf(
            DataConflict(
                conflictType = ConflictType.READING_PROGRESS,
                localData = createReadingProgress(lastReadAt = 1000L, chapterIndex = 5),
                remoteData = createReadingProgress(lastReadAt = 2000L, chapterIndex = 7),
                conflictField = "chapterIndex"
            ),
            DataConflict(
                conflictType = ConflictType.BOOKMARK,
                localData = createBookmark(createdAt = 1000L, position = 100),
                remoteData = createBookmark(createdAt = 2000L, position = 200),
                conflictField = "position"
            )
        )

        // Act
        val result = useCase(conflicts, ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    private fun createReadingProgress(lastReadAt: Long, chapterIndex: Int): ReadingProgressData {
        return ReadingProgressData(
            bookId = 123L,
            chapterId = 456L,
            chapterIndex = chapterIndex,
            offset = 1024,
            progress = 0.75f,
            lastReadAt = lastReadAt
        )
    }

    private fun createBookmark(createdAt: Long, position: Int): BookmarkData {
        return BookmarkData(
            bookmarkId = 1L,
            bookId = 123L,
            chapterId = 456L,
            position = position,
            note = "Test note",
            createdAt = createdAt
        )
    }
}
