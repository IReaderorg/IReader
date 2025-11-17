package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated CategoryRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential category operations with proper category-book
 * relationship management and batch operations.
 */
interface CategoryRepository {
    
    // Basic category operations
    suspend fun getAll(): List<Category>
    fun getAllAsFlow(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    
    // Book-category relationship operations
    suspend fun getCategoriesByBookId(bookId: Long): List<Category>
    fun getCategoriesByBookIdAsFlow(bookId: Long): Flow<List<Category>>
    
    // Category creation
    suspend fun insert(name: String, order: Long): Category?
    
    // Update operations following Mihon's pattern
    suspend fun update(update: CategoryUpdate): Boolean
    suspend fun updatePartial(updates: List<CategoryUpdate>): Boolean
    
    // Deletion operations
    suspend fun delete(categoryId: Long): Boolean
    
    // Batch operations
    suspend fun updateAllFlags(flags: Long?): Boolean
    suspend fun reorderCategories(categories: List<Category>): Boolean
}