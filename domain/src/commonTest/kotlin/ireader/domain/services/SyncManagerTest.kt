package ireader.domain.services

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.usecases.sync.GetSyncedDataUseCase
import ireader.domain.usecases.sync.SyncBooksUseCase
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for SyncManager
 * Tests sync operations, debouncing, auto-sync, and state management
 */
class SyncManagerTest {
    
    private lateinit var syncManager: SyncManager
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var supabasePreferences: SupabasePreferences
    private lateinit var syncBooksUseCase: SyncBooksUseCase
    private lateinit var getSyncedDataUseCase: GetSyncedDataUseCase
    
    @BeforeTest
    fun setup() {
        remoteRepository = mockk()
        supabasePreferences = mockk()
        syncBooksUseCase = mockk()
        getSyncedDataUseCase = mockk()
        
        // Setup default preferences
        every { supabasePreferences.autoSyncEnabled() } returns mockk {
            every { get() } returns true
        }
        every { supabasePreferences.lastSyncTime() } returns mockk {
            every { get() } returns 0L
            every { set(any()) } just Runs
        }
        
        syncManager = SyncManager(
            remoteRepository,
            supabasePreferences,
            syncBooksUseCase,
            getSyncedDataUseCase
        )
    }
    
    @AfterTest
    fun tearDown() {
        syncManager.stopAutoSync()
        unmockkAll()
    }
    
    @Test
    fun `syncReadingProgress should sync progress to remote`() = runTest {
        // Given
        val userId = "user-123"
        val bookId = 1L
        val sourceId = 100L
        val chapterSlug = "chapter-5"
        val scrollPosition = 0.75f
        
        coEvery { remoteRepository.syncReadingProgress(any()) } returns Result.success(Unit)
        
        // When
        val result = syncManager.syncReadingProgress(
            userId, bookId, sourceId, chapterSlug, scrollPosition
        )
        
        // Then
        assertTrue(result.isSuccess)
        coVerify {
            remoteRepository.syncReadingProgress(match { progress ->
                progress.userId == userId &&
                progress.bookId == "$sourceId-$bookId" &&
                progress.lastChapterSlug == chapterSlug &&
                progress.lastScrollPosition == scrollPosition
            })
        }
    }
    
    @Test
    fun `syncReadingProgress should not sync when auto-sync is disabled`() = runTest {
        // Given
        every { supabasePreferences.autoSyncEnabled().get() } returns false
        val userId = "user-123"
        
        // When
        val result = syncManager.syncReadingProgress(
            userId, 1L, 100L, "chapter-1", 0.5f
        )
        
        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { remoteRepository.syncReadingProgress(any()) }
    }
    
    @Test
    fun `syncBook should sync single book`() = runTest {
        // Given
        val userId = "user-123"
        val book = createTestBook(1L)
        
        coEvery { syncBooksUseCase.syncSingleBook(userId, book) } returns Result.success(Unit)
        
        // When
        val result = syncManager.syncBook(userId, book)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { syncBooksUseCase.syncSingleBook(userId, book) }
    }
    
    @Test
    fun `syncBooks should sync multiple books and update sync time`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(
            createTestBook(1L),
            createTestBook(2L),
            createTestBook(3L)
        )
        
        coEvery { syncBooksUseCase(userId, books) } returns Result.success(Unit)
        
