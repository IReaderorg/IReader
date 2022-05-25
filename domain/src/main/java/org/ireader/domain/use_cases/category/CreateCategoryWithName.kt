package org.ireader.domain.use_cases.category

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_models.entities.Category
import javax.inject.Inject

class CreateCategoryWithName @Inject internal constructor(
  private val categoryRepository: CategoryRepository
) {

  suspend fun await(name: String): Result = withContext(NonCancellable) f@{
    if (name.isBlank()) {
      return@f Result.EmptyCategoryNameError
    }

    val categories = categoryRepository.findAll()
    if (categories.any { name.equals(it.name, ignoreCase = true) }) {
      return@f Result.CategoryAlreadyExistsError(name)
    }

    val nextOrder = categories.maxByOrNull { it.category.sort }?.category?.sort?.plus(1) ?: 0
    val newCategory = Category(
      name = name,
      sort = nextOrder
    )

    try {
      categoryRepository.insert(newCategory)
    } catch (e: Exception) {
      return@f Result.InternalError(e)
    }

    Result.Success
  }

  sealed class Result {
    object Success : Result()
    object EmptyCategoryNameError : Result()
    data class CategoryAlreadyExistsError(val name: String) : Result()
    data class InternalError(val error: Throwable) : Result()
  }

}
