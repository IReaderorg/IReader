package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.models.entities.BookCategory

/**
 * Use case for assigning a book to a category
 */
class AssignBookToCategoryUseCase(
    private val bookCategoryRepository: BookCategoryRepository
) {
    /**
     * Assign a book to a category
     */
    suspend operator fun invoke(bookId: Long, categoryId: Long) {
        val bookCategory = BookCategory(
            bookId = bookId,
            categoryId = categoryId
        )
        bookCategoryRepository.insert(bookCategory)
    }
    
    /**
     * Assign a book to multiple categories
     */
    suspend fun assignToMultiple(bookId: Long, categoryIds: List<Long>) {
        categoryIds.forEach { categoryId ->
            invoke(bookId, categoryId)
        }
    }
    
    /**
     * Replace all category assignments for a book
     */
    suspend fun replaceCategories(bookId: Long, categoryIds: List<Long>) {
        // Remove existing assignments
        bookCategoryRepository.delete(bookId)
        
        // Add new assignments
        assignToMultiple(bookId, categoryIds)
    }
}