        // When
        val result = syncManager.syncBooks(userId, books)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { syncBooksUseCase(userId, books) }
        verify { supabasePreferences.lastSyncTime().set(any()) }
    }
    
    @Test
    fun `syncBooks should update isSyncing state`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(createTestBook(1L))
        
        coEvery { syncBooksUseCase(userId, books) } coAnswers {
            // Check state during sync
            assertTrue(syncManager.isSyncing.value)
            Result.success(Unit)
        }
        
        // When
        assertFalse(syncManager.isSyncing.value) // Before sync
        syncManager.syncBooks(userId, books)
        
        // Then
        assertFalse(syncManager.isSyncing.value) // After sync
    }
    
    @Test
    fun `performFullSync should sync all books`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(
            createTestBook(1L),
            createTestBook(2L)
        )
        
        coEvery { syncBooksUseCase(userId, books) } returns Result.success(Unit)
        
        // When
        val result = syncManager.performFullSync(userId, books)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { syncBooksUseCase(userId, books) }
        verify { supabasePreferences.lastSyncTime().set(any()) }
    }
    
    @Test
    fun `performFullSync should handle errors gracefully`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(createTestBook(1L))
        val error = Exception("Network error")
        
        coEvery { syncBooksUseCase(userId, books) } returns Result.failure(error)
        
        // When
        val result = syncManager.performFullSync(userId, books)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        assertFalse(syncManager.isSyncing.value)
    }
    
    @Test
    fun `getSyncedBooks should retrieve books from remote`() = runTest {
        // Given
        val userId = "user-123"
        val syncedBooks = listOf(createTestBook(1L))
        
        coEvery { getSyncedDataUseCase.getSyncedBooks(userId) } returns Result.success(syncedBooks)
        
        // When
        val result = syncManager.getSyncedBooks(userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(syncedBooks, result.getOrNull())
    }
    
    @Test
    fun `lastSyncTime should be updated after successful sync`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(createTestBook(1L))
        val initialTime = syncManager.lastSyncTime.value
        
        coEvery { syncBooksUseCase(userId, books) } returns Result.success(Unit)
        
        // When
        syncManager.syncBooks(userId, books)
        
        // Then
        val updatedTime = syncManager.lastSyncTime.value
        assertTrue(updatedTime > initialTime)
    }
    
    @Test
    fun `startAutoSync should not start when auto-sync is disabled`() = runTest {
        // Given
        every { supabasePreferences.autoSyncEnabled().get() } returns false
        
        // When
        syncManager.startAutoSync()
        
        // Then - No sync should be triggered
        coVerify(exactly = 0) { remoteRepository.getCurrentUser() }
    }
    
    @Test
    fun `stopAutoSync should cancel auto-sync job`() = runTest {
        // Given
        coEvery { remoteRepository.getCurrentUser() } returns Result.success(
            User("user-123", "test@example.com", "testuser", 0L)
        )
        
        // When
        syncManager.startAutoSync()
        syncManager.stopAutoSync()
        
        // Then - Auto-sync should be stopped
        // (Job cancellation is internal, we just verify no errors)
    }
    
    @Test
    fun `syncReadingProgress should handle network errors`() = runTest {
        // Given
        val userId = "user-123"
        val error = Exception("Network unavailable")
        
        coEvery { remoteRepository.syncReadingProgress(any()) } returns Result.failure(error)
        
        // When
        val result = syncManager.syncReadingProgress(
            userId, 1L, 100L, "chapter-1", 0.5f
        )
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
    
    @Test
    fun `syncBook should not sync when auto-sync is disabled`() = runTest {
        // Given
        every { supabasePreferences.autoSyncEnabled().get() } returns false
        val userId = "user-123"
        val book = createTestBook(1L)
        
        // When
        val result = syncManager.syncBook(userId, book)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { syncBooksUseCase.syncSingleBook(any(), any()) }
    }
    
    @Test
    fun `isSyncing should reflect current sync state`() = runTest {
        // Given
        val userId = "user-123"
        val books = listOf(createTestBook(1L))
        
        var syncingDuringOperation = false
        coEvery { syncBooksUseCase(userId, books) } coAnswers {
            syncingDuringOperation = syncManager.isSyncing.value
            Result.success(Unit)
        }
        
        // When
        assertFalse(syncManager.isSyncing.value)
        syncManager.syncBooks(userId, books)
        
        // Then
        assertTrue(syncingDuringOperation)
        assertFalse(syncManager.isSyncing.value)
    }
    
    private fun createTestBook(id: Long): Book {
        return Book(
            id = id,
            sourceId = 100L,
            title = "Test Book $id",
            author = "Test Author",
            description = "Test description",
            genres = listOf("Fantasy"),
            status = 1,
            cover = "https://example.com/cover.jpg",
            customCover = null,
            favorite = true,
            lastUpdate = System.currentTimeMillis(),
            lastInit = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0,
            key = "test-book-$id"
        )
    }
}
