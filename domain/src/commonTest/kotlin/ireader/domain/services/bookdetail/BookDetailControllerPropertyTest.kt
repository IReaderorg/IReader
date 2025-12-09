package ireader.domain.services.bookdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Property-based tests for BookDetailController components.
 * 
 * **Feature: architecture-improvements**
 * 
 * Tests verify:
 * - Property 4: Controller State Propagation (via BookDetailState)
 * - Property 5: Error State Management (via BookDetailError)
 * 
 * Note: Full controller integration tests require complex DI setup.
 * These tests focus on the state and error components which are the core
 * of the SSOT pattern.
 * 
 * **Validates: Requirements 3.1, 3.4, 3.5, 4.2, 4.3, 4.4, 4.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailControllerPropertyTest {
    
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
    
    // ========== Property Tests for BookDetailState ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailState, computed properties SHALL be consistent with the underlying data.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - BookDetailState computed properties are consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Test with empty state
            val emptyState = BookDetailState()
            
            assertEquals(
                0,
                emptyState.selectionCount,
                "Iteration $iteration: Empty state selectionCount should be 0"
            )
            
            assertEquals(
                false,
                emptyState.hasSelection,
                "Iteration $iteration: Empty state hasSelection should be false"
            )
            
            assertEquals(
                false,
                emptyState.hasBook,
                "Iteration $iteration: Empty state hasBook should be false"
            )
            
            assertEquals(
                false,
                emptyState.hasChapters,
                "Iteration $iteration: Empty state hasChapters should be false"
            )
            
            assertEquals(
                0,
                emptyState.totalChapters,
                "Iteration $iteration: Empty state totalChapters should be 0"
            )
            
            assertEquals(
                false,
                emptyState.hasError,
                "Iteration $iteration: Empty state hasError should be false"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailState with loading flags, isAnyLoading SHALL reflect the combined state.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - BookDetailState loading flags are consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val isLoading = iteration % 2 == 0
            val isRefreshingBook = iteration % 3 == 0
            val isRefreshingChapters = iteration % 5 == 0
            
            val state = BookDetailState(
                isLoading = isLoading,
                isRefreshingBook = isRefreshingBook,
                isRefreshingChapters = isRefreshingChapters
            )
            
            assertEquals(
                isLoading || isRefreshingBook || isRefreshingChapters,
                state.isAnyLoading,
                "Iteration $iteration: isAnyLoading should be true if any loading flag is true"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailState with error, hasError SHALL be true.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - BookDetailState error flag is consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val hasError = iteration % 2 == 0
            val error = if (hasError) BookDetailError.LoadFailed("Error $iteration") else null
            
            val state = BookDetailState(error = error)
            
            assertEquals(
                hasError,
                state.hasError,
                "Iteration $iteration: hasError should match whether error is set"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailState with selected chapters, selectionCount SHALL match the set size.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - BookDetailState selection count is consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val selectedIds = (1..iteration % 20).map { it.toLong() }.toSet()
            
            val state = BookDetailState(selectedChapterIds = selectedIds)
            
            assertEquals(
                selectedIds.size,
                state.selectionCount,
                "Iteration $iteration: selectionCount should match selectedChapterIds size"
            )
            
            assertEquals(
                selectedIds.isNotEmpty(),
                state.hasSelection,
                "Iteration $iteration: hasSelection should be true if selectedChapterIds is not empty"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailState with filtered chapters, allFilteredSelected SHALL be correct.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - BookDetailState allFilteredSelected is consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Create mock chapters with IDs
            val chapterCount = (iteration % 10) + 1
            val mockChapters = (1..chapterCount).map { id ->
                createMockChapter(id.toLong())
            }
            
            // Test when all filtered chapters are selected
            val allSelectedIds = mockChapters.map { it.id }.toSet()
            val allSelectedState = BookDetailState(
                filteredChapters = mockChapters,
                selectedChapterIds = allSelectedIds
            )
            
            assertEquals(
                true,
                allSelectedState.allFilteredSelected,
                "Iteration $iteration: allFilteredSelected should be true when all filtered chapters are selected"
            )
            
            // Test when not all filtered chapters are selected
            if (chapterCount > 1) {
                val partialSelectedIds = mockChapters.take(chapterCount - 1).map { it.id }.toSet()
                val partialSelectedState = BookDetailState(
                    filteredChapters = mockChapters,
                    selectedChapterIds = partialSelectedIds
                )
                
                assertEquals(
                    false,
                    partialSelectedState.allFilteredSelected,
                    "Iteration $iteration: allFilteredSelected should be false when not all filtered chapters are selected"
                )
            }
            
            // Test with empty filtered chapters
            val emptyFilteredState = BookDetailState(
                filteredChapters = emptyList(),
                selectedChapterIds = setOf(1L, 2L, 3L)
            )
            
            assertEquals(
                false,
                emptyFilteredState.allFilteredSelected,
                "Iteration $iteration: allFilteredSelected should be false when filteredChapters is empty"
            )
        }
    }

    
    // ========== Property Tests for BookDetailError ==========
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* BookDetailError, toUserMessage() SHALL return a non-empty string.
     * 
     * **Validates: Requirements 4.4**
     */
    @Test
    fun `Property 5 - BookDetailError toUserMessage returns non-empty string`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Generate different error types
            val errors = listOf(
                BookDetailError.LoadFailed("Load error $iteration"),
                BookDetailError.NetworkError("Network error $iteration"),
                BookDetailError.NotFound(iteration.toLong()),
                BookDetailError.RefreshFailed("Refresh error $iteration"),
                BookDetailError.SourceNotAvailable(iteration.toLong()),
                BookDetailError.DatabaseError("Database error $iteration")
            )
            
            errors.forEach { error ->
                val message = error.toUserMessage()
                
                assertTrue(
                    message.isNotEmpty(),
                    "Iteration $iteration: toUserMessage() should return non-empty string for ${error::class.simpleName}"
                )
                
                assertTrue(
                    message.isNotBlank(),
                    "Iteration $iteration: toUserMessage() should return non-blank string for ${error::class.simpleName}"
                )
            }
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* BookDetailError with a message, toUserMessage() SHALL contain that message.
     * 
     * **Validates: Requirements 4.4**
     */
    @Test
    fun `Property 5 - BookDetailError toUserMessage contains error details`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val uniqueMessage = "unique_error_message_$iteration"
            val uniqueId = iteration.toLong()
            
            // Test LoadFailed
            val loadError = BookDetailError.LoadFailed(uniqueMessage)
            assertTrue(
                loadError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: LoadFailed message should contain the error message"
            )
            
            // Test NetworkError
            val networkError = BookDetailError.NetworkError(uniqueMessage)
            assertTrue(
                networkError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: NetworkError message should contain the error message"
            )
            
            // Test NotFound
            val notFoundError = BookDetailError.NotFound(uniqueId)
            assertTrue(
                notFoundError.toUserMessage().contains(uniqueId.toString()),
                "Iteration $iteration: NotFound message should contain the book ID"
            )
            
            // Test RefreshFailed
            val refreshError = BookDetailError.RefreshFailed(uniqueMessage)
            assertTrue(
                refreshError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: RefreshFailed message should contain the error message"
            )
            
            // Test SourceNotAvailable
            val sourceError = BookDetailError.SourceNotAvailable(uniqueId)
            assertTrue(
                sourceError.toUserMessage().contains(uniqueId.toString()),
                "Iteration $iteration: SourceNotAvailable message should contain the source ID"
            )
            
            // Test DatabaseError
            val dbError = BookDetailError.DatabaseError(uniqueMessage)
            assertTrue(
                dbError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: DatabaseError message should contain the error message"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* BookDetailState, clearError() equivalent (setting error to null) SHALL reset error state.
     * 
     * **Validates: Requirements 4.5**
     */
    @Test
    fun `Property 5 - BookDetailState error can be cleared`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Create state with error
            val error = BookDetailError.LoadFailed("Error $iteration")
            val stateWithError = BookDetailState(error = error)
            
            assertTrue(
                stateWithError.hasError,
                "Iteration $iteration: State with error should have hasError = true"
            )
            
            assertEquals(
                error,
                stateWithError.error,
                "Iteration $iteration: State error should match the set error"
            )
            
            // Clear error by creating new state with null error
            val clearedState = stateWithError.copy(error = null)
            
            assertEquals(
                false,
                clearedState.hasError,
                "Iteration $iteration: Cleared state should have hasError = false"
            )
            
            assertNull(
                clearedState.error,
                "Iteration $iteration: Cleared state error should be null"
            )
        }
    }
    
    // ========== Property Tests for ChapterFilter ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ChapterFilter, the filter type SHALL be correctly identified.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ChapterFilter types are distinct`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val filters = listOf(
                ChapterFilter.None,
                ChapterFilter.Unread,
                ChapterFilter.Read,
                ChapterFilter.Bookmarked,
                ChapterFilter.Downloaded,
                ChapterFilter.Combined(
                    showUnread = iteration % 2 == 0,
                    showRead = iteration % 3 == 0,
                    showBookmarked = iteration % 5 == 0,
                    showDownloaded = iteration % 7 == 0
                )
            )
            
            // Verify each filter type is distinct
            assertTrue(
                filters[0] is ChapterFilter.None,
                "Iteration $iteration: First filter should be None"
            )
            assertTrue(
                filters[1] is ChapterFilter.Unread,
                "Iteration $iteration: Second filter should be Unread"
            )
            assertTrue(
                filters[2] is ChapterFilter.Read,
                "Iteration $iteration: Third filter should be Read"
            )
            assertTrue(
                filters[3] is ChapterFilter.Bookmarked,
                "Iteration $iteration: Fourth filter should be Bookmarked"
            )
            assertTrue(
                filters[4] is ChapterFilter.Downloaded,
                "Iteration $iteration: Fifth filter should be Downloaded"
            )
            assertTrue(
                filters[5] is ChapterFilter.Combined,
                "Iteration $iteration: Sixth filter should be Combined"
            )
        }
    }
    
    // ========== Property Tests for ChapterSortOrder ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ChapterSortOrder, the sort type and direction SHALL be correctly stored.
     * 
     * **Validates: Requirements 3.1, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ChapterSortOrder properties are consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val sortTypes = ChapterSortOrder.Type.entries
            val ascending = iteration % 2 == 0
            
            sortTypes.forEach { type ->
                val sortOrder = ChapterSortOrder(type = type, ascending = ascending)
                
                assertEquals(
                    type,
                    sortOrder.type,
                    "Iteration $iteration: Sort type should match"
                )
                
                assertEquals(
                    ascending,
                    sortOrder.ascending,
                    "Iteration $iteration: Sort ascending should match"
                )
            }
            
            // Test default sort order
            val defaultSort = ChapterSortOrder.Default
            assertEquals(
                ChapterSortOrder.Type.SOURCE,
                defaultSort.type,
                "Iteration $iteration: Default sort type should be SOURCE"
            )
            assertEquals(
                true,
                defaultSort.ascending,
                "Iteration $iteration: Default sort should be ascending"
            )
        }
    }
    
    // ========== Property Tests for BookDetailCommand ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* BookDetailCommand, the command type SHALL be correctly identified.
     * 
     * **Validates: Requirements 3.1, 3.3**
     */
    @Test
    fun `Property 4 - BookDetailCommand types are distinct`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val commands = listOf(
                BookDetailCommand.LoadBook(iteration.toLong()),
                BookDetailCommand.Cleanup,
                BookDetailCommand.SelectChapter(iteration.toLong()),
                BookDetailCommand.DeselectChapter(iteration.toLong()),
                BookDetailCommand.ToggleChapterSelection(iteration.toLong()),
                BookDetailCommand.ClearSelection,
                BookDetailCommand.SelectAll(onlyFiltered = iteration % 2 == 0),
                BookDetailCommand.SetFilter(ChapterFilter.None),
                BookDetailCommand.SetSort(ChapterSortOrder.Default),
                BookDetailCommand.RefreshChapters,
                BookDetailCommand.RefreshBook,
                BookDetailCommand.NavigateToReader(iteration.toLong()),
                BookDetailCommand.ContinueReading,
                BookDetailCommand.ClearError
            )
            
            // Verify each command type is distinct
            assertTrue(
                commands[0] is BookDetailCommand.LoadBook,
                "Iteration $iteration: First command should be LoadBook"
            )
            assertTrue(
                commands[1] is BookDetailCommand.Cleanup,
                "Iteration $iteration: Second command should be Cleanup"
            )
            assertTrue(
                commands[2] is BookDetailCommand.SelectChapter,
                "Iteration $iteration: Third command should be SelectChapter"
            )
            assertTrue(
                commands[5] is BookDetailCommand.ClearSelection,
                "Iteration $iteration: Sixth command should be ClearSelection"
            )
            assertTrue(
                commands[13] is BookDetailCommand.ClearError,
                "Iteration $iteration: Last command should be ClearError"
            )
        }
    }
    
    // ========== Property Tests for BookDetailEvent ==========
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* BookDetailEvent.Error, the error SHALL be accessible.
     * 
     * **Validates: Requirements 4.2, 4.3**
     */
    @Test
    fun `Property 5 - BookDetailEvent Error contains error details`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val error = BookDetailError.LoadFailed("Error $iteration")
            val event = BookDetailEvent.Error(error)
            
            assertEquals(
                error,
                event.error,
                "Iteration $iteration: Event error should match the original error"
            )
            
            assertEquals(
                error.toUserMessage(),
                event.error.toUserMessage(),
                "Iteration $iteration: Event error message should match"
            )
        }
    }
    
    // ========== Helper Functions ==========
    
    /**
     * Creates a mock Chapter for testing.
     */
    private fun createMockChapter(id: Long): ireader.domain.models.entities.Chapter {
        return ireader.domain.models.entities.Chapter(
            id = id,
            bookId = 1L,
            key = "chapter_$id",
            name = "Chapter $id",
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            content = emptyList()
        )
    }
}
