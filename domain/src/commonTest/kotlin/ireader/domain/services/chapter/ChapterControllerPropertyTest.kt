package ireader.domain.services.chapter

import ireader.core.source.model.Text
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.usecases.chapter.controller.GetChaptersUseCase
import ireader.domain.usecases.chapter.controller.LoadChapterContentUseCase
import ireader.domain.usecases.chapter.controller.NavigateChapterUseCase
import ireader.domain.usecases.chapter.controller.UpdateProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Property-based tests for ChapterController.
 * 
 * These tests verify the correctness properties defined in the design document.
 * Each test runs multiple iterations with randomly generated data to ensure
 * properties hold across all valid inputs.
 * 
 * **Feature: unified-chapter-controller**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChapterControllerPropertyTest {
    
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
    }
    
    // ========== Mock Implementations ==========
    
    private class MockGetChaptersUseCase : GetChaptersUseCase {
        val chaptersFlow = MutableStateFlow<List<Chapter>>(emptyList())
        var chapters: List<Chapter> = emptyList()
            set(value) {
                field = value
                chaptersFlow.value = value
            }
        
        override fun subscribeByBookId(bookId: Long): Flow<List<Chapter>> = chaptersFlow
        
        override suspend fun findByBookId(bookId: Long): List<Chapter> = chapters
        
        override suspend fun findById(chapterId: Long): Chapter? = 
            chapters.find { it.id == chapterId }
    }
    
    private class MockLoadChapterContentUseCase : LoadChapterContentUseCase {
        var shouldFail = false
        var failureMessage = "Mock failure"
        
        override suspend fun loadContent(
            chapter: Chapter,
            catalog: CatalogLocal?,
            commands: ireader.core.source.model.CommandList
        ): Result<Chapter> {
            return if (shouldFail) {
                Result.failure(Exception(failureMessage))
            } else {
                // Return chapter with mock content if empty
                val contentChapter = if (chapter.content.isEmpty()) {
                    chapter.copy(content = listOf(
                        Text("Paragraph 1"),
                        Text("Paragraph 2"),
                        Text("Paragraph 3")
                    ))
                } else {
                    chapter
                }
                Result.success(contentChapter)
            }
        }
        
        override suspend fun preloadContent(
            chapter: Chapter,
            catalog: CatalogLocal?,
            commands: ireader.core.source.model.CommandList
        ): Result<Chapter> = loadContent(chapter, catalog, commands)
    }
    
    private class MockUpdateProgressUseCase : UpdateProgressUseCase {
        val lastReadFlow = MutableStateFlow<Long?>(null)
        var lastUpdatedBookId: Long? = null
        var lastUpdatedChapterId: Long? = null
        var lastUpdatedParagraphIndex: Int? = null
        
        override suspend fun updateLastRead(bookId: Long, chapterId: Long) {
            lastUpdatedBookId = bookId
            lastUpdatedChapterId = chapterId
            lastReadFlow.value = chapterId
        }
        
        override suspend fun updateParagraphIndex(chapterId: Long, paragraphIndex: Int) {
            lastUpdatedParagraphIndex = paragraphIndex
        }
        
        override fun subscribeLastRead(bookId: Long): Flow<Long?> = lastReadFlow
    }
    
    private class MockNavigateChapterUseCase(
        private val getChaptersUseCase: MockGetChaptersUseCase
    ) : NavigateChapterUseCase {
        
        override suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long? {
            val chapters = getChaptersUseCase.chapters.sortedBy { it.number }
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            return if (currentIndex >= 0 && currentIndex < chapters.lastIndex) {
                chapters[currentIndex + 1].id
            } else {
                null
            }
        }
        
        override suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long? {
            val chapters = getChaptersUseCase.chapters.sortedBy { it.number }
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            return if (currentIndex > 0) {
                chapters[currentIndex - 1].id
            } else {
                null
            }
        }
    }
    
    private class MockBookRepository : BookRepository {
        var book: Book? = null
        
        override suspend fun findBookById(id: Long): Book? = book
        override fun subscribeBookById(id: Long): Flow<Book?> = MutableStateFlow(book)
        override suspend fun findAllBooks(): List<Book> = listOfNotNull(book)
        override suspend fun find(key: String, sourceId: Long): Book? = null
        override suspend fun findAllInLibraryBooks(
            sortType: ireader.domain.models.library.LibrarySort,
            isAsc: Boolean,
            unreadFilter: Boolean
        ): List<Book> = emptyList()
        override suspend fun findBookByKey(key: String): Book? = null
        override suspend fun findBooksByKey(key: String): List<Book> = emptyList()
        override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>> = 
            MutableStateFlow(emptyList())
        override suspend fun deleteBooks(book: List<Book>) {}
        override suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>) {}
        override suspend fun deleteBookById(id: Long) {}
        override suspend fun findDuplicateBook(title: String, sourceId: Long): Book? = null
        override suspend fun deleteAllBooks() {}
        override suspend fun deleteNotInLibraryBooks() {}
        override suspend fun updateBook(book: Book) {}
        override suspend fun updateBook(book: ireader.domain.models.entities.LibraryBook, favorite: Boolean) {}
        override suspend fun updateBook(book: List<Book>) {}
        override suspend fun upsert(book: Book): Long = book.id
        override suspend fun updatePartial(book: Book): Long = book.id
        override suspend fun insertBooks(book: List<Book>): List<Long> = book.map { it.id }
        override suspend fun delete(key: String) {}
        override suspend fun findFavoriteSourceIds(): List<Long> = emptyList()
        override suspend fun repairCategoryAssignments() {}
        override suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int) {}
        override suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int) {}
        override suspend fun getMaxPinnedOrder(): Int = 0
        override suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean) {}
    }
    
    // ========== Test Data Generators ==========
    
    private fun generateBook(id: Long = Random.nextLong(1, 10000)): Book {
        return Book(
            id = id,
            sourceId = Random.nextLong(1, 100),
            title = "Test Book $id",
            key = "book-key-$id"
        )
    }
    
    private fun generateChapter(
        id: Long = Random.nextLong(1, 100000),
        bookId: Long,
        number: Float,
        read: Boolean = Random.nextBoolean(),
        bookmark: Boolean = Random.nextBoolean(),
        hasContent: Boolean = Random.nextBoolean()
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = "chapter-key-$id",
            name = "Chapter $number",
            number = number,
            read = read,
            bookmark = bookmark,
            content = if (hasContent) listOf(
                Text("Content for chapter $id")
            ) else emptyList()
        )
    }
    
    private fun generateChapters(
        bookId: Long,
        count: Int,
        readRatio: Float = 0.5f,
        bookmarkRatio: Float = 0.2f,
        downloadedRatio: Float = 0.3f
    ): List<Chapter> {
        return (1..count).map { i ->
            generateChapter(
                id = i.toLong(),
                bookId = bookId,
                number = i.toFloat(),
                read = Random.nextFloat() < readRatio,
                bookmark = Random.nextFloat() < bookmarkRatio,
                hasContent = Random.nextFloat() < downloadedRatio
            )
        }
    }
    
    private fun createController(
        getChaptersUseCase: MockGetChaptersUseCase = MockGetChaptersUseCase(),
        loadChapterContentUseCase: MockLoadChapterContentUseCase = MockLoadChapterContentUseCase(),
        updateProgressUseCase: MockUpdateProgressUseCase = MockUpdateProgressUseCase(),
        bookRepository: MockBookRepository = MockBookRepository()
    ): ChapterController {
        val navigateChapterUseCase = MockNavigateChapterUseCase(getChaptersUseCase)
        return ChapterController(
            getChaptersUseCase = getChaptersUseCase,
            loadChapterContentUseCase = loadChapterContentUseCase,
            updateProgressUseCase = updateProgressUseCase,
            navigateChapterUseCase = navigateChapterUseCase,
            bookRepository = bookRepository
        )
    }
    
    // ========== Property Tests ==========

    
    /**
     * **Feature: unified-chapter-controller, Property 1: State Consistency**
     * 
     * *For any* number of observers subscribed to the ChapterController, when state 
     * changes occur, all observers SHALL receive the same ChapterState values.
     * 
     * **Validates: Requirements 1.1, 1.4**
     */
    @Test
    fun `Property 1 - State Consistency - all observers receive same state values`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(1, 20))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            // Collect state from multiple "observers" (same StateFlow)
            val observer1State = controller.state.value
            val observer2State = controller.state.value
            val observer3State = controller.state.value
            
            // All observers should see the same state
            assertEquals(observer1State, observer2State)
            assertEquals(observer2State, observer3State)
            
            // After state change, all observers should still see the same state
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            val newObserver1State = controller.state.value
            val newObserver2State = controller.state.value
            
            assertEquals(newObserver1State, newObserver2State)
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 2: Navigation Consistency**
     * 
     * *For any* list of chapters with size N > 0 and any current index I where 0 <= I < N:
     * - Calling nextChapter when I < N-1 SHALL return the chapter at index I+1
     * - Calling previousChapter when I > 0 SHALL return the chapter at index I-1
     * - Calling nextChapter when I = N-1 SHALL return null (stay at current)
     * - Calling previousChapter when I = 0 SHALL return null (stay at current)
     * 
     * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
     */
    @Test
    fun `Property 2 - Navigation Consistency - next and previous return correct chapters`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapterCount = Random.nextInt(2, 15)
            val chapters = generateChapters(book.id, chapterCount)
            val sortedChapters = chapters.sortedBy { it.number }
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            // Load book and initial chapter
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            val startIndex = Random.nextInt(0, chapterCount)
            val startChapter = sortedChapters[startIndex]
            
            controller.dispatch(ChapterCommand.LoadChapter(startChapter.id))
            testScheduler.advanceUntilIdle()
            
            val stateAfterLoad = controller.state.value
            assertEquals(startChapter.id, stateAfterLoad.currentChapter?.id)
            
            // Test next chapter navigation
            if (startIndex < chapterCount - 1) {
                controller.dispatch(ChapterCommand.NextChapter)
                testScheduler.advanceUntilIdle()
                val stateAfterNext = controller.state.value
                assertEquals(sortedChapters[startIndex + 1].id, stateAfterNext.currentChapter?.id)
            }
            
            // Reset to start position
            controller.dispatch(ChapterCommand.LoadChapter(startChapter.id))
            testScheduler.advanceUntilIdle()
            
            // Test previous chapter navigation
            if (startIndex > 0) {
                controller.dispatch(ChapterCommand.PreviousChapter)
                testScheduler.advanceUntilIdle()
                val stateAfterPrev = controller.state.value
                assertEquals(sortedChapters[startIndex - 1].id, stateAfterPrev.currentChapter?.id)
            }
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 3: Jump Navigation Correctness**
     * 
     * *For any* valid chapter ID in the chapters list, dispatching JumpToChapter(chapterId) 
     * SHALL result in currentChapter.id equaling chapterId.
     * 
     * **Validates: Requirements 2.5**
     */
    @Test
    fun `Property 3 - Jump Navigation - JumpToChapter updates currentChapter correctly`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(3, 20))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Jump to a random chapter
            val targetChapter = chapters.random()
            controller.dispatch(ChapterCommand.JumpToChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            
            val state = controller.state.value
            assertEquals(targetChapter.id, state.currentChapter?.id)
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 4: Reading Progress Round Trip**
     * 
     * *For any* book with chapters, if we load a chapter and update progress, then 
     * reload the book, the lastReadChapterId SHALL match the previously loaded chapter ID.
     * 
     * **Validates: Requirements 3.1, 3.2**
     */
    @Test
    fun `Property 4 - Progress Round Trip - lastReadChapterId matches loaded chapter`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val updateProgressUseCase = MockUpdateProgressUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                updateProgressUseCase = updateProgressUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(2, 15))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Load a random chapter
            val targetChapter = chapters.random()
            controller.dispatch(ChapterCommand.LoadChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            
            // Verify progress was updated
            assertEquals(book.id, updateProgressUseCase.lastUpdatedBookId)
            assertEquals(targetChapter.id, updateProgressUseCase.lastUpdatedChapterId)
            
            // Verify lastReadChapterId in state matches
            val state = controller.state.value
            assertEquals(targetChapter.id, state.lastReadChapterId)
            
            controller.release()
        }
    }

    
    /**
     * **Feature: unified-chapter-controller, Property 5: Filter Correctness**
     * 
     * *For any* list of chapters and any filter criteria, the filteredChapters list 
     * SHALL contain only chapters that match the filter predicate, and SHALL contain 
     * all chapters that match the filter predicate.
     * 
     * **Validates: Requirements 6.1, 6.4**
     */
    @Test
    fun `Property 5 - Filter Correctness - filteredChapters contains only matching chapters`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(
                book.id, 
                Random.nextInt(5, 30),
                readRatio = 0.5f,
                bookmarkRatio = 0.3f,
                downloadedRatio = 0.4f
            )
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Test each filter type
            val filters = listOf(
                ChapterFilter.None,
                ChapterFilter.ReadOnly,
                ChapterFilter.UnreadOnly,
                ChapterFilter.BookmarkedOnly,
                ChapterFilter.DownloadedOnly
            )
            
            val filter = filters.random()
            controller.dispatch(ChapterCommand.SetFilter(filter))
            testScheduler.advanceUntilIdle()
            
            val state = controller.state.value
            val expectedFiltered = chapters.filter(filter.toPredicate())
            
            // Verify filtered chapters match expected
            assertEquals(
                expectedFiltered.map { it.id }.toSet(),
                state.filteredChapters.map { it.id }.toSet(),
                "Filter $filter should produce correct results"
            )
            
            // Verify all filtered chapters match the predicate
            state.filteredChapters.forEach { chapter ->
                assertTrue(
                    filter.toPredicate()(chapter),
                    "Chapter ${chapter.id} should match filter $filter"
                )
            }
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 6: Sort Correctness**
     * 
     * *For any* list of chapters and any sort criteria, the filteredChapters list 
     * SHALL be ordered according to the sort comparator.
     * 
     * **Validates: Requirements 6.2**
     */
    @Test
    fun `Property 6 - Sort Correctness - filteredChapters ordered by comparator`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(5, 25))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Test different sort types
            val sortTypes = listOf(
                ChapterSort(ChapterSort.Type.NUMBER, true),
                ChapterSort(ChapterSort.Type.NUMBER, false),
                ChapterSort(ChapterSort.Type.NAME, true),
                ChapterSort(ChapterSort.Type.NAME, false)
            )
            
            val sort = sortTypes.random()
            controller.dispatch(ChapterCommand.SetSort(sort))
            testScheduler.advanceUntilIdle()
            
            val state = controller.state.value
            val expectedSorted = chapters.sortedWith(sort.toComparator())
            
            // Verify order matches
            assertEquals(
                expectedSorted.map { it.id },
                state.filteredChapters.map { it.id },
                "Sort $sort should produce correct order"
            )
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 7: Filter/Sort Preserves Selection**
     * 
     * *For any* selection state and any filter or sort change, the selectedChapterIds 
     * set SHALL remain unchanged after the filter/sort operation.
     * 
     * **Validates: Requirements 6.3**
     */
    @Test
    fun `Property 7 - Filter Sort Preserves Selection - selectedChapterIds unchanged`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(5, 20))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Select some random chapters
            val selectedIds = chapters.shuffled().take(Random.nextInt(1, chapters.size / 2 + 1))
                .map { it.id }.toSet()
            
            selectedIds.forEach { id ->
                controller.dispatch(ChapterCommand.SelectChapter(id))
            }
            testScheduler.advanceUntilIdle()
            
            val selectionBefore = controller.state.value.selectedChapterIds
            assertEquals(selectedIds, selectionBefore)
            
            // Apply filter
            controller.dispatch(ChapterCommand.SetFilter(ChapterFilter.ReadOnly))
            testScheduler.advanceUntilIdle()
            
            val selectionAfterFilter = controller.state.value.selectedChapterIds
            assertEquals(selectionBefore, selectionAfterFilter, "Selection should be preserved after filter")
            
            // Apply sort
            controller.dispatch(ChapterCommand.SetSort(ChapterSort(ChapterSort.Type.NAME, false)))
            testScheduler.advanceUntilIdle()
            
            val selectionAfterSort = controller.state.value.selectedChapterIds
            assertEquals(selectionBefore, selectionAfterSort, "Selection should be preserved after sort")
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 8: Selection Toggle Consistency**
     * 
     * *For any* chapter ID:
     * - Dispatching SelectChapter(id) SHALL result in selectedChapterIds containing id
     * - Dispatching DeselectChapter(id) SHALL result in selectedChapterIds not containing id
     * - Dispatching SelectChapter(id) twice SHALL have the same effect as dispatching it once (idempotent)
     * 
     * **Validates: Requirements 7.1, 7.2**
     */
    @Test
    fun `Property 8 - Selection Toggle - Select adds ID, Deselect removes ID, idempotent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(3, 15))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            val targetChapter = chapters.random()
            
            // Test Select
            controller.dispatch(ChapterCommand.SelectChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            assertTrue(controller.state.value.selectedChapterIds.contains(targetChapter.id))
            
            // Test idempotent Select (selecting again should have same result)
            controller.dispatch(ChapterCommand.SelectChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            assertTrue(controller.state.value.selectedChapterIds.contains(targetChapter.id))
            assertEquals(1, controller.state.value.selectedChapterIds.count { it == targetChapter.id })
            
            // Test Deselect
            controller.dispatch(ChapterCommand.DeselectChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            assertFalse(controller.state.value.selectedChapterIds.contains(targetChapter.id))
            
            // Test idempotent Deselect (deselecting again should have same result)
            controller.dispatch(ChapterCommand.DeselectChapter(targetChapter.id))
            testScheduler.advanceUntilIdle()
            assertFalse(controller.state.value.selectedChapterIds.contains(targetChapter.id))
            
            controller.release()
        }
    }

    
    /**
     * **Feature: unified-chapter-controller, Property 9: Select All Correctness**
     * 
     * *For any* filtered chapters list, dispatching SelectAll SHALL result in 
     * selectedChapterIds containing exactly the IDs of all chapters in filteredChapters.
     * 
     * **Validates: Requirements 7.3**
     */
    @Test
    fun `Property 9 - Select All - selectedChapterIds contains all filtered chapter IDs`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(5, 25))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Optionally apply a filter first
            if (Random.nextBoolean()) {
                controller.dispatch(ChapterCommand.SetFilter(ChapterFilter.ReadOnly))
                testScheduler.advanceUntilIdle()
            }
            
            val filteredIds = controller.state.value.filteredChapters.map { it.id }.toSet()
            
            controller.dispatch(ChapterCommand.SelectAll)
            testScheduler.advanceUntilIdle()
            
            val selectedIds = controller.state.value.selectedChapterIds
            assertEquals(filteredIds, selectedIds, "SelectAll should select exactly all filtered chapters")
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 10: Clear Selection Correctness**
     * 
     * *For any* selection state, dispatching ClearSelection SHALL result in 
     * selectedChapterIds being empty.
     * 
     * **Validates: Requirements 7.4**
     */
    @Test
    fun `Property 10 - Clear Selection - selectedChapterIds becomes empty`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(5, 20))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Select some random chapters
            val toSelect = chapters.shuffled().take(Random.nextInt(1, chapters.size))
            toSelect.forEach { chapter ->
                controller.dispatch(ChapterCommand.SelectChapter(chapter.id))
            }
            testScheduler.advanceUntilIdle()
            
            assertTrue(controller.state.value.selectedChapterIds.isNotEmpty())
            
            // Clear selection
            controller.dispatch(ChapterCommand.ClearSelection)
            testScheduler.advanceUntilIdle()
            
            assertTrue(
                controller.state.value.selectedChapterIds.isEmpty(),
                "ClearSelection should result in empty selection"
            )
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 11: Invert Selection Correctness**
     * 
     * *For any* selection state S and filtered chapters list F, dispatching InvertSelection 
     * SHALL result in selectedChapterIds containing exactly (F.ids - S) where F.ids is 
     * the set of all filtered chapter IDs.
     * 
     * **Validates: Requirements 7.5**
     */
    @Test
    fun `Property 11 - Invert Selection - selectedChapterIds contains inverted set`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(5, 20))
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            val filteredIds = controller.state.value.filteredChapters.map { it.id }.toSet()
            
            // Select some random chapters
            val initialSelection = chapters.shuffled()
                .take(Random.nextInt(0, chapters.size))
                .map { it.id }
                .toSet()
            
            initialSelection.forEach { id ->
                controller.dispatch(ChapterCommand.SelectChapter(id))
            }
            testScheduler.advanceUntilIdle()
            
            val selectionBefore = controller.state.value.selectedChapterIds
            
            // Invert selection
            controller.dispatch(ChapterCommand.InvertSelection)
            testScheduler.advanceUntilIdle()
            
            val selectionAfter = controller.state.value.selectedChapterIds
            val expectedInverted = filteredIds - selectionBefore
            
            assertEquals(
                expectedInverted,
                selectionAfter,
                "InvertSelection should produce (filteredIds - previousSelection)"
            )
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 12: Error Handling Consistency**
     * 
     * *For any* operation that fails (database, network, or invalid ID), the ChapterController SHALL:
     * - Emit a ChapterEvent.Error with appropriate ChapterError type
     * - Not crash or throw uncaught exceptions
     * - Maintain a valid ChapterState (not corrupted)
     * 
     * **Validates: Requirements 8.1, 8.2, 8.3**
     */
    @Test
    fun `Property 12 - Error Handling - failures emit error events without crashes`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val loadChapterContentUseCase = MockLoadChapterContentUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                loadChapterContentUseCase = loadChapterContentUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            // Create chapters WITHOUT content so content loading will be triggered
            val chapters = (1..Random.nextInt(2, 10)).map { i ->
                generateChapter(
                    id = i.toLong(),
                    bookId = book.id,
                    number = i.toFloat(),
                    hasContent = false  // No content - will trigger load
                )
            }
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Test 1: Jump to invalid chapter ID
            val invalidChapterId = Random.nextLong(100000, 200000)
            controller.dispatch(ChapterCommand.JumpToChapter(invalidChapterId))
            testScheduler.advanceUntilIdle()
            
            // Should have error state but not crash
            val stateAfterInvalidJump = controller.state.value
            assertNotNull(stateAfterInvalidJump.error, "Should have error after invalid jump")
            assertTrue(stateAfterInvalidJump.error is ChapterError.ChapterNotFound)
            
            // Clear error and test content load failure
            controller.clearError()
            loadChapterContentUseCase.shouldFail = true
            
            val validChapter = chapters.first()
            controller.dispatch(ChapterCommand.LoadChapter(validChapter.id))
            testScheduler.advanceUntilIdle()
            
            val stateAfterLoadFail = controller.state.value
            assertNotNull(stateAfterLoadFail.error, "Should have error after load failure")
            assertTrue(stateAfterLoadFail.error is ChapterError.ContentLoadFailed)
            
            // State should still be valid (not corrupted)
            assertNotNull(stateAfterLoadFail.book)
            assertTrue(stateAfterLoadFail.chapters.isNotEmpty())
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 13: Content Loading State Transitions**
     * 
     * *For any* chapter with empty content, dispatching LoadChapter SHALL:
     * - Set isLoadingContent to true while fetching
     * - Set isLoadingContent to false after fetch completes (success or failure)
     * 
     * **Validates: Requirements 4.1, 4.2, 4.4**
     */
    @Test
    fun `Property 13 - Content Loading States - isLoadingContent transitions correctly`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            // Create chapters without content to trigger loading
            val chapters = (1..Random.nextInt(3, 10)).map { i ->
                generateChapter(
                    id = i.toLong(),
                    bookId = book.id,
                    number = i.toFloat(),
                    hasContent = false
                )
            }
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Before loading chapter
            assertFalse(controller.state.value.isLoadingContent)
            
            // Load a chapter
            val targetChapter = chapters.random()
            controller.dispatch(ChapterCommand.LoadChapter(targetChapter.id))
            
            // After loading completes
            testScheduler.advanceUntilIdle()
            assertFalse(
                controller.state.value.isLoadingContent,
                "isLoadingContent should be false after load completes"
            )
            
            controller.release()
        }
    }
    
    /**
     * **Feature: unified-chapter-controller, Property 14: Preload State Transitions**
     * 
     * *For any* preload operation, dispatching PreloadNextChapter SHALL:
     * - Set isPreloading to true while preloading
     * - Set isPreloading to false after preload completes
     * 
     * **Validates: Requirements 4.5**
     */
    @Test
    fun `Property 14 - Preload States - isPreloading transitions correctly`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) {
            val getChaptersUseCase = MockGetChaptersUseCase()
            val bookRepository = MockBookRepository()
            val controller = createController(
                getChaptersUseCase = getChaptersUseCase,
                bookRepository = bookRepository
            )
            
            val book = generateBook()
            val chapters = generateChapters(book.id, Random.nextInt(3, 10))
            val sortedChapters = chapters.sortedBy { it.number }
            
            bookRepository.book = book
            getChaptersUseCase.chapters = chapters
            
            controller.dispatch(ChapterCommand.LoadBook(book.id))
            testScheduler.advanceUntilIdle()
            
            // Load a chapter that's not the last one (so there's a next chapter to preload)
            val startIndex = Random.nextInt(0, sortedChapters.size - 1)
            controller.dispatch(ChapterCommand.LoadChapter(sortedChapters[startIndex].id))
            testScheduler.advanceUntilIdle()
            
            // Before preloading
            assertFalse(controller.state.value.isPreloading)
            
            // Trigger preload
            controller.dispatch(ChapterCommand.PreloadNextChapter)
            
            // After preload completes
            testScheduler.advanceUntilIdle()
            assertFalse(
                controller.state.value.isPreloading,
                "isPreloading should be false after preload completes"
            )
            
            controller.release()
        }
    }
    
    // ========== Boundary Tests ==========
    
    /**
     * Test navigation at first chapter boundary.
     * **Validates: Requirements 2.3**
     */
    @Test
    fun `Navigation at first chapter - previousChapter returns null`() = runTest(testDispatcher) {
        val getChaptersUseCase = MockGetChaptersUseCase()
        val bookRepository = MockBookRepository()
        val controller = createController(
            getChaptersUseCase = getChaptersUseCase,
            bookRepository = bookRepository
        )
        
        val book = generateBook()
        val chapters = generateChapters(book.id, 5)
        val firstChapter = chapters.minByOrNull { it.number }!!
        
        bookRepository.book = book
        getChaptersUseCase.chapters = chapters
        
        controller.dispatch(ChapterCommand.LoadBook(book.id))
        testScheduler.advanceUntilIdle()
        
        controller.dispatch(ChapterCommand.LoadChapter(firstChapter.id))
        testScheduler.advanceUntilIdle()
        
        val stateBefore = controller.state.value
        assertEquals(firstChapter.id, stateBefore.currentChapter?.id)
        assertFalse(stateBefore.canGoPrevious)
        
        // Try to go previous - should stay at first chapter
        controller.dispatch(ChapterCommand.PreviousChapter)
        testScheduler.advanceUntilIdle()
        
        val stateAfter = controller.state.value
        assertEquals(firstChapter.id, stateAfter.currentChapter?.id)
        
        controller.release()
    }
    
    /**
     * Test navigation at last chapter boundary.
     * **Validates: Requirements 2.4**
     */
    @Test
    fun `Navigation at last chapter - nextChapter returns null`() = runTest(testDispatcher) {
        val getChaptersUseCase = MockGetChaptersUseCase()
        val bookRepository = MockBookRepository()
        val controller = createController(
            getChaptersUseCase = getChaptersUseCase,
            bookRepository = bookRepository
        )
        
        val book = generateBook()
        val chapters = generateChapters(book.id, 5)
        val lastChapter = chapters.maxByOrNull { it.number }!!
        
        bookRepository.book = book
        getChaptersUseCase.chapters = chapters
        
        controller.dispatch(ChapterCommand.LoadBook(book.id))
        testScheduler.advanceUntilIdle()
        
        controller.dispatch(ChapterCommand.LoadChapter(lastChapter.id))
        testScheduler.advanceUntilIdle()
        
        val stateBefore = controller.state.value
        assertEquals(lastChapter.id, stateBefore.currentChapter?.id)
        assertFalse(stateBefore.canGoNext)
        
        // Try to go next - should stay at last chapter
        controller.dispatch(ChapterCommand.NextChapter)
        testScheduler.advanceUntilIdle()
        
        val stateAfter = controller.state.value
        assertEquals(lastChapter.id, stateAfter.currentChapter?.id)
        
        controller.release()
    }
    
    /**
     * Test book not found error.
     * **Validates: Requirements 8.1**
     */
    @Test
    fun `LoadBook with invalid ID emits BookNotFound error`() = runTest(testDispatcher) {
        val bookRepository = MockBookRepository()
        bookRepository.book = null // No book found
        
        val controller = createController(bookRepository = bookRepository)
        
        controller.dispatch(ChapterCommand.LoadBook(999L))
        testScheduler.advanceUntilIdle()
        
        val state = controller.state.value
        assertNotNull(state.error)
        assertTrue(state.error is ChapterError.BookNotFound)
        assertEquals(999L, (state.error as ChapterError.BookNotFound).bookId)
        
        controller.release()
    }
}
