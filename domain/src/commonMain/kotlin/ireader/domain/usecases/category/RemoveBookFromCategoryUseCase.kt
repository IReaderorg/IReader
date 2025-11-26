package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.models.entities.BookCategory

/**
 * Use case for removing a book from a category
 */
class RemoveBookFromCategoryUseCase(
    private val bookCategoryRepository: BookCategoryRepository
) {
    /**
     * Remove a book from a category
     */
    suspend operator fun invoke(bookId: Long, categoryId: Long) {
        val allBookCategories = bookCategoryRepository.findAll()
        val toDelete = allBookCategories.filter { 
            it.bookId == bookId && it.categoryId == categoryId 
        }
        if (toDelete.isNotEmpty()) {
            bookCategoryRepository.delete(toDelete)
        }
    }
    
    /**
     * Remove a book from all categories
     */
    suspend fun removeFromAll(bookId: Long) {
        bookCategoryRepository.delete(bookId)
    }
    
    /**
     * Remove multiple books from a category
     */
    suspend fun removeMultiple(bookIds: List<Long>, categoryId: Long) {
        bookIds.forEach { bookId ->
            invoke(bookId, categoryId)
        }
    }
}
