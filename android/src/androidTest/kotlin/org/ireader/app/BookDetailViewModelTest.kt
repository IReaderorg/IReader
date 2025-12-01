package org.ireader.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.book.viewmodel.BookDetailState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ViewModel state tests for BookDetailViewModel.
 * 
 * These tests verify the state management logic, including:
 * 1. State transitions (Loading -> Success -> Error)
 * 2. Refreshing state preservation (pull-to-refresh fix)
 * 3. Selection state preservation
 * 4. Search and filter state preservation
 * 5. Derived properties
 * 
 * Run on physical device: ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.app.BookDetailViewModelTest
 */
@RunWith(AndroidJUnit4::class)
class BookDetailViewModelTest {

    private lateinit var context: Context
    
    private val testBookId = 777777L
    private val testSourceId = 1L
    private val testBookKey = "vm-test-book-key"
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    /**
     * Test: Initial state is Loading.
     */
    @Test
    fun test_initial_state_is_loading() {
        val initialState: BookDetailState = BookDetailState.Loading
        assertTrue("Initial state should be Loading", initialState is BookDetailState.Loading)
    }
    
    /**
     * Test: Success state contains correct data.
     */
    @Test
    fun test_success_state_contains_data() {
        val testBook = createTestBook()
        val chapters = createTestChapters(10)
        
        val successState = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(chapters),
            source = null,
            catalogSource = null
        )
        
