package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/**
 * Date filter options for history
 */
enum class DateFilter {
    TODAY, YESTERDAY, PAST_7_DAYS
}

/**
 * Dialog types for the history screen
 */
sealed interface HistoryDialog {
    data class DeleteHistory(val history: HistoryWithRelations) : HistoryDialog
    data object DeleteAllHistory : HistoryDialog
}

/**
 * Immutable state for the History screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class HistoryScreenState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    
    // Data
    val histories: ImmutableMap<Long, ImmutableList<HistoryWithRelations>> = persistentMapOf(),
    
    // Search state
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    
    // Filter state
    val groupByNovel: Boolean = false,
    val dateFilter: DateFilter? = null,
    
    // Error
    val error: UiText? = null,
    
    // Dialog state
    val dialog: HistoryDialog? = null,
    
    // Scroll position
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0
) {
    @Stable
    val isEmpty: Boolean get() = histories.isEmpty() && !isLoading
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && histories.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = histories.isNotEmpty()
    
    @Stable
    val totalCount: Int get() = histories.values.sumOf { it.size }
}

/**
 * UI model for history list items
 */
sealed class HistoryUiModel {
    data class Header(val date: String) : HistoryUiModel()
    data class Item(val item: HistoryWithRelations) : HistoryUiModel()
}
