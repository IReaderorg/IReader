package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category

/**
 * Use case for retrieving a category by ID
 */
class GetCategoryByIdUseCase(
    private val categoryRepository: CategoryRepository
) {
    /**
     * Get category by ID
     */
    suspend operator fun invoke(categoryId: Long): Category? {
        return categoryRepository.get(categoryId)
    }
}
