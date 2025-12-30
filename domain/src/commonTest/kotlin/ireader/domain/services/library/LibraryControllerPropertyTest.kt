package ireader.domain.services.library

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.entities.CategoryUpdate
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort as DomainLibrarySort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for LibraryController.
 * 
 * These tests verify the correctness properties defined in the design document.
 * Each test runs multiple iterations with randomly generated data to ensure
 * properties hold across all valid inputs.
 * 
 * **Feature: architecture-optimization**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryControllerPropertyTest {
    
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
    
    /**
     * Mock LibraryRepository that stores books in memory.
     */
    private class MockLibraryRepository : LibraryRepository {
        private val books = mutableListOf<LibraryBook>()
        private val booksFlow = MutableStateFlow<List<LibraryBook>>(emptyList())
        
        fun setBooks(newBooks: List<LibraryBook>) {
            books.clear()
            books.addAll(newBooks)
            booksFlow.value = newBooks
        }
        
        override suspend fun findAll(sort: DomainLibrarySort, includeArchived: Boolean): List<LibraryBook> {
            return books.toList()
        }
        
        override suspend fun findAllPaginated(
            sort: DomainLibrarySort,
            limit: Int,
            offset: Int,
            includeArchived: Boolean
        ): List<LibraryBook> = books.drop(offset).take(limit)
        
        override suspend fun getLibraryCount(includeArchived: Boolean): Int = books.size
        
        override suspend fun findByCategoryPaginated(
            categoryId: Long,
            sort: DomainLibrarySort,
            limit: Int,
            offset: Int,
            includeArchived: Boolean
        ): List<LibraryBook> = books.filter { it.category.toLong() == categoryId }.drop(offset).take(limit)
        
        override suspend fun getLibraryCountByCategory(categoryId: Long, includeArchived: Boolean): Int =
            books.count { it.category.toLong() == categoryId }
        
        override suspend fun findUncategorizedPaginated(
            sort: DomainLibrarySort,
            limit: Int,
            offset: Int,
            includeArchived: Boolean
        ): List<LibraryBook> = books.filter { it.category == 0 }.drop(offset).take(limit)
        
        override suspend fun getUncategorizedCount(includeArchived: Boolean): Int =
            books.count { it.category == 0 }
        
        override fun subscribe(sort: DomainLibrarySort, includeArchived: Boolean): Flow<List<LibraryBook>> {
            return booksFlow
        }
        
        override fun subscribeFast(sort: DomainLibrarySort, includeArchived: Boolean): Flow<List<LibraryBook>> {
            return booksFlow
        }
        
        override suspend fun findAllFast(sort: DomainLibrarySort, includeArchived: Boolean): List<LibraryBook> {
            return books.toList()
        }
        
        override suspend fun findDownloadedBooks(): List<ireader.domain.models.entities.Book> = emptyList()
        
        override suspend fun findFavorites(): List<ireader.domain.models.entities.Book> = emptyList()
        
        override suspend fun searchPaginated(
            query: String,
            sort: DomainLibrarySort,
            limit: Int,
            offset: Int,
            includeArchived: Boolean
        ): List<LibraryBook> = books.filter { it.title.contains(query, ignoreCase = true) }.drop(offset).take(limit)
        
        override suspend fun getSearchCount(query: String, includeArchived: Boolean): Int =
            books.count { it.title.contains(query, ignoreCase = true) }
        
        override suspend fun getCurrentlyReadingCount(): Int =
            books.count { it.readCount > 0 && it.unreadCount > 0 }
        
        override suspend fun getRecentlyAddedCount(daysAgo: Int): Int = 0
        
        override suspend fun getCompletedCount(): Int =
            books.count { it.readCount > 0 && it.unreadCount == 0 }
        
        override suspend fun getUnreadCount(): Int =
            books.count { it.readCount == 0 }
        
        override suspend fun getArchivedCount(): Int = 0
    }
    
    /**
     * Mock CategoryRepository that stores categories in memory.
     */
    private class MockCategoryRepository : CategoryRepository {
        private val categories = mutableMapOf<Long, Category>()
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
        
        fun setCategories(newCategories: List<Category>) {
            categories.clear()
            newCategories.forEach { categories[it.id] = it }
            categoriesFlow.value = newCategories
        }
        
        override fun subscribe(): Flow<List<CategoryWithCount>> = flowOf(emptyList())
        override suspend fun findAll(): List<CategoryWithCount> = emptyList()
        override suspend fun get(id: Long): Category? = categories[id]
        override suspend fun getAll(): List<Category> = categories.values.toList()
        override fun getAllAsFlow(): Flow<List<Category>> = categoriesFlow
        override suspend fun insert(category: Category) { categories[category.id] = category }
        override suspend fun insert(category: List<Category>) { category.forEach { categories[it.id] = it } }
        override suspend fun update(category: Category) { categories[category.id] = category }
        override suspend fun updateBatch(categories: List<Category>) {}
        override suspend fun updatePartial(update: CategoryUpdate) {}
        override suspend fun updatePartial(updates: List<CategoryUpdate>) {}
        override suspend fun updateAllFlags(flags: Long?) {}
        override suspend fun delete(categoryId: Long) { categories.remove(categoryId) }
        override suspend fun deleteAll() { categories.clear() }
        override fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>> = flowOf(emptyList())
        override suspend fun getCategoriesByMangaId(mangaId: Long): List<Category> = emptyList()
    }


    // ========== Test Helpers ==========
    
    private fun createMockLibraryRepository(): MockLibraryRepository = MockLibraryRepository()
    private fun createMockCategoryRepository(): MockCategoryRepository = MockCategoryRepository()
    
    private fun createController(
        libraryRepository: MockLibraryRepository,
        categoryRepository: MockCategoryRepository
    ): LibraryController {
        return LibraryController(libraryRepository, categoryRepository)
    }
    
    /**
     * Create a random LibraryBook for testing.
     */
    private fun createRandomLibraryBook(
        id: Long = Random.nextLong(1, 10000),
        categoryId: Int = Random.nextInt(0, 10),
        readCount: Int = Random.nextInt(0, 50),
        unreadCount: Int = Random.nextInt(0, 50)
    ): LibraryBook {
        return LibraryBook(
            id = id,
            sourceId = Random.nextLong(1, 100),
            key = "book-key-$id",
            title = "Book Title $id",
            status = 0L,
            cover = "cover-$id",
            lastUpdate = System.currentTimeMillis() - Random.nextLong(0, 1000000)
        ).apply {
            this.category = categoryId
            this.readCount = readCount
            this.unreadCount = unreadCount
            this.lastRead = System.currentTimeMillis() - Random.nextLong(0, 1000000)
        }
    }
    
    /**
     * Create a list of random LibraryBooks.
     */
    private fun createRandomLibraryBooks(count: Int): List<LibraryBook> {
        return (1..count).map { createRandomLibraryBook(id = it.toLong()) }
    }


    // ========== Property Tests ==========
    
    /**
     * **Feature: architecture-optimization, Property 8: Library Filter Correctness**
     * 
     * *For any* LibraryFilter, the filteredBooks list SHALL contain only books 
     * that satisfy the filter predicate.
     * 
     * **Validates: Requirements 3.1, 3.2, 7.3**
     */
    @Test
    fun `Property 8 - Library Filter Correctness - filteredBooks contains only books satisfying filter predicate`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val libraryRepository = createMockLibraryRepository()
            val categoryRepository = createMockCategoryRepository()
            val controller = createController(libraryRepository, categoryRepository)
            
            // Generate random books with varying properties
            val numBooks = Random.nextInt(5, 20)
            val books = (1..numBooks).map { i ->
                createRandomLibraryBook(
                    id = i.toLong(),
                    categoryId = Random.nextInt(1, 5),
                    readCount = Random.nextInt(0, 10),
                    unreadCount = Random.nextInt(0, 10)
                )
            }
            libraryRepository.setBooks(books)
            
            // Load library
            controller.dispatch(LibraryCommand.LoadLibrary)
            testScheduler.advanceUntilIdle()
            
            // Test each filter type
            val filters = listOf(
                LibraryFilter.None,
                LibraryFilter.Downloaded,
                LibraryFilter.Unread,
                LibraryFilter.Started,
                LibraryFilter.Completed,
                LibraryFilter.Category(Random.nextInt(1, 5).toLong())
            )
            
            for (filter in filters) {
                controller.dispatch(LibraryCommand.SetFilter(filter))
                testScheduler.advanceUntilIdle()
                
                val state = controller.state.value
                val predicate = filter.toPredicate()
                
                // Verify all filtered books satisfy the predicate
                for (book in state.filteredBooks) {
                    assertTrue(
                        predicate(book),
                        "Iteration $iteration, Filter $filter: Book ${book.id} should satisfy filter predicate"
                    )
                }
                
                // Verify no books that satisfy the predicate are missing
                val expectedBooks = books.filter(predicate)
                assertEquals(
                    expectedBooks.size,
                    state.filteredBooks.size,
                    "Iteration $iteration, Filter $filter: filteredBooks count should match expected"
                )
            }
            
            controller.release()
        }
    }


    /**
     * **Feature: architecture-optimization, Property 9: Filter/Sort Preserves Selection**
     * 
     * *For any* filter or sort command, the selectedBookIds set SHALL remain 
     * unchanged after the operation.
     * 
     * **Validates: Requirements 3.2, 3.3**
     */
    @Test
    fun `Property 9 - Filter Sort Preserves Selection - selectedBookIds unchanged after filter or sort`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val libraryRepository = createMockLibraryRepository()
            val categoryRepository = createMockCategoryRepository()
            val controller = createController(libraryRepository, categoryRepository)
            
            // Generate random books
            val numBooks = Random.nextInt(10, 30)
            val books = createRandomLibraryBooks(numBooks)
            libraryRepository.setBooks(books)
            
            // Load library
            controller.dispatch(LibraryCommand.LoadLibrary)
            testScheduler.advanceUntilIdle()
            
            // Select some random books
            val numSelections = Random.nextInt(1, minOf(5, numBooks))
            val selectedIds = books.shuffled().take(numSelections).map { it.id }.toSet()
            
            for (bookId in selectedIds) {
                controller.dispatch(LibraryCommand.SelectBook(bookId))
            }
            testScheduler.advanceUntilIdle()
            
            // Verify initial selection
            assertEquals(
                selectedIds,
                controller.state.value.selectedBookIds,
                "Iteration $iteration: Initial selection should match"
            )
            
            // Apply a filter and verify selection is preserved
            val filter = LibraryFilter.Downloaded
            controller.dispatch(LibraryCommand.SetFilter(filter))
            testScheduler.advanceUntilIdle()
            
            assertEquals(
                selectedIds,
                controller.state.value.selectedBookIds,
                "Iteration $iteration: Selection should be preserved after SetFilter"
            )
            
            // Apply a sort and verify selection is preserved
            val sort = LibrarySort(LibrarySort.Type.DateAdded, ascending = false)
            controller.dispatch(LibraryCommand.SetSort(sort))
            testScheduler.advanceUntilIdle()
            
            assertEquals(
                selectedIds,
                controller.state.value.selectedBookIds,
                "Iteration $iteration: Selection should be preserved after SetSort"
            )
            
            // Apply another filter and verify selection is still preserved
            controller.dispatch(LibraryCommand.SetFilter(LibraryFilter.None))
            testScheduler.advanceUntilIdle()
            
            assertEquals(
                selectedIds,
                controller.state.value.selectedBookIds,
                "Iteration $iteration: Selection should be preserved after changing filter back to None"
            )
            
            controller.release()
        }
    }


    /**
     * **Feature: architecture-optimization, Property 10: Selection Idempotence**
     * 
     * *For any* book ID, dispatching SelectBook multiple times SHALL result in 
     * the same state as dispatching it once.
     * 
     * **Validates: Requirements 3.4**
     */
    @Test
    fun `Property 10 - Selection Idempotence - SelectBook multiple times equals single dispatch`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val libraryRepository = createMockLibraryRepository()
            val categoryRepository = createMockCategoryRepository()
            val controller = createController(libraryRepository, categoryRepository)
            
            // Generate random books
            val numBooks = Random.nextInt(5, 20)
            val books = createRandomLibraryBooks(numBooks)
            libraryRepository.setBooks(books)
            
            // Load library
            controller.dispatch(LibraryCommand.LoadLibrary)
            testScheduler.advanceUntilIdle()
            
            // Pick a random book to select
            val bookToSelect = books.random()
            
            // Select the book once
            controller.dispatch(LibraryCommand.SelectBook(bookToSelect.id))
            testScheduler.advanceUntilIdle()
            
            val stateAfterFirstSelect = controller.state.value.selectedBookIds.toSet()
            
            // Select the same book multiple times
            val numAdditionalSelects = Random.nextInt(2, 10)
            repeat(numAdditionalSelects) {
                controller.dispatch(LibraryCommand.SelectBook(bookToSelect.id))
            }
            testScheduler.advanceUntilIdle()
            
            val stateAfterMultipleSelects = controller.state.value.selectedBookIds.toSet()
            
            // Verify state is the same
            assertEquals(
                stateAfterFirstSelect,
                stateAfterMultipleSelects,
                "Iteration $iteration: Selection state should be identical after selecting same book $numAdditionalSelects additional times"
            )
            
            // Verify the book is in the selection
            assertTrue(
                controller.state.value.selectedBookIds.contains(bookToSelect.id),
                "Iteration $iteration: Book ${bookToSelect.id} should be in selection"
            )
            
            // Verify selection count is 1 (only one book selected)
            assertEquals(
                1,
                controller.state.value.selectionCount,
                "Iteration $iteration: Selection count should be 1 after selecting same book multiple times"
            )
            
            controller.release()
        }
    }


    /**
     * **Feature: architecture-optimization, Property 11: Clear Selection Empties Set**
     * 
     * *For any* selection state, dispatching ClearSelection SHALL result in 
     * an empty selectedBookIds set.
     * 
     * **Validates: Requirements 3.5**
     */
    @Test
    fun `Property 11 - Clear Selection Empties Set - ClearSelection results in empty selectedBookIds`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val libraryRepository = createMockLibraryRepository()
            val categoryRepository = createMockCategoryRepository()
            val controller = createController(libraryRepository, categoryRepository)
            
            // Generate random books
            val numBooks = Random.nextInt(5, 30)
            val books = createRandomLibraryBooks(numBooks)
            libraryRepository.setBooks(books)
            
            // Load library
            controller.dispatch(LibraryCommand.LoadLibrary)
            testScheduler.advanceUntilIdle()
            
            // Select a random number of books
            val numSelections = Random.nextInt(0, numBooks)
            val booksToSelect = books.shuffled().take(numSelections)
            
            for (book in booksToSelect) {
                controller.dispatch(LibraryCommand.SelectBook(book.id))
            }
            testScheduler.advanceUntilIdle()
            
            // Verify we have the expected number of selections
            assertEquals(
                numSelections,
                controller.state.value.selectionCount,
                "Iteration $iteration: Should have $numSelections books selected before clear"
            )
            
            // Clear selection
            controller.dispatch(LibraryCommand.ClearSelection)
            testScheduler.advanceUntilIdle()
            
            // Verify selection is empty
            assertTrue(
                controller.state.value.selectedBookIds.isEmpty(),
                "Iteration $iteration: selectedBookIds should be empty after ClearSelection"
            )
            
            assertEquals(
                0,
                controller.state.value.selectionCount,
                "Iteration $iteration: selectionCount should be 0 after ClearSelection"
            )
            
            assertEquals(
                false,
                controller.state.value.hasSelection,
                "Iteration $iteration: hasSelection should be false after ClearSelection"
            )
            
            controller.release()
        }
    }
    
    /**
     * **Feature: architecture-optimization, Property 11: Clear Selection Empties Set (Idempotent)**
     * 
     * *For any* selection state, dispatching ClearSelection multiple times SHALL 
     * result in the same empty state.
     * 
     * **Validates: Requirements 3.5**
     */
    @Test
    fun `Property 11 - Clear Selection Empties Set - Multiple ClearSelection calls are idempotent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS / 10) { iteration ->
            val libraryRepository = createMockLibraryRepository()
            val categoryRepository = createMockCategoryRepository()
            val controller = createController(libraryRepository, categoryRepository)
            
            // Generate random books
            val numBooks = Random.nextInt(5, 20)
            val books = createRandomLibraryBooks(numBooks)
            libraryRepository.setBooks(books)
            
            // Load library
            controller.dispatch(LibraryCommand.LoadLibrary)
            testScheduler.advanceUntilIdle()
            
            // Select some books
            val numSelections = Random.nextInt(1, numBooks)
            books.shuffled().take(numSelections).forEach { book ->
                controller.dispatch(LibraryCommand.SelectBook(book.id))
            }
            testScheduler.advanceUntilIdle()
            
            // Clear selection multiple times
            val numClears = Random.nextInt(2, 5)
            repeat(numClears) {
                controller.dispatch(LibraryCommand.ClearSelection)
            }
            testScheduler.advanceUntilIdle()
            
            // Verify selection is still empty
            assertTrue(
                controller.state.value.selectedBookIds.isEmpty(),
                "Iteration $iteration: selectedBookIds should be empty after $numClears ClearSelection calls"
            )
            
            controller.release()
        }
    }
}
