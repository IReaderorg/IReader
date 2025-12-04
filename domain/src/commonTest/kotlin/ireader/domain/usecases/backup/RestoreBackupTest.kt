//package ireader.domain.usecases.backup
//
//import ireader.core.db.Transactions
//import ireader.domain.data.repository.*
//import ireader.domain.models.common.Uri
//import ireader.domain.models.entities.Book
//import ireader.domain.models.entities.BookCategory
//import ireader.domain.models.entities.Category
//import ireader.domain.models.entities.CategoryWithCount
//import ireader.domain.models.entities.Chapter
//import ireader.domain.preferences.prefs.LibraryPreferences
//import ireader.domain.usecases.backup.backup.*
//import ireader.domain.usecases.file.FileSaver
//import ireader.core.prefs.Preference
//import io.mockk.*
//import kotlinx.coroutines.test.runTest
//import kotlinx.serialization.ExperimentalSerializationApi
//import kotlinx.serialization.encodeToByteArray
//import kotlinx.serialization.protobuf.ProtoBuf
//import kotlin.test.*
//
///**
// * Comprehensive tests for RestoreBackup use case
// * Tests backup restoration functionality including:
// * - Book restoration (new and existing)
// * - Chapter restoration with merge logic
// * - Category restoration and mapping
// * - Error handling scenarios
// */
//class RestoreBackupTest {
//
//    private lateinit var restoreBackup: RestoreBackup
//    private lateinit var bookRepository: BookRepository
//    private lateinit var categoryRepository: CategoryRepository
//    private lateinit var chapterRepository: ChapterRepository
//    private lateinit var bookCategoryRepository: BookCategoryRepository
//    private lateinit var libraryPreferences: LibraryPreferences
//    private lateinit var transactions: Transactions
//    private lateinit var fileSaver: FileSaver
//    private lateinit var perCategorySettingsPref: Preference<Boolean>
//
//    @BeforeTest
//    fun setup() {
//        bookRepository = mockk(relaxed = true)
//        categoryRepository = mockk(relaxed = true)
//        chapterRepository = mockk(relaxed = true)
//        bookCategoryRepository = mockk(relaxed = true)
//        libraryPreferences = mockk(relaxed = true)
//        transactions = mockk(relaxed = true)
//        fileSaver = mockk(relaxed = true)
//        perCategorySettingsPref = mockk(relaxed = true)
//
//        // Setup transactions to execute the action directly
//        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers {
//            val action = firstArg<suspend () -> Any>()
//            action()
//        }
//
//        // Setup library preferences
//        every { libraryPreferences.perCategorySettings() } returns perCategorySettingsPref
//
//        restoreBackup = RestoreBackup(
//            bookRepository = bookRepository,
//            categoryRepository = categoryRepository,
//            chapterRepository = chapterRepository,
//            mangaCategoryRepository = bookCategoryRepository,
//            libraryPreferences = libraryPreferences,
//            transactions = transactions,
//            fileSaver = fileSaver
//        )
//    }
//
//    @AfterTest
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    // ==================== Book Restoration Tests ====================
//
//    @Test
//    fun `restoreFromBytes should restore new book successfully`() = runTest {
//        // Given
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            title = "Test Book"
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        coEvery { bookRepository.find(any(), any()) } returns null
//        coEvery { bookRepository.upsert(any()) } returns 1L
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify { bookRepository.upsert(match { it.key == "test-book-key" && it.title == "Test Book" }) }
//    }
//
//    @Test
//    fun `restoreFromBytes should update existing book when backup is newer`() = runTest {
//        // Given
//        val existingBook = createTestBook(
//            id = 1L,
//            key = "test-book-key",
//            initialized = false,
//            lastUpdate = 1000L
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            title = "Updated Book",
//            initialized = true,
//            lastUpdate = 2000L
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        coEvery { bookRepository.find("test-book-key", 1L) } returns existingBook
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify { bookRepository.updateBook(match<Book> { it.title == "Updated Book" && it.favorite }) }
//    }
//
//    @Test
//    fun `restoreFromBytes should not update existing favorite book when initialized matches`() = runTest {
//        // Given
//        val existingBook = createTestBook(
//            id = 1L,
//            key = "test-book-key",
//            initialized = true,
//            favorite = true
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            title = "Test Book",
//            initialized = true
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        coEvery { bookRepository.find("test-book-key", 1L) } returns existingBook
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify(exactly = 0) { bookRepository.updateBook(any<Book>()) }
//    }
//
//    // ==================== Chapter Restoration Tests ====================
//
//    @Test
//    fun `restoreFromBytes should restore chapters for new book`() = runTest {
//        // Given
//        val chapterProtos = listOf(
//            createTestChapterProto("ch1", "Chapter 1", read = true),
//            createTestChapterProto("ch2", "Chapter 2", read = false)
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            chapters = chapterProtos
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        val restoredBook = createTestBook(id = 1L, key = "test-book-key")
//        coEvery { bookRepository.find(any(), any()) } returns null andThen restoredBook
//        coEvery { bookRepository.upsert(any()) } returns 1L
//        coEvery { chapterRepository.findChaptersByBookId(1L) } returns emptyList()
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify {
//            chapterRepository.insertChapters(match { chapters ->
//                chapters.size == 2 &&
//                chapters.any { it.key == "ch1" && it.read } &&
//                chapters.any { it.key == "ch2" && !it.read }
//            })
//        }
//    }
//
//    @Test
//    fun `restoreFromBytes should merge chapters when backup is newer`() = runTest {
//        // Given
//        val existingBook = createTestBook(id = 1L, key = "test-book-key", lastUpdate = 1000L)
//        val existingChapters = listOf(
//            createTestChapter(id = 1L, bookId = 1L, key = "ch1", read = true, bookmark = false),
//            createTestChapter(id = 2L, bookId = 1L, key = "ch2", read = false, bookmark = true)
//        )
//
//        val chapterProtos = listOf(
//            createTestChapterProto("ch1", "Chapter 1 Updated", read = false, bookmark = true),
//            createTestChapterProto("ch2", "Chapter 2 Updated", read = true, bookmark = false),
//            createTestChapterProto("ch3", "Chapter 3 New", read = false, bookmark = false)
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            lastUpdate = 2000L,
//            chapters = chapterProtos
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        coEvery { bookRepository.find("test-book-key", 1L) } returns existingBook
//        coEvery { chapterRepository.findChaptersByBookId(1L) } returns existingChapters
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify { chapterRepository.deleteChapters(existingChapters) }
//        coVerify {
//            chapterRepository.insertChapters(match { chapters ->
//                // ch1: backup read=false, db read=true -> merged read=true
//                // ch1: backup bookmark=true, db bookmark=false -> merged bookmark=true
//                val ch1 = chapters.find { it.key == "ch1" }
//                val ch2 = chapters.find { it.key == "ch2" }
//                val ch3 = chapters.find { it.key == "ch3" }
//                ch1 != null && ch1.read && ch1.bookmark &&
//                ch2 != null && ch2.read && ch2.bookmark &&
//                ch3 != null && !ch3.read && !ch3.bookmark
//            })
//        }
//    }
//
//    @Test
//    fun `restoreFromBytes should update existing chapters when database is newer`() = runTest {
//        // Given
//        val existingBook = createTestBook(id = 1L, key = "test-book-key", lastUpdate = 2000L)
//        val existingChapters = listOf(
//            createTestChapter(id = 1L, bookId = 1L, key = "ch1", read = false, bookmark = false),
//            createTestChapter(id = 2L, bookId = 1L, key = "ch2", read = false, bookmark = false)
//        )
//
//        val chapterProtos = listOf(
//            createTestChapterProto("ch1", "Chapter 1", read = true, bookmark = false),
//            createTestChapterProto("ch2", "Chapter 2", read = false, bookmark = true)
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            lastUpdate = 1000L,
//            chapters = chapterProtos
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        coEvery { bookRepository.find("test-book-key", 1L) } returns existingBook
//        coEvery { chapterRepository.findChaptersByBookId(1L) } returns existingChapters
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify(exactly = 0) { chapterRepository.deleteChapters(any()) }
//        coVerify {
//            chapterRepository.insertChapters(match { chapters ->
//                // Merge read/bookmark status from backup into existing
//                val ch1 = chapters.find { it.key == "ch1" }
//                val ch2 = chapters.find { it.key == "ch2" }
//                ch1 != null && ch1.read && // backup read=true merged
//                ch2 != null && ch2.bookmark // backup bookmark=true merged
//            })
//        }
//    }
//
//    @Test
//    fun `restoreFromBytes should skip chapters when backup has empty chapters`() = runTest {
//        // Given
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            chapters = emptyList()
//        )
//        val backupData = createBackupData(listOf(bookProto))
//
//        val restoredBook = createTestBook(id = 1L, key = "test-book-key")
//        coEvery { bookRepository.find(any(), any()) } returns null andThen restoredBook
//        coEvery { bookRepository.upsert(any()) } returns 1L
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify(exactly = 0) { chapterRepository.insertChapters(any()) }
//    }
//
//    // ==================== Category Restoration Tests ====================
//
//    @Test
//    fun `restoreFromBytes should restore new categories`() = runTest {
//        // Given
//        val categoryProtos = listOf(
//            createTestCategoryProto("Category 1", order = 0),
//            createTestCategoryProto("Category 2", order = 1)
//        )
//        val backupData = createBackupData(emptyList(), categoryProtos)
//
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify {
//            categoryRepository.insert(match<List<Category>> { categories ->
//                categories.size == 2 &&
//                categories.any { it.name == "Category 1" } &&
//                categories.any { it.name == "Category 2" }
//            })
//        }
//    }
//
//    @Test
//    fun `restoreFromBytes should skip existing categories with same name`() = runTest {
//        // Given
//        val existingCategories = listOf(
//            CategoryWithCount(Category(id = 1L, name = "Category 1", order = 0), 5)
//        )
//        val categoryProtos = listOf(
//            createTestCategoryProto("Category 1", order = 0),
//            createTestCategoryProto("Category 2", order = 1)
//        )
//        val backupData = createBackupData(emptyList(), categoryProtos)
//
//        coEvery { categoryRepository.findAll() } returns existingCategories
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify {
//            categoryRepository.insert(match<List<Category>> { categories ->
//                categories.size == 1 && categories[0].name == "Category 2"
//            })
//        }
//    }
//
//    @Test
//    fun `restoreFromBytes should handle case-insensitive category matching`() = runTest {
//        // Given
//        val existingCategories = listOf(
//            CategoryWithCount(Category(id = 1L, name = "CATEGORY 1", order = 0), 5)
//        )
//        val categoryProtos = listOf(
//            createTestCategoryProto("category 1", order = 0)
//        )
//        val backupData = createBackupData(emptyList(), categoryProtos)
//
//        coEvery { categoryRepository.findAll() } returns existingCategories
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        // Should not insert because "category 1" matches "CATEGORY 1" case-insensitively
//        coVerify(exactly = 0) { categoryRepository.insert(any<List<Category>>()) }
//    }
//
//    @Test
//    fun `restoreFromBytes should assign book to restored categories`() = runTest {
//        // Given
//        val categoryProtos = listOf(
//            createTestCategoryProto("Category 1", order = 0),
//            createTestCategoryProto("Category 2", order = 1)
//        )
//        val bookProto = createTestBookProto(
//            sourceId = 1L,
//            key = "test-book-key",
//            categories = listOf(0L, 1L) // References to category orders
//        )
//        val backupData = createBackupData(listOf(bookProto), categoryProtos)
//
//        val existingCategories = listOf(
//            CategoryWithCount(Category(id = 10L, name = "Category 1", order = 0), 0),
//            CategoryWithCount(Category(id = 20L, name = "Category 2", order = 1), 0)
//        )
//
//        coEvery { bookRepository.find(any(), any()) } returns null andThen createTestBook(id = 1L, key = "test-book-key")
//        coEvery { bookRepository.upsert(any()) } returns 1L
//        coEvery { categoryRepository.findAll() } returns existingCategories
//        coEvery { chapterRepository.findChaptersByBookId(any()) } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify {
//            bookCategoryRepository.insertAll(match { bookCategories ->
//                bookCategories.size == 2 &&
//                bookCategories.any { it.bookId == 1L && it.categoryId == 10L } &&
//                bookCategories.any { it.bookId == 1L && it.categoryId == 20L }
//            })
//        }
//    }
//
//    // ==================== Error Handling Tests ====================
//
//    @Test
//    fun `restoreFromBytes should return error for invalid backup data`() = runTest {
//        // Given - completely invalid data that can't be parsed by any backup format
//        val invalidData = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0xFF.toByte(), 0xFE.toByte())
//
//        // When
//        val result = restoreBackup.restoreFromBytes(invalidData)
//
//        // Then - should return error because data can't be parsed
//        assertTrue(result is RestoreBackup.Result.Error, "Expected error result for invalid backup data, got: $result")
//    }
//
//    @Test
//    fun `restoreFromBytes should handle empty backup gracefully`() = runTest {
//        // Given
//        val backupData = createBackupData(emptyList(), emptyList())
//
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//    }
//
//    @Test
//    fun `restoreFromBytes should continue restoration when chapter restore fails`() = runTest {
//        // Given
//        val bookProto1 = createTestBookProto(
//            sourceId = 1L,
//            key = "book1",
//            chapters = listOf(createTestChapterProto("ch1", "Chapter 1"))
//        )
//        val bookProto2 = createTestBookProto(
//            sourceId = 1L,
//            key = "book2",
//            chapters = listOf(createTestChapterProto("ch2", "Chapter 2"))
//        )
//        val backupData = createBackupData(listOf(bookProto1, bookProto2))
//
//        val book1 = createTestBook(id = 1L, key = "book1")
//        val book2 = createTestBook(id = 2L, key = "book2")
//
//        coEvery { bookRepository.find("book1", 1L) } returns null andThen book1
//        coEvery { bookRepository.find("book2", 1L) } returns null andThen book2
//        coEvery { bookRepository.upsert(match { it.key == "book1" }) } returns 1L
//        coEvery { bookRepository.upsert(match { it.key == "book2" }) } returns 2L
//        coEvery { chapterRepository.findChaptersByBookId(1L) } throws RuntimeException("DB Error")
//        coEvery { chapterRepository.findChaptersByBookId(2L) } returns emptyList()
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        // Book 2 chapters should still be restored
//        coVerify { chapterRepository.insertChapters(match { it.any { ch -> ch.key == "ch2" } }) }
//    }
//
//    // ==================== restoreFrom (URI) Tests ====================
//
//    @Test
//    fun `restoreFrom should read backup from URI and restore`() = runTest {
//        // Given
//        val uri = mockk<Uri>()
//        val bookProto = createTestBookProto(sourceId = 1L, key = "test-book")
//        val backupData = createBackupData(listOf(bookProto))
//
//        every { fileSaver.read(uri) } returns backupData
//        coEvery { bookRepository.find(any(), any()) } returns null
//        coEvery { bookRepository.upsert(any()) } returns 1L
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        var successCalled = false
//        var errorMessage: ireader.i18n.UiText? = null
//
//        // When
//        val result = restoreBackup.restoreFrom(
//            uri = uri,
//            onError = { errorMessage = it },
//            onSuccess = { successCalled = true }
//        )
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        assertTrue(successCalled)
//        assertNull(errorMessage)
//    }
//
//    @Test
//    fun `restoreFrom should call onError when file read fails`() = runTest {
//        // Given
//        val uri = mockk<Uri>()
//        every { fileSaver.read(uri) } throws RuntimeException("File not found")
//
//        var successCalled = false
//        var errorMessage: ireader.i18n.UiText? = null
//
//        // When
//        val result = restoreBackup.restoreFrom(
//            uri = uri,
//            onError = { errorMessage = it },
//            onSuccess = { successCalled = true }
//        )
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Error)
//        assertFalse(successCalled)
//        assertNotNull(errorMessage)
//    }
//
//    // ==================== Multiple Books Tests ====================
//
//    @Test
//    fun `restoreFromBytes should restore multiple books correctly`() = runTest {
//        // Given
//        val bookProtos = listOf(
//            createTestBookProto(sourceId = 1L, key = "book1", title = "Book 1"),
//            createTestBookProto(sourceId = 2L, key = "book2", title = "Book 2"),
//            createTestBookProto(sourceId = 1L, key = "book3", title = "Book 3")
//        )
//        val backupData = createBackupData(bookProtos)
//
//        coEvery { bookRepository.find(any(), any()) } returns null
//        coEvery { bookRepository.upsert(any()) } returnsMany listOf(1L, 2L, 3L)
//        coEvery { categoryRepository.findAll() } returns emptyList()
//
//        // When
//        val result = restoreBackup.restoreFromBytes(backupData)
//
//        // Then
//        assertTrue(result is RestoreBackup.Result.Success)
//        coVerify(exactly = 3) { bookRepository.upsert(any()) }
//    }
//
//    // ==================== Helper Functions ====================
//
//    @OptIn(ExperimentalSerializationApi::class)
//    private fun createBackupData(
//        books: List<BookProto>,
//        categories: List<CategoryProto> = emptyList()
//    ): ByteArray {
//        val backup = BackupProto(library = books, categories = categories)
//        return ProtoBuf.encodeToByteArray(backup)
//    }
//
//    private fun createTestBookProto(
//        sourceId: Long,
//        key: String,
//        title: String = "Test Book",
//        author: String = "Test Author",
//        initialized: Boolean = true,
//        lastUpdate: Long = Clock.System.now().toEpochMilliseconds(),
//        chapters: List<ChapterProto> = emptyList(),
//        categories: List<Long> = emptyList()
//    ): BookProto {
//        return BookProto(
//            sourceId = sourceId,
//            key = key,
//            title = title,
//            author = author,
//            initialized = initialized,
//            lastUpdate = lastUpdate,
//            chapters = chapters,
//            categories = categories
//        )
//    }
//
//    private fun createTestChapterProto(
//        key: String,
//        name: String,
//        read: Boolean = false,
//        bookmark: Boolean = false,
//        sourceOrder: Long = 0
//    ): ChapterProto {
//        return ChapterProto(
//            key = key,
//            name = name,
//            read = read,
//            bookmark = bookmark,
//            sourceOrder = sourceOrder
//        )
//    }
//
//    private fun createTestCategoryProto(
//        name: String,
//        order: Long,
//        flags: Long = 0
//    ): CategoryProto {
//        return CategoryProto(
//            name = name,
//            order = order,
//            flags = flags
//        )
//    }
//
//    private fun createTestBook(
//        id: Long,
//        key: String,
//        sourceId: Long = 1L,
//        title: String = "Test Book",
//        initialized: Boolean = true,
//        favorite: Boolean = false,
//        lastUpdate: Long = 0L
//    ): Book {
//        return Book(
//            id = id,
//            sourceId = sourceId,
//            key = key,
//            title = title,
//            initialized = initialized,
//            favorite = favorite,
//            lastUpdate = lastUpdate
//        )
//    }
//
//    private fun createTestChapter(
//        id: Long,
//        bookId: Long,
//        key: String,
//        name: String = "Test Chapter",
//        read: Boolean = false,
//        bookmark: Boolean = false
//    ): Chapter {
//        return Chapter(
//            id = id,
//            bookId = bookId,
//            key = key,
//            name = name,
//            read = read,
//            bookmark = bookmark
//        )
//    }
//}
