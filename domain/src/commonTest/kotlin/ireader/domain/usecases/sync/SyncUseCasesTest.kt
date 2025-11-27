package ireader.domain.usecases.sync

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.remote.SyncedBook
import ireader.domain.services.SyncManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for sync use cases
 * Tests synchronization between local and remote data
 */
class SyncUseCasesTest {
    
    private lateinit var syncBookToRemoteUseCase: SyncBookToRemoteUseCase
    private lateinit var checkSyncAvailabilityUseCase: CheckSyncAvailabilityUseCase
    private lateinit var syncManager: SyncManager
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var bookRepository: BookRepository
    
    @BeforeTest
    fun setup() {
        syncManager = mockk()
        remoteRepository = mockk()
        bookRepository = mockk()
        syncBookToRemoteUseCase = SyncBookToRemoteUseCase(syncManager, remoteRepository)
        checkSyncAvailabilityUseCase = CheckSyncAvailabilityUseCase(remoteRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `syncBookToRemote should sync book successfully`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(bookId)
        coEvery { bookRepository.findBookById(bookId) } returns book
        coEvery { syncManager.syncBook(book) } returns Result.success(Unit)
        coEvery { remoteRepository.syncBook(any()) } returns Result.success(Unit)
        
        // When
        val result = syncBookToRemoteUseCase(bookId)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `syncBookToRemote should fail when book not found`() = runTest {
        // Given
        val bookId = 999L
        coEvery { bookRepository.findBookById(bookId) } returns null
        
        // When
        val result = syncBookToRemoteUseCase(bookId)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `checkSyncAvailability should return true when remote is available`() = runTest {
        // Given
        coEvery { remoteRepository.isAvailable() } returns true
        
        // When
        val result = checkSyncAvailabilityUseCase()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `checkSyncAvailability should return false when remote is null`() = runTest {
        // Given
        val useCase = CheckSyncAvailabilityUseCase(null)
        
        // When
        val result = useCase()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `checkSyncAvailability should return false when remote is unavailable`() = runTest {
        // Given
        coEvery { remoteRepository.isAvailable() } returns false
        
        // When
        val result = checkSyncAvailabilityUseCase()
        
        // Then
        assertFalse(result)
    }
    
    private fun createTestBook(id: Long): Book {
        return Book(
            id = id,
            sourceId = 100L,
            title = "Test Book",
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
