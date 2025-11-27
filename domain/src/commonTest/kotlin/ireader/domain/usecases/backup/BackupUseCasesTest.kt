package ireader.domain.usecases.backup

import ireader.core.db.Transactions
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.file.FileSaver
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.*

/**
 * Unit tests for backup and restore functionality
 * Tests critical backup operations including creation, validation, and restoration
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
        fileSystem = mockk()
        libraryRepository = mockk()
        categoryRepository = mockk()
        chapterRepository = mockk()
        historyRepository = mockk()
        transactions = mockk()
        fileSaver = mockk()
        
        createBackup = CreateBackup(
            fileSystem,
            libraryRepository,
            categoryRepository,
            chapterRepository,
            historyRepository,
            transactions,
            fileSaver
        )
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `saveTo should create backup successfully`() = runTest {
        // Given
        val uri = Uri("file:///backup/library.backup")
        val books = listOf(createTestBook(1L), createTestBook(2L))
        val categories = listOf(createTestCategory(1L), createTestCategory(2L))
        
        coEvery { transactions.run<Any>(any()) } returns Unit
        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { categoryRepository.findAll() } returns categories.map { 
            ireader.domain.models.entities.CategoryWithCount(it, 0) 
        }
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { fileSaver.save(uri, any()) } just Runs
        coEvery { fileSaver.validate(uri) } returns true
        
        var successCalled = false
        var errorCalled = false
        
        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { errorCalled = true },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )
        
        // Then
        assertTrue(result)
        assertTrue(successCalled)
        assertFalse(errorCalled)
        coVerify { fileSaver.save(uri, any()) }
        coVerify { fileSaver.validate(uri) }
    }
    
    @Test
    fun `saveTo should handle backup failure`() = runTest {
        // Given
        val uri = Uri("file:///backup/library.backup")
        coEvery { transactions.run<Any>(any()) } throws Exception("Database error")
        
        var errorCalled = false
        var successCalled = false
        
        // When
        val result = createBackup.saveTo(
            uri = uri,
            onError = { errorCalled = true },
            onSuccess = { successCalled = true },
            currentEvent = { }
        )
        
        // Then
        assertFalse(result)
        assertTrue(errorCalled)
        assertFalse(successCalled)
    }
    
    @Test
    fun `saveTo should handle empty library`() = runTest {
        // Given
        val uri = Uri("file:///backup/library.backup")
        
        coEvery { transactions.run<Any>(any()) } returns Unit
        coEvery { libraryRepository.findFavorites() } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { fileSaver.save(uri, any()) } just Runs
        coEvery { fileSaver.validate(uri) } returns true
        
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
    fun `createBackupData should generate backup for specific books`() = runTest {
        // Given
        val books = listOf(
            createTestBook(1L),
            createTestBook(2L),
            createTestBook(3L)
        )
        
        coEvery { transactions.run<Any>(any()) } returns Unit
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns listOf(
            createTestChapter(1L, 1L)
        )
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        
        // When
        val backupData = createBackup.createBackupData(books)
        
        // Then
        assertNotNull(backupData)
        assertTrue(backupData.isNotEmpty())
    }
    
    @Test
    fun `createBackupData should include chapters and history`() = runTest {
        // Given
        val book = createTestBook(1L)
        val chapters = listOf(
            createTestChapter(1L, book.id),
            createTestChapter(2L, book.id)
        )
        
        coEvery { transactions.run<Any>(any()) } returns Unit
        coEvery { chapterRepository.findChaptersByBookId(book.id) } returns chapters
        coEvery { historyRepository.findHistoriesByBookId(book.id) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(book.id) } returns emptyList()
        coEvery { categoryRepository.findAll() } returns emptyList()
        
        // When
        val backupData = createBackup.createBackupData(listOf(book))
        
        // Then
        assertNotNull(backupData)
        coVerify { chapterRepository.findChaptersByBookId(book.id) }
    }
    
    @Test
    fun `saveTo should report progress during backup`() = runTest {
        // Given
        val uri = Uri("file:///backup/library.backup")
        val books = listOf(createTestBook(1L))
        val chapters = listOf(
            createTestChapter(1L, 1L),
            createTestChapter(2L, 1L)
        )
        
        coEvery { transactions.run<Any>(any()) } returns Unit
        coEvery { libraryRepository.findFavorites() } returns books
        coEvery { categoryRepository.findAll() } returns emptyList()
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns chapters
        coEvery { historyRepository.findHistoriesByBookId(any()) } returns emptyList()
        coEvery { categoryRepository.getCategoriesByMangaId(any()) } returns emptyList()
        coEvery { fileSaver.save(uri, any()) } just Runs
        coEvery { fileSaver.validate(uri) } returns true
        
        val events = mutableListOf<String>()
        
        // When
        createBackup.saveTo(
            uri = uri,
            onError = { },
            onSuccess = { },
            currentEvent = { events.add(it) }
        )
        
        // Then
        assertTrue(events.isNotEmpty())
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
    
    private fun createTestCategory(id: Long): Category {
        return Category(
            id = id,
            name = "Category $id",
            order = id.toInt(),
            flags = 0
        )
    }
    
    private fun createTestChapter(id: Long, bookId: Long): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = "chapter-$id",
            name = "Chapter $id",
            dateUpload = System.currentTimeMillis(),
            number = id.toFloat(),
            sourceOrder = id.toInt(),
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            dateFetch = System.currentTimeMillis(),
            translator = null,
            content = emptyList()
        )
    }
}

/**
 * Unit tests for RestoreBackup use case
 */
class RestoreBackupTest {
    
    private lateinit var restoreBackup: RestoreBackup
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var chapterRepository: ChapterRepository
    
    @BeforeTest
    fun setup() {
        libraryRepository = mockk()
        categoryRepository = mockk()
        chapterRepository = mockk()
        restoreBackup = RestoreBackup(libraryRepository, categoryRepository, chapterRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `restore should import books from backup`() = runTest {
        // Given
        val backupData = ByteArray(100) // Mock backup data
        coEvery { libraryRepository.insertBooks(any()) } just Runs
        coEvery { categoryRepository.insertCategories(any()) } just Runs
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        val result = restoreBackup.restore(backupData)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `restore should handle corrupted backup data`() = runTest {
        // Given
        val corruptedData = ByteArray(10) // Invalid backup data
        
        // When
        val result = restoreBackup.restore(corruptedData)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `restore should handle database errors`() = runTest {
        // Given
        val backupData = ByteArray(100)
        coEvery { libraryRepository.insertBooks(any()) } throws Exception("Database error")
        
        // When
        val result = restoreBackup.restore(backupData)
        
        // Then
        assertTrue(result.isFailure)
    }
}
