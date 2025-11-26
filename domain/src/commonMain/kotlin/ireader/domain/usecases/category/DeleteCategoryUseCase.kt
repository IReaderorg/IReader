package ireader.domain.usecases.category

import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.BookCategory

/**
 * Use case for deleting a category
 */
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository
) {
    /**
     * Delete a category
     * 
     * @param categoryId The ID of the category to delete
     * @param moveToDefaultCategory If true, move books to default category; if false, remove category assignment
     */
    suspend operator fun invoke(
        categoryId: Long,
        moveToDefaultCategory: Boolean = true
    ): Result<Unit> {
        val category = categoryRepository.get(categoryId)
            ?: return Result.failure(IllegalArgumentException("Category not found"))
        
        // Handle books in this category
        if (moveToDefaultCategory) {
            // Get all book-category associations
            val allBookCategories = bookCategoryRepository.findAll()
            val bookCategoriesForThisCategory = allBookCategories.filter { it.categoryId == categoryId }
            
            // Move books to default category (ID = 0)
            bookCategoriesForThisCategory.forEach { bookCategory ->
                bookCategoryRepository.insert(
                    BookCategory(bookId = bookCategory.bookId, categoryId = 0)
                )
            }
        }
        
        // Delete book-category associations for this category
        val allBookCategories = bookCategoryRepository.findAll()
        val toDelete = allBookCategories.filter { it.categoryId == categoryId }
        if (toDelete.isNotEmpty()) {
            bookCategoryRepository.delete(toDelete)
        }
        
        // Delete the category
        categoryRepository.delete(categoryId)
        
        return Result.success(Unit)
    }
}
