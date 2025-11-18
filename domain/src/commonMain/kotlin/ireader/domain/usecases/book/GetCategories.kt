package ireader.domain.usecases.book

import ireader.core.log.IReaderLog
import ireader.domain.data.repository.consolidated.CategoryRepository
import ireader.domain.models.entities.Category
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving category information following Mihon's pattern.
 * Provides both suspend and Flow-based access to category data.
 */
class GetCategories(
    private val categoryRepository: CategoryRepository,
) {
    /**
     * Get all categories as a suspend function
     */
    suspend fun await(): List<Category> {
        return try {
            categoryRepository.getAll()
        } catch (e: Exception) {
            IReaderLog.error("Failed to get all categories", e, "GetCategories")
            emptyList()
        }
    }

    /**
     * Subscribe to all categories as a Flow
     */
    fun subscribe(): Flow<List<Category>> {
        return categoryRepository.getAllAsFlow()
    }

    /**
     * Get a category by ID
     */
    suspend fun awaitCategory(id: Long): Category? {
        return try {
            categoryRepository.getCategoryById(id)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get category by id: $id", e, "GetCategories")
            null
        }
    }

    /**
     * Get categories for a specific book
     */
    suspend fun awaitForBook(bookId: Long): List<Category> {
        return try {
            categoryRepository.getCategoriesByBookId(bookId)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get categories for book: $bookId", e, "GetCategories")
            emptyList()
        }
    }

    /**
     * Subscribe to categories for a specific book
     */
    fun subscribeForBook(bookId: Long): Flow<List<Category>> {
        return categoryRepository.getCategoriesByBookIdAsFlow(bookId)
    }
}