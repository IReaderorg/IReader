package ireader.domain.use_cases.library

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.library.LibraryStatistics
import ireader.domain.use_cases.library.LibraryStatisticsUseCase
import ireader.domain.use_cases.library.LibraryUpdateUseCase
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for library management use cases
 * Tests critical library operations including updates, statistics, and tracking
 */
class LibraryUpdateUseCaseTest {
    
    private lateinit var libraryUpdateUseCase: LibraryUpdateUseCase
    private lateinit var libraryRepository: LibraryRepository
    
    @BeforeTest
    fun setup() {
        libraryRepository = mockk()
        libraryUpdateUseCase = LibraryUpdateUseCase(libraryRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `updateLibrary should check for updates for all favorite books`() = runTest {
        // Given
        val favoriteBooks = listOf(
            createTestBook(1L, favorite = true),
            createTestBook(2L, favorite = true),
            createTestBook(3L, favorite = true)
        )
        coEvery { libraryRepository.findFavorites() } returns favoriteBooks
        coEvery { libraryRepository.updateBook(any()) } returns 1
        
        // When
        val result = libraryUpdateUseCase.updateAll()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { libraryRepository.findFavorites() }
    }
    
    @Test
    fun `updateLibrary should handle empty library`() = runTest {
        // Given
        coEvery { libraryRepository.findFavorites() } returns emptyList()
        
        // When
        val result = libraryUpdateUseCase.updateAll()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { libraryRepository.updateBook(any()) }
    }
    
    @Test
    fun `updateSingleBook should update specific book`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(bookId, favorite = true)
        coEvery { libraryRepository.findBookById(bookId) } returns book
        coEvery { libraryRepository.updateBook(book) } returns 1
        
        // When
        val result = libraryUpdateUseCase.updateSingle(bookId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { libraryRepository.updateBook(book) }
    }
    
    @Test
    fun `updateSingleBook should fail when book not found`() = runTest {
        // Given
        val bookId = 999L
        coEvery { libraryRepository.findBookById(bookId) } returns null
        
        // When
        val result = libraryUpdateUseCase.updateSingle(bookId)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `updateCategory should update all books in category`() = runTest {
        // Given
        val categoryId = 1L
        val booksInCategory = listOf(
            createTestBook(1L, favorite = true),
            createTestBook(2L, favorite = true)
        )
        coEvery { libraryRepository.findBooksByCategory(categoryId) } returns booksInCategory
        coEvery { libraryRepository.updateBook(any()) } returns 1
        
        // When
        val result = libraryUpdateUseCase.updateCategory(categoryId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { libraryRepository.updateBook(any()) }
    }
    
    private fun createTestBook(id: Long, favorite: Boolean): Book {
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
            favorite = favorite,
            lastUpdate = System.currentTimeMillis(),
            lastInit = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0,
            key = "test-book-$id"
        )
    }
}

/**
 * Unit tests for library statistics use case
 */
class LibraryStatisticsUseCaseTest {
    
    private lateinit var libraryStatisticsUseCase: LibraryStatisticsUseCase
    private lateinit var libraryRepository: LibraryRepository
    
    @BeforeTest
    fun setup() {
        libraryRepository = mockk()
        libraryStatisticsUseCase = LibraryStatisticsUseCase(libraryRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getStatistics should return library statistics`() = runTest {
        // Given
        val expectedStats = LibraryStatistics(
            totalBooks = 100,
            totalChapters = 5000,
            readChapters = 2500,
            unreadChapters = 2500,
            completedBooks = 30,
            ongoingBooks = 70,
            totalReadingTime = 360000L,
            averageReadingSpeed = 250
        )
        coEvery { libraryRepository.getStatistics() } returns flowOf(expectedStats)
        
        // When
        val result = mutableListOf<LibraryStatistics>()
        libraryStatisticsUseCase.getStatistics().collect { result.add(it) }
        
        // Then
        assertEquals(1, result.size)
        assertEquals(100, result.first().totalBooks)
        assertEquals(5000, result.first().totalChapters)
        assertEquals(2500, result.first().readChapters)
    }
    
    @Test
    fun `getStatistics should handle empty library`() = runTest {
        // Given
        val emptyStats = LibraryStatistics(
            totalBooks = 0,
            totalChapters = 0,
            readChapters = 0,
            unreadChapters = 0,
            completedBooks = 0,
            ongoingBooks = 0,
            totalReadingTime = 0L,
            averageReadingSpeed = 0
        )
        coEvery { libraryRepository.getStatistics() } returns flowOf(emptyStats)
        
        // When
        val result = mutableListOf<LibraryStatistics>()
        libraryStatisticsUseCase.getStatistics().collect { result.add(it) }
        
        // Then
        assertEquals(1, result.size)
        assertEquals(0, result.first().totalBooks)
    }
    
    @Test
    fun `getReadingProgress should calculate completion percentage`() = runTest {
        // Given
        val bookId = 1L
        val totalChapters = 100
        val readChapters = 75
        coEvery { libraryRepository.getBookProgress(bookId) } returns Pair(readChapters, totalChapters)
        
        // When
        val progress = libraryStatisticsUseCase.getReadingProgress(bookId)
        
        // Then
        assertEquals(75.0, progress)
    }
    
    @Test
    fun `getReadingProgress should return 0 for books with no chapters`() = runTest {
        // Given
        val bookId = 1L
        coEvery { libraryRepository.getBookProgress(bookId) } returns Pair(0, 0)
        
        // When
        val progress = libraryStatisticsUseCase.getReadingProgress(bookId)
        
        // Then
        assertEquals(0.0, progress)
    }
    
    @Test
    fun `getReadingProgress should return 100 for completed books`() = runTest {
        // Given
        val bookId = 1L
        val totalChapters = 50
        coEvery { libraryRepository.getBookProgress(bookId) } returns Pair(totalChapters, totalChapters)
        
        // When
        val progress = libraryStatisticsUseCase.getReadingProgress(bookId)
        
        // Then
        assertEquals(100.0, progress)
    }
}

/**
 * Unit tests for library backup use case
 */
class LibraryBackupUseCaseTest {
    
    private lateinit var libraryBackupUseCase: LibraryBackupUseCase
    private lateinit var libraryRepository: LibraryRepository
    
    @BeforeTest
    fun setup() {
        libraryRepository = mockk()
        libraryBackupUseCase = LibraryBackupUseCase(libraryRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `createBackup should backup all favorite books`() = runTest {
        // Given
        val favoriteBooks = listOf(
            createTestBook(1L),
            createTestBook(2L),
            createTestBook(3L)
        )
        coEvery { libraryRepository.findFavorites() } returns favoriteBooks
        
        // When
        val result = libraryBackupUseCase.createBackup()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { libraryRepository.findFavorites() }
    }
    
    @Test
    fun `createBackup should handle empty library`() = runTest {
        // Given
        coEvery { libraryRepository.findFavorites() } returns emptyList()
        
        // When
        val result = libraryBackupUseCase.createBackup()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `restoreBackup should restore books from backup`() = runTest {
        // Given
        val backupBooks = listOf(
            createTestBook(1L),
            createTestBook(2L)
        )
        coEvery { libraryRepository.insertBooks(backupBooks) } just Runs
        
        // When
        val result = libraryBackupUseCase.restoreBackup(backupBooks)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { libraryRepository.insertBooks(backupBooks) }
    }
    
    @Test
    fun `restoreBackup should handle errors gracefully`() = runTest {
        // Given
        val backupBooks = listOf(createTestBook(1L))
        coEvery { libraryRepository.insertBooks(backupBooks) } throws Exception("Database error")
        
        // When
        val result = libraryBackupUseCase.restoreBackup(backupBooks)
        
        // Then
        assertTrue(result.isFailure)
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
