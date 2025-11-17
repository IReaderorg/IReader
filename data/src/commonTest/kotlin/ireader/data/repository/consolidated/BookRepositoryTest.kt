package ireader.data.repository.consolidated

import ireader.data.core.DatabaseHandler
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.errors.IReaderError
import ireader.domain.models.updates.BookUpdate
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class BookRepositoryTest {
    
    private lateinit var repository: BookRepositoryImpl
    private lateinit var handler: DatabaseHandler
    
    @BeforeTest
    fun setup() {
        handler = mockk()
        repository = BookRepositoryImpl(handler)
    }
    
    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `getBookById returns book when found`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        coEvery { handler.awaitOneOrNull<Book>(any()) } returns expectedBook
        
        // When
        val result = repository.getBookById(bookId)
        
        // Then
        assertEquals(expectedBook, result)
        coVerify { handler.awaitOneOrNull<Book>(any()) }
    }
    
    @Test
    fun `getBookById returns null when not found`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.awaitOneOrNull<Book>(any()) } returns null
        
        // When
        val result = repository.getBookById(bookId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getBookById throws DatabaseError when database fails`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.awaitOneOrNull<Book>(any()) } throws RuntimeException("Database error")
        
        // When & Then
        assertFailsWith<IReaderError.DatabaseError> {
            repository.getBookById(bookId)
        }
    }
    
    @Test
    fun `getBookByIdAsFlow returns flow of book`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        every { handler.subscribeToOneOrNull<Book>(any()) } returns flowOf(expectedBook)
        
        // When
        val result = repository.getBookByIdAsFlow(bookId)
        
        // Then
        result.collect { book ->
            assertEquals(expectedBook, book)
        }
    }
    
    @Test
    fun `getFavorites returns list of favorite books`() = runTest {
        // Given
        val favoriteBooks = listOf(
            createTestBook(id = 1L, favorite = true),
            createTestBook(id = 2L, favorite = true)
        )
        coEvery { handler.awaitList<Book>(any()) } returns favoriteBooks
        
        // When
        val result = repository.getFavorites()
        
        // Then
        assertEquals(favoriteBooks, result)
    }
    
    @Test
    fun `getFavorites returns empty list when database fails`() = runTest {
        // Given
        coEvery { handler.awaitList<Book>(any()) } throws RuntimeException("Database error")
        
        // When
        val result = repository.getFavorites()
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `update returns true when successful`() = runTest {
        // Given
        val update = BookUpdate(id = 1L, title = "Updated Title")
        coEvery { handler.await<Unit>(any()) } returns Unit
        
        // When
        val result = repository.update(update)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(any()) }
    }
    
    @Test
    fun `update returns false when fails`() = runTest {
        // Given
        val update = BookUpdate(id = 1L, title = "Updated Title")
        coEvery { handler.await<Unit>(any()) } throws RuntimeException("Update failed")
        
        // When
        val result = repository.update(update)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `updateAll returns true when all updates successful`() = runTest {
        // Given
        val updates = listOf(
            BookUpdate(id = 1L, title = "Title 1"),
            BookUpdate(id = 2L, title = "Title 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.updateAll(updates)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `updateAll returns false when transaction fails`() = runTest {
        // Given
        val updates = listOf(
            BookUpdate(id = 1L, title = "Title 1"),
            BookUpdate(id = 2L, title = "Title 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Transaction failed")
        
        // When
        val result = repository.updateAll(updates)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `insertNetworkBooks returns inserted books with IDs`() = runTest {
        // Given
        val books = listOf(
            createTestBook(id = 0L, title = "Book 1"),
            createTestBook(id = 0L, title = "Book 2")
        )
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.insertNetworkBooks(books)
        
        // Then
        assertEquals(books.size, result.size)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `deleteBooks returns true when successful`() = runTest {
        // Given
        val bookIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When
        val result = repository.deleteBooks(bookIds)
        
        // Then
        assertTrue(result)
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `deleteBooks returns false when fails`() = runTest {
        // Given
        val bookIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Delete failed")
        
        // When
        val result = repository.deleteBooks(bookIds)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `setBookCategories updates categories successfully`() = runTest {
        // Given
        val bookId = 1L
        val categoryIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } returns Unit
        
        // When & Then
        assertDoesNotThrow {
            repository.setBookCategories(bookId, categoryIds)
        }
        coVerify { handler.await<Unit>(inTransaction = true, any()) }
    }
    
    @Test
    fun `setBookCategories throws DatabaseError when fails`() = runTest {
        // Given
        val bookId = 1L
        val categoryIds = listOf(1L, 2L, 3L)
        coEvery { handler.await<Unit>(inTransaction = true, any()) } throws RuntimeException("Categories update failed")
        
        // When & Then
        assertFailsWith<IReaderError.DatabaseError> {
            repository.setBookCategories(bookId, categoryIds)
        }
    }
    
    private fun createTestBook(
        id: Long = 1L,
        sourceId: Long = 1L,
        title: String = "Test Book",
        key: String = "test-key",
        author: String = "Test Author",
        favorite: Boolean = false
    ): Book {
        return Book(
            id = id,
            sourceId = sourceId,
            title = title,
            key = key,
            author = author,
            favorite = favorite
        )
    }
    
    private fun createTestLibraryBook(
        id: Long = 1L,
        sourceId: Long = 1L,
        title: String = "Test Library Book",
        key: String = "test-key"
    ): LibraryBook {
        return LibraryBook(
            id = id,
            sourceId = sourceId,
            key = key,
            title = title,
            status = 0L,
            cover = "",
            lastUpdate = 0L
        )
    }
}