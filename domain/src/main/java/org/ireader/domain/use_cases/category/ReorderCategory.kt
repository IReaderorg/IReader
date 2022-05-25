package org.ireader.domain.use_cases.category

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_models.entities.Category
import javax.inject.Inject

class ReorderCategory @Inject internal constructor(
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
      category.category.copy(sort = index)
    }

    try {
      categoryRepository.insertAll(updates)
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
