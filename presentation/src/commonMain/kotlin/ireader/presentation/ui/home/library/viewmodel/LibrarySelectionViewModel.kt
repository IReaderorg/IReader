package ireader.presentation.ui.home.library.viewmodel

import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for book selection and batch operations
 * 
 * Responsibilities:
 * - Managing selected books
 * - Selection mode state
 * - Batch selection operations (select all, flip, clear)
 * - Category assignment for selected books
 */
class LibrarySelectionViewModel(
    private val getCategory: CategoriesUseCases,
) : BaseViewModel() {

    // Use optimized SelectionManager
    private val selectionManager = SelectionManager()
    
    /**
     * Currently selected book IDs
     */
    val selectedBooks: StateFlow<Set<Long>> = selectionManager.selectedIds
    
    /**
     * Whether selection mode is active
     */
    val isSelectionMode: StateFlow<Boolean> = selectedBooks
        .map { it.isNotEmpty() }
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, false)
    
    /**
     * Number of selected books
     */
    val selectionCount: StateFlow<Int> = selectedBooks
        .map { it.size }
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, 0)
    
    /**
     * Toggle selection for a book
     */
    fun toggleSelection(bookId: Long) {
        selectionManager.toggle(bookId)
    }
    
    /**
     * Check if a book is selected
     */
    fun isSelected(bookId: Long): Boolean {
        return selectionManager.contains(bookId)
    }
    
    /**
     * Select all books in the given list
     */
    fun selectAll(bookIds: List<Long>) {
        selectionManager.addAll(bookIds)
    }
    
    /**
     * Flip selection for given books
     */
    fun flipSelection(bookIds: List<Long>) {
        selectionManager.flip(bookIds)
    }
    
    /**
     * Clear all selections
     */
    fun clearSelection() {
        selectionManager.clear()
    }
    
    /**
     * Get selected book IDs as a set (efficient, no copy)
     */
    fun getSelectedIds(): Set<Long> {
        return selectionManager.toSet()
    }
    
    /**
     * Get selected book IDs as a list (creates copy, use sparingly)
     */
    fun getSelectedIdsList(): List<Long> {
        return selectionManager.toList()
    }
    
    /**
     * Get category assignment state for selected books
     */
    suspend fun getCategoryState(
        category: Category,
        bookCategories: List<BookCategory>
    ): androidx.compose.ui.state.ToggleableState {
        val selectedIds = getSelectedIds()
        val booksInCategory = bookCategories
            .filter { it.categoryId == category.id }
            .map { it.bookId }
            .toSet()
        
        val selectedInCategory = selectedIds.intersect(booksInCategory)
        
        return when {
            selectedInCategory.isEmpty() -> androidx.compose.ui.state.ToggleableState.Off
            selectedInCategory.size == selectedIds.size -> androidx.compose.ui.state.ToggleableState.On
            else -> androidx.compose.ui.state.ToggleableState.Indeterminate
        }
    }
}


