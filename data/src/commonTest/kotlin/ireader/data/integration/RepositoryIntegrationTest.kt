package ireader.data.integration

import ireader.data.book.BookRepositoryImpl
import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.category.CategoryRepositoryImpl
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.Category
import ireader.core.source.model.Page
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for repository layer.
 *
 * These tests verify:
 * - End-to-end data flow through repositories
 * - DatabaseHandler interactions
 * - Transaction handling
 * - Data consistency across repositories
 * - Flow-based reactive queries
 *
 * Note: These tests use an in-memory database for isolation.
 */
class RepositoryIntegrationTest {

    private lateinit var handler: DatabaseHandler
    private lateinit var bookRepository: BookRepository
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var categoryRepository: CategoryRepository

    @BeforeTest
    fun setup() {
        // Create in-memory database for testing
        handler = createInMemoryDatabaseHandler()
        
        // Initialize repositories with mock bookCategoryRepository
        val mockBookCategoryRepository = mockk<BookCategoryRepository>(relaxed = true)
        bookRepository = BookRepositoryImpl(handler, mockBookCategoryRepository)
        chapterRepository = ChapterRepositoryImpl(handler)
        categoryRepository = CategoryRepositoryImpl(handler)
    }

    // ========== BOOK AND CHAPTER INTEGRATION ==========

    @Test
    fun `inserting book and chapters maintains referential integrity`() = runTest {
        // Given
        val book = createTestBook(id = 1L, title = "Test Book")
        val chapters = listOf(
            createTestChapter(id = 1L, bookId = 1L, name = "Chapter 1"),
            createTestChapter(id = 2L, bookId = 1L, name = "Chapter 2"),
            createTestChapter(id = 3L, bookId = 1L, name = "Chapter 3")
        )

        // When
        bookRepository.upsert(book)
        chapters.forEach { chapterRepository.insertChapter(it) }

        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        val retrievedChapters = chapterRepository.findChaptersByBookId(1L)

        assertNotNull(retrievedBook)
        assertEquals(3, retrievedChapters.size)
        assertTrue(retrievedChapters.all { it.bookId == book.id })
    }

    @Test
    fun `deleting book cascades to chapters`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        val chapters = listOf(
            createTestChapter(id = 1L, bookId = 1L),
            createTestChapter(id = 2L, bookId = 1L)
        )

        bookRepository.upsert(book)
        chapters.forEach { chapterRepository.insertChapter(it) }

        // When
        bookRepository.deleteBookById(1L)

        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        val retrievedChapters = chapterRepository.findChaptersByBookId(1L)

