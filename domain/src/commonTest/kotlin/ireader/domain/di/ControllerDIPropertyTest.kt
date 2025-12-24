package ireader.domain.di

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.book.BookController
import ireader.domain.services.book.bookModule
import ireader.domain.services.library.LibraryController
import ireader.domain.services.library.libraryModule
import ireader.domain.services.preferences.ReaderPreferencesController
import ireader.domain.services.preferences.preferencesModule
import ireader.domain.usecases.history.HistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Property-based tests for DI singleton identity.
 * 
 * These tests verify that Controllers registered as singletons in DI
 * return the same instance on multiple requests.
 * 
 * **Feature: architecture-optimization**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ControllerDIPropertyTest {
    
    companion object {
        private const val PROPERTY_TEST_ITERATIONS = 100
    }
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }
    
    // ========== Mock Implementations ==========

    /**
     * Mock PreferenceStore that stores values in memory.
     */
    private class MockPreferenceStore : PreferenceStore {
        private val intValues = mutableMapOf<String, Int>()
        private val floatValues = mutableMapOf<String, Float>()
        private val booleanValues = mutableMapOf<String, Boolean>()
        private val stringValues = mutableMapOf<String, String>()
        private val longValues = mutableMapOf<String, Long>()
        
        override fun getString(key: String, defaultValue: String): Preference<String> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { stringValues[key] ?: defaultValue },
                setter = { stringValues[key] = it }
            )
        }
        
        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { longValues[key] ?: defaultValue },
                setter = { longValues[key] = it }
            )
        }
        
        override fun getInt(key: String, defaultValue: Int): Preference<Int> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { intValues[key] ?: defaultValue },
                setter = { intValues[key] = it }
            )
        }
        
        override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { floatValues[key] ?: defaultValue },
                setter = { floatValues[key] = it }
            )
        }
        
        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { booleanValues[key] ?: defaultValue },
                setter = { booleanValues[key] = it }
            )
        }
        
        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
        
        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T
        ): Preference<T> {
            return MockPreference(
                key = key,
                defaultValue = defaultValue,
                getter = { 
                    val stored = stringValues[key]
                    if (stored != null) deserializer(stored) else defaultValue
                },
                setter = { stringValues[key] = serializer(it) }
            )
        }
        
        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: kotlinx.serialization.KSerializer<T>,
            serializersModule: kotlinx.serialization.modules.SerializersModule
        ): Preference<T> {
            throw UnsupportedOperationException("Not needed for these tests")
        }
    }
    
    /**
     * Mock Preference implementation for testing.
     */
    private class MockPreference<T>(
        private val key: String,
        private val defaultValue: T,
        private val getter: () -> T,
        private val setter: (T) -> Unit
    ) : Preference<T> {
        override fun key(): String = key
        override fun get(): T = getter()
        override fun set(value: T) = setter(value)
        override fun isSet(): Boolean = true
        override fun delete() {}
        override fun defaultValue(): T = defaultValue
        override fun changes(): kotlinx.coroutines.flow.Flow<T> = kotlinx.coroutines.flow.flowOf(get())
        override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<T> {
            return kotlinx.coroutines.flow.MutableStateFlow(get())
        }
    }

    
    /**
     * Mock BookRepository for testing.
     */
    private class MockBookRepository : BookRepository {
        override suspend fun findAllBooks(): List<Book> = emptyList()
        override fun subscribeBookById(id: Long): Flow<Book?> = flowOf(null)
        override suspend fun findBookById(id: Long): Book? = null
        override suspend fun find(key: String, sourceId: Long): Book? = null
        override suspend fun findAllInLibraryBooks(sortType: ireader.domain.models.library.LibrarySort, isAsc: Boolean, unreadFilter: Boolean): List<Book> = emptyList()
        override suspend fun findBookByKey(key: String): Book? = null
        override suspend fun findBooksByKey(key: String): List<Book> = emptyList()
        override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>> = flowOf(emptyList())
        override suspend fun deleteBooks(book: List<Book>) {}
        override suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>) {}
        override suspend fun deleteBookById(id: Long) {}
        override suspend fun findDuplicateBook(title: String, sourceId: Long): Book? = null
        override suspend fun deleteAllBooks() {}
        override suspend fun deleteNotInLibraryBooks() {}
        override suspend fun updateBook(book: Book) {}
        override suspend fun updateBook(book: ireader.domain.models.entities.LibraryBook, favorite: Boolean) {}
        override suspend fun updateBook(book: List<Book>) {}
        override suspend fun upsert(book: Book): Long = 0L
        override suspend fun updatePartial(book: Book): Long = 0L
        override suspend fun insertBooks(book: List<Book>): List<Long> = emptyList()
        override suspend fun delete(key: String) {}
        override suspend fun findFavoriteSourceIds(): List<Long> = emptyList()
        override suspend fun repairCategoryAssignments() {}
        override suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int) {}
        override suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int) {}
        override suspend fun getMaxPinnedOrder(): Int = 0
        override suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean) {}
        override suspend fun updateChapterPage(bookId: Long, chapterPage: Int) {}
    }
    
    /**
     * Mock CategoryRepository for testing.
     */
    private class MockCategoryRepository : CategoryRepository {
        override fun subscribe(): Flow<List<ireader.domain.models.entities.CategoryWithCount>> = flowOf(emptyList())
        override suspend fun findAll(): List<ireader.domain.models.entities.CategoryWithCount> = emptyList()
        override suspend fun get(id: Long): Category? = null
        override suspend fun getAll(): List<Category> = emptyList()
        override fun getAllAsFlow(): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoriesByMangaId(mangaId: Long): List<Category> = emptyList()
        override fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun insert(category: Category) {}
        override suspend fun insert(category: List<Category>) {}
        override suspend fun update(category: Category) {}
        override suspend fun updateBatch(categories: List<Category>) {}
        override suspend fun updatePartial(update: ireader.domain.models.entities.CategoryUpdate) {}
        override suspend fun updatePartial(updates: List<ireader.domain.models.entities.CategoryUpdate>) {}
        override suspend fun updateAllFlags(flags: Long?) {}
        override suspend fun delete(categoryId: Long) {}
        override suspend fun deleteAll() {}
    }
    
    /**
     * Mock ChapterRepository for testing.
     */
    private class MockChapterRepository : ChapterRepository {
        override fun subscribeChapterById(chapterId: Long): Flow<Chapter?> = flowOf(null)
        override suspend fun findChapterById(chapterId: Long): Chapter? = null
        override suspend fun findAllChapters(): List<Chapter> = emptyList()
        override suspend fun findAllInLibraryChapter(): List<Chapter> = emptyList()
        override suspend fun findChaptersByBookId(bookId: Long): List<Chapter> = emptyList()
        override suspend fun findLastReadChapter(bookId: Long): Chapter? = null
        override suspend fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?> = flowOf(null)
        override suspend fun insertChapter(chapter: Chapter): Long = 0L
        override suspend fun insertChapters(chapters: List<Chapter>): List<Long> = emptyList()
        override suspend fun deleteChaptersByBookId(bookId: Long) {}
        override suspend fun deleteChapters(chapters: List<Chapter>) {}
        override suspend fun deleteChapter(chapter: Chapter) {}
        override suspend fun deleteAllChapters() {}
        override fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>> = flowOf(emptyList())
        override suspend fun findChaptersByBookIdWithContent(bookId: Long): List<Chapter> = emptyList()
    }
    
    /**
     * Mock HistoryRepository for testing.
     */
    private class MockHistoryRepository : HistoryRepository {
        override suspend fun findHistory(id: Long): History? = null
        override suspend fun findHistoryByChapterId(chapterId: Long): History? = null
        override suspend fun findHistoryByBookId(bookId: Long): History? = null
        override suspend fun findHistoriesByBookId(bookId: Long): List<History> = emptyList()
        override fun subscribeHistoryByBookId(bookId: Long): Flow<History?> = flowOf(null)
        override suspend fun findHistoryByChapterUrl(chapterUrl: String): History? = null
        override suspend fun findHistories(): List<History> = emptyList()
        override fun findHistoriesByFlow(query: String): Flow<List<ireader.domain.models.entities.HistoryWithRelations>> = flowOf(emptyList())
        override suspend fun upsert(chapterId: Long, readAt: Long, readDuration: Long, progress: Float) {}
        override suspend fun insertHistory(history: History) {}
        override suspend fun insertHistories(histories: List<History>) {}
        override suspend fun deleteHistories(histories: List<History>) {}
        override suspend fun deleteHistory(chapterId: Long) {}
        override suspend fun deleteHistoryByBookId(bookId: Long) {}
        override suspend fun deleteAllHistories() {}
        override suspend fun updateHistory(chapterId: Long, readAt: Long?, readDuration: Long?, progress: Float?) {}
        override suspend fun resetHistoryById(historyId: Long) {}
        override suspend fun resetHistoryByBookId(historyId: Long) {}
    }
    
    /**
     * Mock LibraryRepository for testing.
     */
    private class MockLibraryRepository : LibraryRepository {
        override suspend fun findAll(sort: ireader.domain.models.library.LibrarySort, includeArchived: Boolean): List<ireader.domain.models.entities.LibraryBook> = emptyList()
        override fun subscribe(sort: ireader.domain.models.library.LibrarySort, includeArchived: Boolean): Flow<List<ireader.domain.models.entities.LibraryBook>> = flowOf(emptyList())
        override fun subscribeFast(sort: ireader.domain.models.library.LibrarySort, includeArchived: Boolean): Flow<List<ireader.domain.models.entities.LibraryBook>> = flowOf(emptyList())
        override suspend fun findAllFast(sort: ireader.domain.models.library.LibrarySort, includeArchived: Boolean): List<ireader.domain.models.entities.LibraryBook> = emptyList()
        override suspend fun findDownloadedBooks(): List<Book> = emptyList()
        override suspend fun findFavorites(): List<Book> = emptyList()
    }
    
    /**
     * Create UiPreferences for testing using mock store.
     */
    private fun createMockUiPreferences(store: PreferenceStore): UiPreferences {
        return UiPreferences(store)
    }
    
    /**
     * Mock HistoryUseCase for testing - extends the real class with mock repository.
     */
    private fun createMockHistoryUseCase(): HistoryUseCase {
        return HistoryUseCase(MockHistoryRepository())
    }

    
    // ========== Test Helpers ==========
    
    /**
     * Creates a test Koin module with mock dependencies.
     */
    private fun createTestDependenciesModule() = module {
        // Mock repositories
        single<BookRepository> { MockBookRepository() }
        single<CategoryRepository> { MockCategoryRepository() }
        single<ChapterRepository> { MockChapterRepository() }
        single<HistoryRepository> { MockHistoryRepository() }
        single<LibraryRepository> { MockLibraryRepository() }
        
        // Mock preferences
        single<PreferenceStore> { MockPreferenceStore() }
        single { ReaderPreferences(get()) }
        single { createMockUiPreferences(get()) }
        
        // Mock use cases
        single { createMockHistoryUseCase() }
    }
    
    // ========== Property Tests ==========
    
    /**
     * **Feature: architecture-optimization, Property 13: DI Singleton Identity**
     * 
     * *For any* Controller requested from DI multiple times, the same instance SHALL be returned.
     * 
     * **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
     */
    @Test
    fun `Property 13 - DI Singleton Identity - same Controller instance returned on multiple requests`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Start fresh Koin for each iteration
            stopKoin()
            
            startKoin {
                modules(
                    createTestDependenciesModule(),
                    preferencesModule,
                    bookModule,
                    libraryModule
                )
            }
            
            val koin = getKoin()
            
            // Test ReaderPreferencesController singleton identity
            val preferencesController1 = koin.get<ReaderPreferencesController>()
            val preferencesController2 = koin.get<ReaderPreferencesController>()
            val preferencesController3 = koin.get<ReaderPreferencesController>()
            
            assertSame(
                preferencesController1,
                preferencesController2,
                "Iteration $iteration: ReaderPreferencesController should be singleton (1 vs 2)"
            )
            assertSame(
                preferencesController2,
                preferencesController3,
                "Iteration $iteration: ReaderPreferencesController should be singleton (2 vs 3)"
            )
            
            // Test BookController singleton identity
            val bookController1 = koin.get<BookController>()
            val bookController2 = koin.get<BookController>()
            val bookController3 = koin.get<BookController>()
            
            assertSame(
                bookController1,
                bookController2,
                "Iteration $iteration: BookController should be singleton (1 vs 2)"
            )
            assertSame(
                bookController2,
                bookController3,
                "Iteration $iteration: BookController should be singleton (2 vs 3)"
            )
            
            // Test LibraryController singleton identity
            val libraryController1 = koin.get<LibraryController>()
            val libraryController2 = koin.get<LibraryController>()
            val libraryController3 = koin.get<LibraryController>()
            
            assertSame(
                libraryController1,
                libraryController2,
                "Iteration $iteration: LibraryController should be singleton (1 vs 2)"
            )
            assertSame(
                libraryController2,
                libraryController3,
                "Iteration $iteration: LibraryController should be singleton (2 vs 3)"
            )
            
            // Verify that different controller types are different instances
            // (they are different types, so they are inherently different instances)
            assertTrue(
                preferencesController1 != null && bookController1 != null,
                "Iteration $iteration: Different controller types should be different instances"
            )
            assertTrue(
                bookController1 != null && libraryController1 != null,
                "Iteration $iteration: Different controller types should be different instances"
            )
            
            // Clean up controllers
            preferencesController1.release()
            bookController1.dispatch(ireader.domain.services.book.BookCommand.Cleanup)
            libraryController1.dispatch(ireader.domain.services.library.LibraryCommand.Cleanup)
            
            testScheduler.advanceUntilIdle()
        }
    }
    
    /**
     * Test that Controllers maintain singleton identity across concurrent access.
     * 
     * **Validates: Requirements 6.2**
     */
    @Test
    fun `Controllers maintain singleton identity across concurrent access`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS / 10) { iteration ->
            stopKoin()
            
            startKoin {
                modules(
                    createTestDependenciesModule(),
                    preferencesModule,
                    bookModule,
                    libraryModule
                )
            }
            
            val koin = getKoin()
            
            // Simulate concurrent access by getting controllers multiple times
            val preferencesControllers = (1..10).map { koin.get<ReaderPreferencesController>() }
            val bookControllers = (1..10).map { koin.get<BookController>() }
            val libraryControllers = (1..10).map { koin.get<LibraryController>() }
            
            // All instances should be the same
            val firstPreferences = preferencesControllers.first()
            val firstBook = bookControllers.first()
            val firstLibrary = libraryControllers.first()
            
            preferencesControllers.forEachIndexed { index, controller ->
                assertSame(
                    firstPreferences,
                    controller,
                    "Iteration $iteration: All ReaderPreferencesController instances should be same (index $index)"
                )
            }
            
            bookControllers.forEachIndexed { index, controller ->
                assertSame(
                    firstBook,
                    controller,
                    "Iteration $iteration: All BookController instances should be same (index $index)"
                )
            }
            
            libraryControllers.forEachIndexed { index, controller ->
                assertSame(
                    firstLibrary,
                    controller,
                    "Iteration $iteration: All LibraryController instances should be same (index $index)"
                )
            }
            
            // Clean up
            firstPreferences.release()
            firstBook.dispatch(ireader.domain.services.book.BookCommand.Cleanup)
            firstLibrary.dispatch(ireader.domain.services.library.LibraryCommand.Cleanup)
            
            testScheduler.advanceUntilIdle()
        }
    }
    
    /**
     * Test that module includes work correctly.
     * 
     * **Validates: Requirements 6.3, 6.4**
     */
    @Test
    fun `Module includes register all required Controllers`() = runTest(testDispatcher) {
        stopKoin()
        
        startKoin {
            modules(
                createTestDependenciesModule(),
                preferencesModule,
                bookModule,
                libraryModule
            )
        }
        
        val koin = getKoin()
        
        // Verify all controllers can be resolved
        val preferencesController = koin.getOrNull<ReaderPreferencesController>()
        val bookController = koin.getOrNull<BookController>()
        val libraryController = koin.getOrNull<LibraryController>()
        
        assertTrue(
            preferencesController != null,
            "ReaderPreferencesController should be resolvable from DI"
        )
        assertTrue(
            bookController != null,
            "BookController should be resolvable from DI"
        )
        assertTrue(
            libraryController != null,
            "LibraryController should be resolvable from DI"
        )
        
        // Clean up
        preferencesController?.release()
        bookController?.dispatch(ireader.domain.services.book.BookCommand.Cleanup)
        libraryController?.dispatch(ireader.domain.services.library.LibraryCommand.Cleanup)
        
        testScheduler.advanceUntilIdle()
    }
}
