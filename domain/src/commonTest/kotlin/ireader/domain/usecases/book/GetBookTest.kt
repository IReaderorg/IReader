package ireader.domain.usecases.book

import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.entities.Book
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for GetBook use case
 */
class GetBookTest {
    
    private lateinit var getBook: GetBook
    private lateinit var bookRepository: BookRepository
    
    @BeforeTest
    fun setup() {
        bookRepository = mockk()
        getBook = GetBook(bookRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `await should return book when found`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(bookId)
        coEvery { bookRepository.getBookById(bookId) } returns expectedBook
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertNotNull(result)
        assertEquals(bookId, result.id)
        assertEquals(expectedBook.title, result.title)
    }
    
    @Test
    fun `await should return null when book not found`() = runTest {
        // Given
        val bookId = 999L
        coEvery { bookRepository.getBookById(bookId) } returns null
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `await should handle exceptions gracefully`() = runTest {
        // Given
        val bookId = 1L
        coEvery { bookRepository.getBookById(bookId) } throws RuntimeException("Database error")
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `subscribe should emit book updates`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(bookId)
        every { bookRepository.getBookByIdAsFlow(bookId) } returns flowOf(expectedBook)
        
        // When
        val result = getBook.subscribe(bookId).first()
        
        // Then
        assertNotNull(result)
        assertEquals(bookId, result.id)
    }
    
    @Test
    fun `subscribe should emit null when book not found`() = runTest {
        // Given
        val bookId = 999L
        every { bookRepository.getBookByIdAsFlow(bookId) } returns flowOf(null)
        
        // When
        val result = getBook.subscribe(bookId).first()
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `awaitByUrlAndSource should return book when found`() = runTest {
        // Given
        val url = "https://example.com/book/123"
        val sourceId = 1L
        val expectedBook = createTestBook(1L, key = url)
        coEvery { bookRepository.getBookByUrlAndSourceId(url, sourceId) } returns expectedBook
        
        // When
        val result = getBook.awaitByUrlAndSource(url, sourceId)
        
        // Then
        assertNotNull(result)
        assertEquals(url, result.key)
        assertEquals(sourceId, result.sourceId)
    }
    
    @Test
    fun `awaitByUrlAndSource should return null when book not found`() = runTest {
        // Given
        val url = "https://example.com/book/999"
        val sourceId = 1L
        coEvery { bookRepository.getBookByUrlAndSourceId(url, sourceId) } returns null
        
        // When
        val result = getBook.awaitByUrlAndSource(url, sourceId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `awaitByUrlAndSource should handle exceptions gracefully`() = runTest {
        // Given
        val url = "https://example.com/book/123"
        val sourceId = 1L
        coEvery { bookRepository.getBookByUrlAndSourceId(url, sourceId) } throws RuntimeException("Network error")
        
        // When
        val result = getBook.awaitByUrlAndSource(url, sourceId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `subscribeByUrlAndSource should emit book updates`() = runTest {
        // Given
        val url = "https://example.com/book/123"
        val sourceId = 1L
        val expectedBook = createTestBook(1L, key = url, sourceId = sourceId)
        every { bookRepository.getBookByUrlAndSourceIdAsFlow(url, sourceId) } returns flowOf(expectedBook)
        
        // When
        val result = getBook.subscribeByUrlAndSource(url, sourceId).first()
        
        // Then
        assertNotNull(result)
        assertEquals(url, result.key)
        assertEquals(sourceId, result.sourceId)
    }
    
    @Test
    fun `subscribeByUrlAndSource should emit null when book not found`() = runTest {
        // Given
        val url = "https://example.com/book/999"
        val sourceId = 1L
        every { bookRepository.getBookByUrlAndSourceIdAsFlow(url, sourceId) } returns flowOf(null)
        
        // When
        val result = getBook.subscribeByUrlAndSource(url, sourceId).first()
        
        // Then
        assertNull(result)
    }
    
    private fun createTestBook(
        id: Long,
        title: String = "Test Book",
        key: String = "test-book-$id",
        sourceId: Long = 1L
    ): Book {
        return Book(
            id = id,
            sourceId = sourceId,
            title = title,
            key = key,
            author = "Test Author",
            description = "Test Description",
            cover = "https://example.com/cover.jpg"
        )
    }
}