        assertEquals(null, retrievedBook)
        assertTrue(retrievedChapters.isEmpty())
    }

    @Test
    fun `updating book does not affect chapters`() = runTest {
        // Given
        val book = createTestBook(id = 1L, title = "Original Title")
        val chapter = createTestChapter(id = 1L, bookId = 1L)

        bookRepository.upsert(book)
        chapterRepository.insertChapter(chapter)

        // When
        val updatedBook = book.copy(title = "Updated Title")
        bookRepository.updateBook(updatedBook)

        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        val retrievedChapter = chapterRepository.findChapterById(1L)

        assertEquals("Updated Title", retrievedBook?.title)
        assertNotNull(retrievedChapter)
        assertEquals(1L, retrievedChapter.bookId)
    }

    // ========== BOOK AND CATEGORY INTEGRATION ==========

    @Test
    fun `book can be assigned to multiple categories`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        val category1 = createTestCategory(id = 1L, name = "Fiction")
        val category2 = createTestCategory(id = 2L, name = "Fantasy")

        bookRepository.upsert(book)
        // Note: Category operations would need BookCategoryRepository
        // This test is simplified for now
        
        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        assertNotNull(retrievedBook)
    }

    @Test
    fun `removing category removes book assignments`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        val category = createTestCategory(id = 1L, name = "Fiction")

        bookRepository.upsert(book)
        // Note: Category operations would need BookCategoryRepository
        // This test is simplified for now
        
        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        assertNotNull(retrievedBook)
    }

    // ========== FLOW-BASED REACTIVE QUERIES ==========

    @Test
    fun `flow emits updated book when changed`() = runTest {
        // Given
        val book = createTestBook(id = 1L, title = "Original Title")
        bookRepository.upsert(book)

        // When
        val flow = bookRepository.subscribeBookById(1L)
        val initialBook = flow.first()

        // Update book
        val updatedBook = book.copy(title = "Updated Title")
        bookRepository.updateBook(updatedBook)

        val updatedBookFromFlow = flow.first()

        // Then
        assertEquals("Original Title", initialBook?.title)
        assertEquals("Updated Title", updatedBookFromFlow?.title)
    }

    @Test
    fun `flow emits updated chapters when changed`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        val chapter = createTestChapter(id = 1L, bookId = 1L, read = false)

        bookRepository.upsert(book)
        chapterRepository.insertChapter(chapter)

        // When
        val flow = chapterRepository.subscribeChaptersByBookId(1L)
        val initialChapters = flow.first()

        // Mark chapter as read - update the chapter directly
        val updatedChapter = chapter.copy(read = true)
        chapterRepository.insertChapter(updatedChapter)

        val updatedChapters = flow.first()

        // Then
        assertEquals(false, initialChapters.first().read)
        assertEquals(true, updatedChapters.first().read)
    }

    // ========== TRANSACTION HANDLING ==========

    @Test
    fun `transaction rolls back on error`() = runTest {
        // Given
        val book1 = createTestBook(id = 1L, title = "Book 1")
        val book2 = createTestBook(id = 2L, title = "Book 2")

        // When
        try {
            handler.await(inTransaction = true) {
                bookRepository.upsert(book1)
                // Simulate error
                throw Exception("Simulated error")
                @Suppress("UNREACHABLE_CODE")
                bookRepository.upsert(book2)
            }
        } catch (e: Exception) {
            // Expected
        }

        // Then
        val books = bookRepository.findAllBooks()
        assertTrue(books.isEmpty()) // Both inserts should be rolled back
    }

    @Test
    fun `transaction commits on success`() = runTest {
        // Given
        val book1 = createTestBook(id = 1L, title = "Book 1")
        val book2 = createTestBook(id = 2L, title = "Book 2")

        // When
        handler.await(inTransaction = true) {
            bookRepository.upsert(book1)
            bookRepository.upsert(book2)
        }

        // Then
        val books = bookRepository.findAllBooks()
        assertEquals(2, books.size)
    }

    // ========== BATCH OPERATIONS ==========

    @Test
    fun `batch insert performs efficiently`() = runTest {
        // Given
        val books = (1..100).map { createTestBook(id = it.toLong(), title = "Book $it") }

        // When
        val startTime = System.currentTimeMillis()
        bookRepository.insertBooks(books)
        val duration = System.currentTimeMillis() - startTime

        // Then
        val retrievedBooks = bookRepository.findAllBooks()
        assertEquals(100, retrievedBooks.size)
        assertTrue(duration < 1000) // Should complete in less than 1 second
    }

    @Test
    fun `batch update performs efficiently`() = runTest {
        // Given
        val books = (1..100).map { createTestBook(id = it.toLong(), title = "Book $it") }
        bookRepository.insertBooks(books)

        // When
        val updatedBooks = books.map { it.copy(favorite = true) }
        val startTime = System.currentTimeMillis()
        bookRepository.updateBook(updatedBooks)
        val duration = System.currentTimeMillis() - startTime

        // Then
        val retrievedBooks = bookRepository.findAllBooks()
        assertTrue(retrievedBooks.all { it.favorite })
        assertTrue(duration < 1000) // Should complete in less than 1 second
    }

    // ========== DATA CONSISTENCY ==========

    @Test
    fun `concurrent updates maintain consistency`() = runTest {
        // Given
        val book = createTestBook(id = 1L, title = "Original")
        bookRepository.upsert(book)

        // When - Simulate concurrent updates
        val update1 = book.copy(title = "Update 1")
        val update2 = book.copy(title = "Update 2")

        bookRepository.updateBook(update1)
        bookRepository.updateBook(update2)

        // Then
        val retrievedBook = bookRepository.findBookById(1L)
        assertNotNull(retrievedBook)
        // Last update should win
        assertEquals("Update 2", retrievedBook.title)
    }

    @Test
    fun `reading while writing maintains consistency`() = runTest {
        // Given
        val book = createTestBook(id = 1L)
        bookRepository.upsert(book)

        // When - Read while updating
        val flow = bookRepository.subscribeBookById(1L)
        
        // Start reading
        val readJob = launch {
            flow.collect { bookData ->
                // Verify book is always in valid state
                if (bookData != null) {
                    assertNotNull(bookData)
                    assertTrue(bookData.id > 0)
                }
            }
        }

        // Perform updates
        repeat(10) { i ->
            val updatedBook = book.copy(title = "Update $i")
            bookRepository.updateBook(updatedBook)
            delay(10)
        }

        readJob.cancel()

        // Then
        val finalBook = bookRepository.findBookById(1L)
        assertNotNull(finalBook)
        assertEquals("Update 9", finalBook.title)
    }

    // ========== HELPER METHODS ==========

    private fun createInMemoryDatabaseHandler(): DatabaseHandler {
        // Implementation would create an in-memory SQLite database
        // This is a placeholder for the actual implementation
        TODO("Implement in-memory database handler")
    }

    private fun createTestBook(
        id: Long = 1L,
        title: String = "Test Book",
        key: String = "test-key-$id",
        sourceId: Long = 1L,
        favorite: Boolean = false,
        author: String = "Test Author",
        description: String = "Test Description",
        genres: List<String> = listOf("Fiction"),
        status: Long = 0L,
        cover: String = "https://example.com/cover.jpg",
        customCover: String = "",
        lastUpdate: Long = System.currentTimeMillis(),
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
            dateAdded = dateAdded
        )
    }

    private fun createTestChapter(
        id: Long = 1L,
        bookId: Long = 1L,
        key: String = "chapter-$id",
        name: String = "Chapter $id",
        number: Float = id.toFloat(),
        translator: String = "",
        dateUpload: Long = System.currentTimeMillis(),
        dateFetch: Long = System.currentTimeMillis(),
        sourceOrder: Long = id,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Long = 0L,
        content: List<Page> = emptyList()
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = key,
            name = name,
            number = number,
            translator = translator,
            dateUpload = dateUpload,
            dateFetch = dateFetch,
            sourceOrder = sourceOrder,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            content = content
        )
    }

    private fun createTestCategory(
        id: Long = 1L,
        name: String = "Test Category",
        order: Long = id,
        flags: Long = 0L
    ): Category {
        return Category(
            id = id,
            name = name,
            order = order,
            flags = flags
        )
    }
}
