package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryUpdate

/**
 * Use case for updating a category
 */
class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    /**
     * Update a category
     */
    suspend operator fun invoke(category: Category): Result<Unit> {
        // Validate name
        if (category.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name cannot be blank"))
        }
        
        // Check for duplicate name (excluding current category)
        val existing = categoryRepository.getAll()
            .find { it.name == category.name && it.id != category.id }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Category with name '${category.name}' already exists"))
        }
        
        categoryRepository.update(category)
        return Result.success(Unit)
    }
    
    /**
     * Rename a category
     */
    suspend fun rename(categoryId: Long, newName: String): Result<Unit> {
        val category = categoryRepository.get(categoryId)
            ?: return Result.failure(IllegalArgumentException("Category not found"))
        
        // Use updatePartial for efficient update
        categoryRepository.updatePartial(CategoryUpdate(id = categoryId, name = newName))
        return Result.success(Unit)
    }
}
