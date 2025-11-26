package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category

/**
 * Use case for creating a new category
 */
class CreateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    /**
     * Create a new category
     */
    suspend operator fun invoke(name: String): Result<Unit> {
        // Validate name
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Category name cannot be blank"))
        }
        
        // Check for duplicate
        val existing = categoryRepository.getAll().find { it.name == name }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Category with name '$name' already exists"))
        }
        
        // Get next order - use getAll() which returns List<Category>
        val allCategories = categoryRepository.getAll()
        val maxOrder = allCategories.maxOfOrNull { it.flags.toInt() } ?: 0
        
        // Create category
        val category = Category(
            id = 0,
            name = name,
            flags = (maxOrder + 1).toLong()
        )
        
        categoryRepository.insert(category)
        return Result.success(Unit)
    }
}
