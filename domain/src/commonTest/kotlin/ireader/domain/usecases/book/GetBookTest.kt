package ireader.domain.usecases.book

import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.entities.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetBookTest {
    
    private lateinit var bookRepository: BookRepository
    private lateinit var getBook: GetBook
    
    @BeforeTest
    fun setup() {
        bookRepository = mockk()
        getBook = GetBook(bookRepository)
    }
    
    @Test
    fun `await returns book when found`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        coEvery { bookRepository.getBookById(bookId) } returns expectedBook
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertEquals(expectedBook, result)
        coVerify { bookRepository.getBookById(bookId) }
    }
    
    @Test
    fun `await returns null when book not found`() = runTest {
        // Given
        val bookId = 1L
        coEvery { bookRepository.getBookById(bookId) } returns null
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertNull(result)
        coVerify { bookRepository.getBookById(bookId) }
    }
    
    @Test
    fun `await returns null when repository throws exception`() = runTest {
        // Given
        val bookId = 1L
        coEvery { bookRepository.getBookById(bookId) } throws RuntimeException("Database error")
        
        // When
        val result = getBook.await(bookId)
        
        // Then
        assertNull(result)
        coVerify { bookRepository.getBookById(bookId) }
    }
    
    @Test
    fun `subscribe returns flow from repository`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        val expectedFlow = flowOf(expectedBook)
        coEvery { bookRepository.getBookByIdAsFlow(bookId) } returns expectedFlow
        
        // When
        val result = getBook.subscribe(bookId)
        
        // Then
        assertEquals(expectedFlow, result)
        coVerify { bookRepository.getBookByIdAsFlow(bookId) }
    }
    
    @Test
    fun `awaitByUrlAndSource returns book when found`() = runTest {
        // Given
        val url = "test-url"
        val sourceId = 1L
        val expectedBook = createTestBook(key = url, sourceId = sourceId)
        coEvery { bookRepository.getBookByUrlAndSourceId(url, sourceId) } returns expectedBook
        
        // When
        val result = getBook.awaitByUrlAndSource(url, sourceId)
        
        // Then
        assertEquals(expectedBook, result)
        coVerify { bookRepository.getBookByUrlAndSourceId(url, sourceId) }
    }
    
    @Test
    fun `awaitByUrlAndSource returns null when repository throws exception`() = runTest {
        // Given
        val url = "test-url"
        val sourceId = 1L
        coEvery { bookRepository.getBookByUrlAndSourceId(url, sourceId) } throws RuntimeException("Network error")
        
        // When
        val result = getBook.awaitByUrlAndSource(url, sourceId)
        
        // Then
        assertNull(result)
        coVerify { bookRepository.getBookByUrlAndSourceId(url, sourceId) }
    }
    
    private fun createTestBook(
        id: Long = 1L,
        sourceId: Long = 1L,
        title: String = "Test Book",
        key: String = "test-key",
        favorite: Boolean = false
    ) = Book(
        id = id,
        sourceId = sourceId,
        title = title,
        key = key,
        favorite = favorite
    )
}