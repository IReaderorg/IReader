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
        val localHistory = createHistory(lastRead = 1000L, readingProgress = 0.5)
        val remoteHistory = createHistory(lastRead = 2000L, readingProgress = 0.7)
        val conflict = DataConflict(
            conflictType = ConflictType.HISTORY,
            localData = localHistory,
            remoteData = remoteHistory,
            conflictField = "readingProgress"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(remoteHistory, resolved[0])
    }

    @Test
    fun `invoke with LOCAL_WINS should always choose local data`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localHistory = createHistory(lastRead = 1000L, readingProgress = 0.5)
        val remoteHistory = createHistory(lastRead = 2000L, readingProgress = 0.7)
        val conflict = DataConflict(
            conflictType = ConflictType.HISTORY,
            localData = localHistory,
            remoteData = remoteHistory,
            conflictField = "readingProgress"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.LOCAL_WINS)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(localHistory, resolved[0])
    }

    @Test
    fun `invoke with REMOTE_WINS should always choose remote data`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localHistory = createHistory(lastRead = 2000L, readingProgress = 0.7)
        val remoteHistory = createHistory(lastRead = 1000L, readingProgress = 0.5)
        val conflict = DataConflict(
            conflictType = ConflictType.HISTORY,
            localData = localHistory,
            remoteData = remoteHistory,
            conflictField = "readingProgress"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.REMOTE_WINS)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        assertEquals(remoteHistory, resolved[0])
    }

    @Test
    fun `invoke with MERGE should merge history by choosing furthest progress`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localHistory = createHistory(lastRead = 1000L, readingProgress = 0.5)
        val remoteHistory = createHistory(lastRead = 2000L, readingProgress = 0.7)
        val conflict = DataConflict(
            conflictType = ConflictType.HISTORY,
            localData = localHistory,
            remoteData = remoteHistory,
            conflictField = "readingProgress"
        )

        // Act
        val result = useCase(listOf(conflict), ConflictResolutionStrategy.MERGE)

        // Assert
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals(1, resolved.size)
        val mergedHistory = resolved[0] as HistorySyncData
        assertEquals(0.7, mergedHistory.readingProgress) // Furthest progress
    }

    @Test
    fun `invoke with MANUAL should return failure requiring user intervention`() {
        // Arrange
        val useCase = ResolveConflictsUseCase()
        val localHistory = createHistory(lastRead = 1000L, readingProgress = 0.5)
        val remoteHistory = createHistory(lastRead = 2000L, readingProgress = 0.7)
        val conflict = DataConflict(
            conflictType = ConflictType.HISTORY,
            localData = localHistory,
            remoteData = remoteHistory,
            conflictField = "readingProgress"
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
                conflictType = ConflictType.HISTORY,
                localData = createHistory(lastRead = 1000L, readingProgress = 0.5),
                remoteData = createHistory(lastRead = 2000L, readingProgress = 0.7),
                conflictField = "readingProgress"
            ),
            DataConflict(
                conflictType = ConflictType.CHAPTER,
                localData = createChapter(dateFetch = 1000L, read = true),
                remoteData = createChapter(dateFetch = 2000L, read = false),
                conflictField = "read"
            )
        )

        // Act
        val result = useCase(conflicts, ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    private fun createHistory(lastRead: Long, readingProgress: Double): HistorySyncData {
        return HistorySyncData(
            chapterGlobalId = "1|ch1",
            lastRead = lastRead,
            timeRead = 5000L,
            readingProgress = readingProgress
        )
    }

    private fun createChapter(dateFetch: Long, read: Boolean): ChapterSyncData {
        return ChapterSyncData(
            globalId = "1|ch1",
            bookGlobalId = "1|book1",
            key = "ch1",
            name = "Chapter 1",
            read = read,
            bookmark = false,
            lastPageRead = 10,
            sourceOrder = 1,
            number = 1.0f,
            dateUpload = 1000L,
            dateFetch = dateFetch,
            translator = "",
            content = "[]"
        )
    }
}
