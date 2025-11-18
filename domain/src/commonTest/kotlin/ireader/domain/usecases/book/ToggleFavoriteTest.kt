package ireader.domain.usecases.book

import ireader.domain.models.entities.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ToggleFavoriteTest {
    
    private lateinit var getBook: GetBook
    private lateinit var addToLibrary: AddToLibrary
    private lateinit var removeFromLibrary: RemoveFromLibrary
    private lateinit var toggleFavorite: ToggleFavorite
    
    @BeforeTest
    fun setup() {
        getBook = mockk()
        addToLibrary = mockk()
        removeFromLibrary = mockk()
        toggleFavorite = ToggleFavorite(getBook, addToLibrary, removeFromLibrary)
    }
    
    @Test
    fun `await adds book to library when not favorite`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId, favorite = false)
        coEvery { getBook.await(bookId) } returns book
        coEvery { addToLibrary.await(bookId) } returns true
        
        // When
        val result = toggleFavorite.await(bookId)
        
        // Then
        assertTrue(result)
        coVerify { getBook.await(bookId) }
        coVerify { addToLibrary.await(bookId) }
        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
    }
    
    @Test
    fun `await removes book from library when favorite`() = runTest {
        // Given
        val bookId = 1L
        val book = createTestBook(id = bookId, favorite = true)
        coEvery { getBook.await(bookId) } returns book
        coEvery { removeFromLibrary.await(bookId) } returns true
        
        // When
        val result = toggleFavorite.await(bookId)
        
        // Then
        assertTrue(result)
        coVerify { getBook.await(bookId) }
        coVerify { removeFromLibrary.await(bookId) }
        coVerify(exactly = 0) { addToLibrary.await(any()) }
    }
    
    @Test
    fun `await returns false when book not found`() = runTest {
        // Given
        val bookId = 1L
        coEvery { getBook.await(bookId) } returns null
        
        // When
        val result = toggleFavorite.await(bookId)
        
        // Then
        assertFalse(result)
        coVerify { getBook.await(bookId) }
        coVerify(exactly = 0) { addToLibrary.await(any()) }
        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
    }
    
    @Test
    fun `await with book entity adds to library when not favorite`() = runTest {
        // Given
        val book = createTestBook(favorite = false)
        coEvery { addToLibrary.await(book.id) } returns true
        
        // When
        val result = toggleFavorite.await(book)
        
        // Then
        assertTrue(result)
        coVerify { addToLibrary.await(book.id) }
        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
    }
    
    @Test
    fun `await with book entity removes from library when favorite`() = runTest {
        // Given
        val book = createTestBook(favorite = true)
        coEvery { removeFromLibrary.await(book.id) } returns true
        
        // When
        val result = toggleFavorite.await(book)
        
        // Then
        assertTrue(result)
        coVerify { removeFromLibrary.await(book.id) }
        coVerify(exactly = 0) { addToLibrary.await(any()) }
    }
    
    @Test
    fun `await returns false when add to library fails`() = runTest {
        // Given
        val book = createTestBook(favorite = false)
        coEvery { addToLibrary.await(book.id) } returns false
        
        // When
        val result = toggleFavorite.await(book)
        
        // Then
        assertFalse(result)
        coVerify { addToLibrary.await(book.id) }
    }
    
    @Test
    fun `await returns false when remove from library fails`() = runTest {
        // Given
        val book = createTestBook(favorite = true)
        coEvery { removeFromLibrary.await(book.id) } returns false
        
        // When
        val result = toggleFavorite.await(book)
        
        // Then
        assertFalse(result)
        coVerify { removeFromLibrary.await(book.id) }
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