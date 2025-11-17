package ireader.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ireader.data.book.BookRepositoryImpl
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for BookRepository implementation.
 *
 * Tests cover:
 * - CRUD operations
 * - Error handling
 * - Flow-based reactive queries
 * - Partial updates
 * - Transaction handling
 * - Edge cases
 */
class BookRepositoryTest {

    private lateinit var repository: BookRepository
    private lateinit var handler: DatabaseHandler

    @BeforeTest
    fun setup() {
        handler = mockk(relaxed = true)
        repository = BookRepositoryImpl(handler)
    }

    // ========== GET OPERATIONS ==========

    @Test
    fun `findBookById returns book when found`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        coEvery { handler.awaitOne<Book>(any()) } returns expectedBook

        // When
        val result = repository.findBookById(bookId)

        // Then
        assertNotNull(result)
        assertEquals(expectedBook.id, result.id)
        assertEquals(expectedBook.title, result.title)
        coVerify(exactly = 1) { handler.awaitOne<Book>(any()) }
    }

    @Test
    fun `findBookById returns null when not found`() = runTest {
        // Given
        val bookId = 999L
        coEvery { handler.awaitOne<Book>(any()) } throws NoSuchElementException()

        // When
        val result = repository.findBookById(bookId)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `subscribeBookById returns flow of book`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        every { handler.subscribeToOne<Book>(any()) } returns flowOf(expectedBook)

        // When
        val result = repository.subscribeBookById(bookId).first()

        // Then
        assertNotNull(result)
        assertEquals(expectedBook.id, result.id)
    }

    @Test
    fun `findAllBooks returns list of books`() = runTest {
        // Given
        val expectedBooks = listOf(
            createTestBook(id = 1L, title = "Book 1"),
            createTestBook(id = 2L, title = "Book 2"),
            createTestBook(id = 3L, title = "Book 3")
        )
        coEvery { handler.awaitList<Book>(any()) } returns expectedBooks

        // When
        val result = repository.findAllBooks()

        // Then
        assertEquals(3, result.size)
        assertEquals(expectedBooks, result)
    }

    @Test
    fun `findAllBooks returns empty list when no books`() = runTest {
        // Given
        coEvery { handler.awaitList<Book>(any()) } returns emptyList()

        // When
        val result = repository.findAllBooks()

        // Then
        assertTrue(result.isEmpty())
    }

    // ========== INSERT OPERATIONS ==========

    @Test
    fun `insertBook successfully inserts book`() = runTest {
        // Given
        val book = createTestBook()
        coEvery { handler.await<Long>(any()) } returns book.id

        // When
        val result = repository.insertBook(book)

        // Then
        assertEquals(book.id, result)
        coVerify(exactly = 1) { handler.await<Long>(any()) }
    }

    @Test
    fun `insertBooks successfully inserts multiple books`() = runTest {
        // Given
        val books = listOf(
            createTestBook(id = 1L),
            createTestBook(id = 2L),
            createTestBook(id = 3L)
        )
        coEvery { handler.await<List<Long>>(any()) } returns listOf(1L, 2L, 3L)

        // When
        val result = repository.insertBooks(books)

        // Then
        assertEquals(3, result.size)
        coVerify(exactly = 1) { handler.await<List<Long>>(any()) }
    }

    @Test
    fun `insertBook handles database error`() = runTest {
        // Given
        val book = createTestBook()
        coEvery { handler.await<Long>(any()) } throws Exception("Database error")

        // When/Then
        assertFailsWith<Exception> {
            repository.insertBook(book)
        }
    }

    // ========== UPDATE OPERATIONS ==========

    @Test
    fun `updateBook successfully updates book`() = runTest {
        // Given
        val book = createTestBook(id = 1L, title = "Updated Title")
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.updateBook(book)

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    @Test
    fun `updateBooks successfully updates multiple books`() = runTest {
        // Given
        val books = listOf(
            createTestBook(id = 1L),
            createTestBook(id = 2L)
        )
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.updateBook(books)

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    @Test
    fun `updatePartial successfully updates book fields`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        coEvery { handler.await<Long>(any()) } returns book.id

        // When
        val result = repository.updatePartial(book)

        // Then
        assertEquals(book.id, result)
    }

    // ========== DELETE OPERATIONS ==========

    @Test
    fun `deleteBookById successfully deletes book`() = runTest {
        // Given
        val bookId = 1L
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.deleteBookById(bookId)

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    @Test
    fun `deleteBooks successfully deletes multiple books`() = runTest {
        // Given
        val books = listOf(
            createTestBook(id = 1L),
            createTestBook(id = 2L)
        )
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.deleteBooks(books)

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    @Test
    fun `deleteAllBooks successfully deletes all books`() = runTest {
        // Given
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.deleteAllBooks()

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    // ========== SEARCH OPERATIONS ==========

    @Test
    fun `findBookByKey returns book when found`() = runTest {
        // Given
        val key = "test-key"
        val expectedBook = createTestBook(key = key)
        coEvery { handler.awaitOne<Book>(any()) } returns expectedBook

        // When
        val result = repository.findBookByKey(key)

        // Then
        assertNotNull(result)
        assertEquals(key, result.key)
    }

    @Test
    fun `findBooksByKey returns list of books with matching key`() = runTest {
        // Given
        val key = "test-key"
        val expectedBooks = listOf(
            createTestBook(id = 1L, key = key),
            createTestBook(id = 2L, key = key)
        )
        coEvery { handler.awaitList<Book>(any()) } returns expectedBooks

        // When
        val result = repository.findBooksByKey(key)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.key == key })
    }

    // ========== LIBRARY OPERATIONS ==========

    @Test
    fun `findAllInLibraryBooks returns favorite books`() = runTest {
        // Given
        val expectedBooks = listOf(
            createTestBook(id = 1L, favorite = true),
            createTestBook(id = 2L, favorite = true)
        )
        coEvery { handler.awaitList<Book>(any()) } returns expectedBooks

        // When
        val result = repository.findAllInLibraryBooks()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.favorite })
    }

    @Test
    fun `deleteNotInLibraryBooks removes non-favorite books`() = runTest {
        // Given
        coEvery { handler.await<Unit>(any()) } returns Unit

        // When
        repository.deleteNotInLibraryBooks()

        // Then
        coVerify(exactly = 1) { handler.await<Unit>(any()) }
    }

    // ========== DUPLICATE DETECTION ==========

    @Test
    fun `findDuplicateBook returns book when duplicate exists`() = runTest {
        // Given
        val title = "Test Book"
        val sourceId = 1L
        val expectedBook = createTestBook(title = title, sourceId = sourceId)
        coEvery { handler.awaitOne<Book>(any()) } returns expectedBook

        // When
        val result = repository.findDuplicateBook(title, sourceId)

        // Then
        assertNotNull(result)
        assertEquals(title, result.title)
        assertEquals(sourceId, result.sourceId)
    }

    @Test
    fun `findDuplicateBook returns null when no duplicate`() = runTest {
        // Given
        val title = "Unique Book"
        val sourceId = 1L
        coEvery { handler.awaitOne<Book>(any()) } throws NoSuchElementException()

        // When
        val result = repository.findDuplicateBook(title, sourceId)

        // Then
        assertEquals(null, result)
    }

    // ========== HELPER METHODS ==========

    private fun createTestBook(
        id: Long = 1L,
        title: String = "Test Book",
        key: String = "test-key",
        sourceId: Long = 1L,
        favorite: Boolean = false,
        author: String = "Test Author",
        description: String = "Test Description",
        genres: List<String> = listOf("Fiction"),
        status: Int = 0,
        cover: String = "https://example.com/cover.jpg",
        customCover: String = "",
        lastUpdate: Long = System.currentTimeMillis(),
        lastInit: Long = System.currentTimeMillis(),
        dateAdded: Long = System.currentTimeMillis()
    ): Book {
        return Book(
            id = id,
            title = title,
            key = key,
            sourceId = sourceId,
            favorite = favorite,
            author = author,
            description = description,
            genres = genres,
            status = status,
            cover = cover,
            customCover = customCover,
            lastUpdate = lastUpdate,
            lastInit = lastInit,
            dateAdded = dateAdded
        )
    }
}
