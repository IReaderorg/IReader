package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving categories
 */
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    /**
     * Get all categories as a one-time operation
     */
    suspend operator fun invoke(): List<CategoryWithCount> {
        return categoryRepository.findAll()
    }
    
    /**
     * Subscribe to category changes
     */
    fun subscribe(): Flow<List<CategoryWithCount>> {
        return categoryRepository.subscribe()
    }
    
    /**
     * Get categories with book count
     */
    suspend fun withCount(): List<CategoryWithCount> {
        return categoryRepository.findAll()
    }
    
    /**
     * Get all categories without counts
     */
    suspend fun getAll(): List<Category> {
        return categoryRepository.getAll()
    }
}
