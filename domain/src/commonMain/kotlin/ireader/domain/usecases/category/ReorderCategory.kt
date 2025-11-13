package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Use case for reordering categories in the library.
 * 
 * This use case handles the logic for changing the position of a category
 * in the user's category list. It updates the order field of all affected
 * categories to maintain a consistent ordering.
 * 
 * System categories (ALL and UNCATEGORIZED) cannot be reordered.
 * 
 * @property categoryRepository Repository for category data access
 */
class ReorderCategory  internal constructor(
    private val categoryRepository: CategoryRepository
) {

    /**
     * Reorders a category to a new position.
     * 
     * This method moves a category from its current position to the specified
     * new position, updating the order of all affected categories. The operation
     * is performed in a non-cancellable context to ensure data consistency.
     * 
     * @param categoryId The unique identifier of the category to reorder
     * @param newPosition The target position (0-based index)
     * @return Result indicating success, no change, or an error
     */
    suspend fun await(categoryId: Long, newPosition: Int) = withContext(NonCancellable) f@{
        if (categoryId == Category.ALL_ID || categoryId == Category.UNCATEGORIZED_ID) return@f Result.InternalError(
            IllegalArgumentException()
        )
        val categories = categoryRepository.findAll().filter { !it.category.isSystemCategory }

        // If nothing changed, return
        val currPosition = categories.indexOfFirst { it.id == categoryId }

        if (currPosition == newPosition || currPosition == -1) {
            return@f Result.Unchanged
        }

        val reorderedCategories = categories.toMutableList()
        val movedCategory = reorderedCategories.removeAt(currPosition)
        reorderedCategories.add(newPosition, movedCategory)

        val updates = reorderedCategories.mapIndexed { index, category ->
            category.category.copy(order = index.toLong())
        }

        try {
            categoryRepository.updateBatch(updates)
        } catch (e: Exception) {
            return@f Result.InternalError(e)
        }
        Result.Success
    }

    /**
     * Reorders a category to a new position.
     * 
     * Convenience method that accepts a Category object instead of just the ID.
     * 
     * @param category The category to reorder
     * @param newPosition The target position (0-based index)
     * @return Result indicating success, no change, or an error
     */
    suspend fun await(category: Category, newPosition: Int): Result {
        return await(category.id, newPosition)
    }

    /**
     * Result of the reorder operation.
     */
    sealed class Result {
        /** The category was successfully reordered */
        object Success : Result()
        
        /** No reordering was needed (category already at target position) */
        object Unchanged : Result()
        
        /** An error occurred during the reorder operation */
        data class InternalError(val error: Throwable) : Result()
    }
}
