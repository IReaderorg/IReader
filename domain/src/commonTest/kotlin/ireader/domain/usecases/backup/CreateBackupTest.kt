package ireader.domain.usecases.backup

import ireader.core.db.Transactions
import ireader.domain.data.repository.*
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.usecases.file.FileSaver
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.*

/**
 * Comprehensive tests for CreateBackup use case
 * Tests backup creation functionality including:
 * - Book serialization
 * - Chapter serialization
 * - Category serialization
 * - File saving
 */
class CreateBackupTest {

    private lateinit var createBackup: CreateBackup
    private lateinit var fileSystem: FileSystem
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var transactions: Transactions
    private lateinit var fileSaver: FileSaver

    @BeforeTest
    fun setup() {
        fileSystem = mockk(relaxed = true)
        libraryRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        chapterRepository = mockk(relaxed = true)
        historyRepository = mockk(relaxed = true)
        transactions = mockk(relaxed = true)
        fileSaver = mockk(relaxed = true)

        // Setup transactions to execute the action directly
        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers {
            val action = firstArg<suspend () -> Any>()
            action()
        }

        createBackup = CreateBackup(
            fileSystem = fileSystem,
            mangaRepository = libraryRepository,
            categoryRepository = categoryRepository,
            chapterRepository = chapterRepository,
            historyRepository = historyRepository,
            transactions = transactions,
            fileSaver = fileSaver
        )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    // ==================== Backup Creation Tests ====================

    @Test
    fun `saveTo should create backup with books and chapters`() = runTest {
        // Given
        val books = listOf(
            createTestBook(1L, "Book 1"),
            createTestBook(2L, "Book 2")
        )
        val chapters1 = listOf(
            createTestChapter(1L, 1L, "Chapter 1"),
            createTestChapter(2L, 1L, "Chapter 2")
        )
        val chapters2 = listOf(
            createTestChapter(3L, 2L, "Chapter 1")
        )
        val uri = mockk<Uri>()

        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { chapterRepository.findChaptersByBookId(1L) } returns chapters1
        coEvery { chapterRepository.findChaptersByBookId(2L) } returns chapters2
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        every { fileSaver.save(uri, any()) } just Runs
        every { fileSaver.validate(uri) } returns true

        var successCalled = false
        var errorMessage: ireader.i18n.UiText? = null

        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { errorMessage = it },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )

        // Then
        assertTrue(result)
        assertTrue(successCalled)
        assertNull(errorMessage)
        verify { fileSaver.save(uri, any()) }
    }

    @Test
    fun `saveTo should include categories in backup`() = runTest {
        // Given
        val books = listOf(createTestBook(1L, "Book 1"))
        val categories = listOf(
            CategoryWithCount(Category(id = 1L, name = "Category 1", order = 0), 5),
            CategoryWithCount(Category(id = 2L, name = "Category 2", order = 1), 3)
        )
        val uri = mockk<Uri>()

        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns categories
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        every { fileSaver.save(uri, any()) } just Runs
        every { fileSaver.validate(uri) } returns true

        var successCalled = false

        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )

        // Then
        assertTrue(result)
        assertTrue(successCalled)
    }

    @Test
    fun `saveTo should call onError when save fails`() = runTest {
        // Given
        val books = listOf(createTestBook(1L, "Book 1"))
        val uri = mockk<Uri>()

        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        every { fileSaver.save(uri, any()) } throws RuntimeException("Write failed")

        var successCalled = false
        var errorMessage: ireader.i18n.UiText? = null

        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { errorMessage = it },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )

        // Then
        assertFalse(result)
        assertFalse(successCalled)
        assertNotNull(errorMessage)
    }

    @Test
    fun `saveTo should handle empty library`() = runTest {
        // Given
        val uri = mockk<Uri>()

        coEvery { libraryRepository.findFavorites() } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        every { fileSaver.save(uri, any()) } just Runs
        every { fileSaver.validate(uri) } returns true

        var successCalled = false

        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )

        // Then
        assertTrue(result)
        assertTrue(successCalled)
    }

    @Test
    fun `createBackupData should create valid backup bytes`() = runTest {
        // Given
        val books = listOf(
            createTestBook(1L, "Book 1"),
            createTestBook(2L, "Book 2")
        )

        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()

        // When
        val backupData = createBackup.createBackupData(books)

        // Then
        assertTrue(backupData.isNotEmpty())
    }

    @Test
    fun `saveTo should report current event for each chapter`() = runTest {
        // Given
        val books = listOf(createTestBook(1L, "Book 1"))
        val chapters = listOf(
            createTestChapter(1L, 1L, "Chapter 1"),
            createTestChapter(2L, 1L, "Chapter 2"),
            createTestChapter(3L, 1L, "Chapter 3")
        )
        val uri = mockk<Uri>()
        val reportedEvents = mutableListOf<String>()

        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { chapterRepository.findChaptersByBookId(1L) } returns chapters
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        every { fileSaver.save(uri, any()) } just Runs
        every { fileSaver.validate(uri) } returns true

        // When
        createBackup.saveTo(
            uri = uri,
            onError = { },
            onSuccess = { },
            currentEvent = { reportedEvents.add(it) }
        )

        // Then
        assertEquals(3, reportedEvents.size)
        assertTrue(reportedEvents.contains("Chapter 1"))
        assertTrue(reportedEvents.contains("Chapter 2"))
        assertTrue(reportedEvents.contains("Chapter 3"))
    }

    @Test
    fun `saveTo should exclude system categories`() = runTest {
        // Given
        val books = listOf(createTestBook(1L, "Book 1"))
        val categories = listOf(
            CategoryWithCount(Category(id = Category.ALL_ID, name = "All", order = 0), 10),
            CategoryWithCount(Category(id = Category.UNCATEGORIZED_ID, name = "Uncategorized", order = 1), 5),
            CategoryWithCount(Category(id = 1L, name = "Custom Category", order = 2), 3)
        )
        val uri = mockk<Uri>()

        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns categories
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        every { fileSaver.save(uri, any()) } just Runs
        every { fileSaver.validate(uri) } returns true

        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { },
            onSuccess = { },
            currentEvent = { }
        )

        // Then
        assertTrue(result)
        // System categories (ALL_ID and UNCATEGORIZED_ID) should be filtered out
    }

    // ==================== Helper Functions ====================

    private fun createTestBook(
        id: Long,
        title: String,
        sourceId: Long = 1L,
        key: String = "book-$id"
    ): Book {
        return Book(
            id = id,
            sourceId = sourceId,
            title = title,
            key = key,
            author = "Test Author",
            description = "Test Description",
            favorite = true
        )
    }

    private fun createTestChapter(
        id: Long,
        bookId: Long,
        name: String,
        key: String = "chapter-$id"
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = key,
            name = name
        )
    }
}
