package ireader.domain.usecases.category

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category



class ReorderCategory  internal constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend fun await(categoryId: Long, newPosition: Int) = withContext(NonCancellable) f@{
        if (categoryId == Category.ALL_ID || categoryId == Category.UNCATEGORIZED_ID) return@f Result.InternalError(IllegalArgumentException())
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
            categoryRepository.insert(updates)
        } catch (e: Exception) {
            return@f Result.InternalError(e)
        }
        Result.Success
    }

    suspend fun await(category: Category, newPosition: Int): Result {
        return await(category.id, newPosition)
    }

    sealed class Result {
        object Success : Result()
        object Unchanged : Result()
        data class InternalError(val error: Throwable) : Result()
    }
}
