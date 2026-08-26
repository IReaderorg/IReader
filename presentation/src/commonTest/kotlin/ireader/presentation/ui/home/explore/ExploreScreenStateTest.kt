package ireader.presentation.ui.home.explore

import ireader.i18n.UiText
import ireader.presentation.ui.home.explore.viewmodel.ExploreScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExploreScreenStateTest {

    @Test
    fun test_initial_state_defaults() {
        val state = ExploreScreenState()
        assertFalse(state.isLoading, "Default isLoading should be false")
        assertEquals(1, state.page, "Default page should be 1")
        assertTrue(state.books.isEmpty(), "Default books should be empty")
        assertFalse(state.isInitialLoading, "isInitialLoading should be false when isLoading is false")
        assertFalse(state.hasContent, "hasContent should be false when books are empty")
        assertFalse(state.isLikelyBrokenSource, "Default state without error should not be marked as broken")
    }

    @Test
    fun test_isInitialLoading_true_only_when_loading_first_page_with_no_books() {
        val loadingFirstPage = ExploreScreenState(
            isLoading = true,
            page = 1,
            books = emptyList()
        )
        assertTrue(loadingFirstPage.isInitialLoading, "Should be initial loading on page 1 with no books")

        val loadingSubsequentPage = ExploreScreenState(
            isLoading = true,
            page = 2,
            books = emptyList()
        )
        assertFalse(loadingSubsequentPage.isInitialLoading, "Should not be initial loading on page 2")

        val loadingWithBooks = ExploreScreenState(
            isLoading = true,
            page = 1,
            books = listOf(ireader.domain.models.entities.Book(
                id = 1L,
                sourceId = 1L,
                title = "Test",
                key = "key",
                author = "",
                description = "",
                genres = emptyList(),
                status = 0,
                cover = "",
                customCover = "",
                favorite = false,
                lastUpdate = 0L,
                initialized = true,
                dateAdded = 0L,
                viewer = 0,
                flags = 0
            ))
        )
        assertFalse(loadingWithBooks.isInitialLoading, "Should not be initial loading when books are present")
    }

    @Test
    fun test_recovery_from_stuck_loading_state() {
        // Simulates state when loadJob was cancelled mid-flight
        val stuckState = ExploreScreenState(
            isLoading = true,
            page = 1,
            books = emptyList()
        )
        assertTrue(stuckState.isInitialLoading)

        // After resetting stuck loading
        val recoveredState = stuckState.copy(isLoading = false)
        assertFalse(recoveredState.isInitialLoading)
        assertFalse(recoveredState.isLoading)
    }

    @Test
    fun test_isErrorWithNoContent() {
        val errorState = ExploreScreenState(
            error = UiText.DynamicString("Network failed"),
            books = emptyList(),
            isLoading = false,
            isSourceBroken = false,
            isSearchModeEnabled = true // in search mode, empty results is a normal network error
        )
        assertTrue(errorState.isErrorWithNoContent)

        val loadingErrorState = errorState.copy(isLoading = true)
        assertFalse(loadingErrorState.isErrorWithNoContent)
    }

    @Test
    fun test_onScreenResumed_conditions() {
        // Condition: No books, no error, not end reached -> needs load
        val emptyState = ExploreScreenState(
            books = emptyList(),
            error = null,
            endReached = false
        )
        val needsLoad = emptyState.books.isEmpty() && !emptyState.endReached && emptyState.error == null
        assertTrue(needsLoad, "Empty state without errors should trigger load on resume")

        // Condition: Already has books -> does NOT need load on resume
        val populatedState = ExploreScreenState(
            books = listOf(ireader.domain.models.entities.Book(
                id = 1L,
                sourceId = 1L,
                title = "Test",
                key = "key",
                author = "",
                description = "",
                genres = emptyList(),
                status = 0,
                cover = "",
                customCover = "",
                favorite = false,
                lastUpdate = 0L,
                initialized = true,
                dateAdded = 0L,
                viewer = 0,
                flags = 0
            ))
        )
        val populatedNeedsLoad = populatedState.books.isEmpty() && !populatedState.endReached && populatedState.error == null
        assertFalse(populatedNeedsLoad, "Populated state should retain loaded books without reload")
    }
}