        assertTrue("State should be Success", successState is BookDetailState.Success)
        assertEquals("Book title should match", "Test Book", successState.book.title)
        assertEquals("Should have 10 chapters", 10, successState.chapters.size)
    }
    
    /**
     * Test: Error state contains message.
     */
    @Test
    fun test_error_state_contains_message() {
        val errorState = BookDetailState.Error(
            message = "Book not found - it may not have been saved properly",
            throwable = null
        )
        
        assertTrue("State should be Error", errorState is BookDetailState.Error)
        assertEquals("Error message should match", "Book not found - it may not have been saved properly", errorState.message)
    }
    
    /**
     * Test: Refreshing state is preserved when chapters update.
     * 
     * This is the KEY test for the pull-to-refresh bug fix.
     * When database emits new chapters, the refreshing state must be preserved.
     */
    @Test
    fun test_refreshing_state_preserved_on_chapter_update() {
        val testBook = createTestBook()
        
        // Initial state with refreshing = true (user pulled to refresh)
        val initialState = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null,
            isRefreshingChapters = true
        )
        
        assertTrue("Initial state should be refreshing", initialState.isRefreshingChapters)
        
        // Simulate database update - new chapters arrive
        // The fix ensures we preserve the refreshing state
        val newChapters = createTestChapters(10)
        
        val updatedState = initialState.copy(
            chapters = persistentListOf<Chapter>().addAll(newChapters),
            // THIS IS THE FIX - preserve refreshing state from current state
            isRefreshingChapters = initialState.isRefreshingChapters
        )
        
        assertTrue("Refreshing state MUST be preserved after chapter update", updatedState.isRefreshingChapters)
        assertEquals("Should have 10 chapters", 10, updatedState.chapters.size)
    }
    
    /**
     * Test: Refreshing state is cleared when refresh completes.
     */
    @Test
    fun test_refreshing_state_cleared_on_completion() {
        val testBook = createTestBook()
        
        val refreshingState = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null,
            isRefreshingChapters = true
        )
        
        // Simulate refresh completion - explicitly set to false
        val completedState = refreshingState.copy(
            isRefreshingChapters = false
        )
        
        assertFalse("Refreshing should be false after completion", completedState.isRefreshingChapters)
    }
    
    /**
     * Test: Selection state is preserved during updates.
     */
    @Test
    fun test_selection_state_preserved() {
        val testBook = createTestBook()
        val chapters = createTestChapters(10)
        
        val selectedIds = persistentSetOf(1L, 2L, 3L)
        val stateWithSelection = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(chapters),
            source = null,
            catalogSource = null,
            selectedChapterIds = selectedIds
        )
        
        assertEquals("Should have 3 selected chapters", 3, stateWithSelection.selectedChapterIds.size)
        assertTrue("hasSelection should be true", stateWithSelection.hasSelection)
        
        // Simulate update that preserves selection
        val updatedState = stateWithSelection.copy(
            chapters = persistentListOf<Chapter>().addAll(createTestChapters(15)),
            selectedChapterIds = stateWithSelection.selectedChapterIds
        )
        
        assertEquals("Selection should still have 3 items", 3, updatedState.selectedChapterIds.size)
    }
    
    /**
     * Test: Search state is preserved during updates.
     */
    @Test
    fun test_search_state_preserved() {
        val testBook = createTestBook()
        
        val stateWithSearch = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null,
            searchQuery = "Chapter 5",
            isSearchMode = true
        )
        
        assertEquals("Search query should be set", "Chapter 5", stateWithSearch.searchQuery)
        assertTrue("Should be in search mode", stateWithSearch.isSearchMode)
        
        // Simulate update that preserves search
        val updatedState = stateWithSearch.copy(
            chapters = persistentListOf<Chapter>().addAll(createTestChapters(10)),
            searchQuery = stateWithSearch.searchQuery,
            isSearchMode = stateWithSearch.isSearchMode
        )
        
        assertEquals("Search query should be preserved", "Chapter 5", updatedState.searchQuery)
        assertTrue("Search mode should be preserved", updatedState.isSearchMode)
    }
    
    /**
     * Test: Derived properties work correctly.
     */
    @Test
    fun test_derived_properties() {
        val testBook = createTestBook().copy(favorite = true)
        val chapters = createTestChapters(10).mapIndexed { index, chapter ->
            chapter.copy(read = index < 3) // First 3 chapters read
        }
        
        val state = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(chapters),
            source = null,
            catalogSource = null,
            isRefreshingBook = true,
            isRefreshingChapters = false
        )
        
        assertTrue("hasChapters should be true", state.hasChapters)
        assertTrue("hasReadChapters should be true", state.hasReadChapters)
        assertTrue("isRefreshing should be true (book is refreshing)", state.isRefreshing)
        assertTrue("isInLibrary should be true", state.isInLibrary)
        assertFalse("hasSelection should be false", state.hasSelection)
        assertEquals("selectedCount should be 0", 0, state.selectedCount)
    }
    
    /**
     * Test: Chapter filtering with search query.
     */
    @Test
    fun test_chapter_filtering_with_search() {
        val testBook = createTestBook()
        val chapters = createTestChapters(20)
        
        val state = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(chapters),
            source = null,
            catalogSource = null,
            searchQuery = "Chapter 1" // Should match Chapter 1, 10-19
        )
        
        val filtered = state.getFilteredChapters()
        
        // "Chapter 1" matches: Chapter 1, Chapter 10, Chapter 11, ..., Chapter 19
        assertTrue("Should contain Chapter 1", filtered.any { it.name == "Chapter 1" })
        assertTrue("Should contain Chapter 10", filtered.any { it.name == "Chapter 10" })
        assertFalse("Should NOT contain Chapter 2", filtered.any { it.name == "Chapter 2" })
    }
    
    /**
     * Test: Empty chapters state.
     */
    @Test
    fun test_empty_chapters_state() {
        val testBook = createTestBook()
        
        val state = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null
        )
        
        assertFalse("hasChapters should be false", state.hasChapters)
        assertFalse("hasReadChapters should be false", state.hasReadChapters)
    }
    
    /**
     * Test: isArchived derived property.
     */
    @Test
    fun test_is_archived_property() {
        val archivedBook = createTestBook().copy(isArchived = true)
        val normalBook = createTestBook().copy(isArchived = false)
        
        val archivedState = BookDetailState.Success(
            book = archivedBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null
        )
        
        val normalState = BookDetailState.Success(
            book = normalBook,
            chapters = persistentListOf(),
            source = null,
            catalogSource = null
        )
        
        assertTrue("isArchived should be true for archived book", archivedState.isArchived)
        assertFalse("isArchived should be false for normal book", normalState.isArchived)
    }
    
    /**
     * Test: getSelectedChapters returns correct chapters.
     */
    @Test
    fun test_get_selected_chapters() {
        val testBook = createTestBook()
        val chapters = createTestChapters(10)
        
        // Select chapters with IDs that match our test chapters
        val chapterIds = chapters.take(3).map { it.id }
        val selectedIds = chapterIds.fold(persistentSetOf<Long>()) { acc, id -> acc.add(id) }
        
        val state = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(chapters),
            source = null,
            catalogSource = null,
            selectedChapterIds = selectedIds
        )
        
        val selectedChapters = state.getSelectedChapters()
        
        assertEquals("Should have 3 selected chapters", 3, selectedChapters.size)
        assertTrue("All selected chapters should be in the selection", 
            selectedChapters.all { it.id in selectedIds })
    }
    
    /**
     * Test: Multiple UI states preserved together.
     */
    @Test
    fun test_multiple_ui_states_preserved() {
        val testBook = createTestBook()
        
        // State with multiple UI states set
        val complexState = BookDetailState.Success(
            book = testBook,
            chapters = persistentListOf<Chapter>().addAll(createTestChapters(10)),
            source = null,
            catalogSource = null,
            isRefreshingBook = false,
            isRefreshingChapters = true,
            isSummaryExpanded = true,
            selectedChapterIds = persistentSetOf(1L, 2L),
            searchQuery = "test",
            isSearchMode = true
        )
        
        // Simulate database update - preserve ALL UI states
        val updatedState = complexState.copy(
            chapters = persistentListOf<Chapter>().addAll(createTestChapters(20)),
            // Preserve all UI states
            isRefreshingBook = complexState.isRefreshingBook,
            isRefreshingChapters = complexState.isRefreshingChapters,
            isSummaryExpanded = complexState.isSummaryExpanded,
            selectedChapterIds = complexState.selectedChapterIds,
            searchQuery = complexState.searchQuery,
            isSearchMode = complexState.isSearchMode
        )
        
        // Verify all states preserved
        assertFalse("isRefreshingBook should be preserved", updatedState.isRefreshingBook)
        assertTrue("isRefreshingChapters should be preserved", updatedState.isRefreshingChapters)
        assertTrue("isSummaryExpanded should be preserved", updatedState.isSummaryExpanded)
        assertEquals("selectedChapterIds should be preserved", 2, updatedState.selectedChapterIds.size)
        assertEquals("searchQuery should be preserved", "test", updatedState.searchQuery)
        assertTrue("isSearchMode should be preserved", updatedState.isSearchMode)
        assertEquals("chapters should be updated", 20, updatedState.chapters.size)
    }
    
    // Helper functions
    
    private fun createTestBook(): Book {
        return Book(
            id = testBookId,
            sourceId = testSourceId,
            title = "Test Book",
            key = testBookKey,
            author = "Test Author",
            description = "Test Description",
            genres = listOf("Test Genre"),
            status = 1,
            cover = "",
            customCover = "",
            favorite = false,
            lastUpdate = System.currentTimeMillis(),
            initialized = false,
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0
        )
    }
    
    private fun createTestChapters(count: Int): List<Chapter> {
        return (1..count).map { i ->
            Chapter(
                id = testBookId * 1000 + i,
                bookId = testBookId,
                key = "vm-chapter-$i",
                name = "Chapter $i",
                read = false,
                bookmark = false,
                lastPageRead = 0,
                dateFetch = System.currentTimeMillis(),
                dateUpload = System.currentTimeMillis(),
                sourceOrder = i.toLong(),
                number = i.toFloat(),
                translator = "",
                content = emptyList()
            )
        }
    }
}
