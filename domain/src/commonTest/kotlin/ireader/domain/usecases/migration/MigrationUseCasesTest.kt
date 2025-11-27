package ireader.domain.usecases.migration

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.source.MigrateToSourceUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for migration use cases
 * Tests book migration between sources with chapter mapping
 */
class MigrationUseCasesTest {
    
    private lateinit var migrateToSourceUseCase: MigrateToSourceUseCase
    private lateinit var bookRepository: BookRepository
    private lateinit var chapterRepository: ChapterRepository
    
    @BeforeTest
    fun setup() {
        bookRepository = mockk()
        chapterRepository = mockk()
        migrateToSourceUseCase = MigrateToSourceUseCase(
            bookRepository,
            chapterRepository
        )
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `migrateBook should migrate to new source successfully`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val newSourceId = 200L
        val newBookKey = "new-book-key"
        
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1f),
            createTestChapter(2L, oldBook.id, number = 2f),
            createTestChapter(3L, oldBook.id, number = 3f)
        )
        
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1f),
            createTestChapter(102L, 0L, number = 2f),
            createTestChapter(103L, 0L, number = 3f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        val result = migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = newSourceId,
            newBookKey = newBookKey,
            newChapters = newChapters
        )
        
        // Then
        assertTrue(result.isSuccess)
        coVerify {
            bookRepository.updateBook(match { book ->
                book.sourceId == newSourceId && book.key == newBookKey
            })
        }
    }
    
    @Test
    fun `migrateBook should preserve read status`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1f, read = true),
            createTestChapter(2L, oldBook.id, number = 2f, read = true),
            createTestChapter(3L, oldBook.id, number = 3f, read = false)
        )
        
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1f),
            createTestChapter(102L, 0L, number = 2f),
            createTestChapter(103L, 0L, number = 3f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = newChapters
        )
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { chapters ->
                chapters[0].read == true &&
                chapters[1].read == true &&
                chapters[2].read == false
            })
        }
    }
    
    @Test
    fun `migrateBook should preserve bookmarks`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1f, bookmark = true),
            createTestChapter(2L, oldBook.id, number = 2f, bookmark = false)
        )
        
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1f),
            createTestChapter(102L, 0L, number = 2f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = newChapters
        )
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { chapters ->
                chapters[0].bookmark == true &&
                chapters[1].bookmark == false
            })
        }
    }
    
    @Test
    fun `migrateBook should handle missing chapters gracefully`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1f),
            createTestChapter(2L, oldBook.id, number = 2f),
            createTestChapter(3L, oldBook.id, number = 3f)
        )
        
        // New source has fewer chapters
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1f),
            createTestChapter(102L, 0L, number = 2f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        val result = migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = newChapters
        )
        
        // Then
        assertTrue(result.isSuccess)
        coVerify {
            chapterRepository.insertChapters(match { chapters ->
                chapters.size == 2
            })
        }
    }
    
    @Test
    fun `migrateBook should fail when book not found`() = runTest {
        // Given
        val bookId = 999L
        coEvery { bookRepository.findBookById(bookId) } returns null
        
        // When
        val result = migrateToSourceUseCase.migrate(
            oldBookId = bookId,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = emptyList()
        )
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `migrateBook should handle database errors`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val error = Exception("Database error")
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns emptyList()
        coEvery { bookRepository.updateBook(any()) } throws error
        
        // When
        val result = migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = emptyList()
        )
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
    
    @Test
    fun `migrateBook should map chapters by number`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1.0f, read = true),
            createTestChapter(2L, oldBook.id, number = 1.5f, read = false),
            createTestChapter(3L, oldBook.id, number = 2.0f, read = true)
        )
        
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1.0f),
            createTestChapter(102L, 0L, number = 1.5f),
            createTestChapter(103L, 0L, number = 2.0f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = newChapters
        )
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { chapters ->
                chapters[0].number == 1.0f && chapters[0].read == true &&
                chapters[1].number == 1.5f && chapters[1].read == false &&
                chapters[2].number == 2.0f && chapters[2].read == true
            })
        }
    }
    
    @Test
    fun `migrateBook should preserve last page read`() = runTest {
        // Given
        val oldBook = createTestBook(1L, sourceId = 100L)
        val oldChapters = listOf(
            createTestChapter(1L, oldBook.id, number = 1f, lastPageRead = 42L)
        )
        
        val newChapters = listOf(
            createTestChapter(101L, 0L, number = 1f)
        )
        
        coEvery { bookRepository.findBookById(oldBook.id) } returns oldBook
        coEvery { chapterRepository.findChaptersByBookId(oldBook.id) } returns oldChapters
        coEvery { bookRepository.updateBook(any()) } returns 1
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        migrateToSourceUseCase.migrate(
            oldBookId = oldBook.id,
            newSourceId = 200L,
            newBookKey = "new-key",
            newChapters = newChapters
        )
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { chapters ->
                chapters[0].lastPageRead == 42L
            })
        }
    }
    
    private fun createTestBook(id: Long, sourceId: Long): Book {
        return Book(
            id = id,
            sourceId = sourceId,
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
    
    private fun createTestChapter(
        id: Long,
        bookId: Long,
        number: Float,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Long = 0L
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = "chapter-$id",
            name = "Chapter $number",
            dateUpload = System.currentTimeMillis(),
            number = number,
            sourceOrder = number.toInt(),
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            dateFetch = System.currentTimeMillis(),
            translator = null,
            content = emptyList()
        )
    }
}
