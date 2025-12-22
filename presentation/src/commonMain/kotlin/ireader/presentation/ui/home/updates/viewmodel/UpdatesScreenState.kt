package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.i18n.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDateTime

/**
 * Update progress information
 */
@Immutable
data class UpdateProgress(
    val currentBook: String,
    val currentIndex: Int,
    val totalBooks: Int,
    val estimatedTimeRemaining: Long? = null
)

/**
 * Pagination state for updates screen.
 * Tracks how many items are currently loaded and whether more are available.
 */
@Immutable
data class UpdatesPaginationState(
    val loadedCount: Int = INITIAL_PAGE_SIZE,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = true,
    val totalItems: Int = 0
) {
    companion object {
        const val INITIAL_PAGE_SIZE = 50
        const val PAGE_SIZE = 30
    }
    
    @Stable
    val canLoadMore: Boolean get() = hasMoreItems && !isLoadingMore
}

/**
 * Dialog types for the updates screen
 */
sealed interface UpdatesDialog {
    data object ConfirmDownload : UpdatesDialog
    data object ConfirmMarkAsRead : UpdatesDialog
}

/**
 * Immutable state for the Updates screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class UpdatesScreenState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    
    // Update progress
    val updateProgress: UpdateProgress? = null,
    
    // Data
    val updates: ImmutableMap<LocalDateTime, ImmutableList<UpdatesWithRelations>> = persistentMapOf(),
    val updateHistory: ImmutableList<ireader.domain.models.entities.UpdateHistory> = persistentListOf(),
    val downloadedChapters: ImmutableSet<Long> = persistentSetOf(),
    
    // Selection state
    val selectedChapterIds: ImmutableSet<Long> = persistentSetOf(),
    
    // Filter state
    val selectedCategoryId: Long? = null,
    val categories: ImmutableList<Category> = persistentListOf(),
    
    // Error
    val error: UiText? = null,
    
    // Dialog state
    val dialog: UpdatesDialog? = null,
    
    // Scroll position
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0,
    
    // Pagination state
    val paginationState: UpdatesPaginationState = UpdatesPaginationState()
) {
    @Stable
    val hasSelection: Boolean get() = selectedChapterIds.isNotEmpty()
    
    @Stable
    val selectedCount: Int get() = selectedChapterIds.size
    
    @Stable
    val isEmpty: Boolean get() = updates.isEmpty() && !isLoading
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && updates.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = updates.isNotEmpty()
    
    @Stable
    val totalCount: Int get() = updates.values.sumOf { it.size }
    
}
