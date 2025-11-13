package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryUpdate
import ireader.domain.models.entities.CategoryWithCount

/**
 * Repository interface for category data access operations.
 * 
 * Categories are used to organize books in the user's library.
 * This repository provides methods for managing categories including
 * CRUD operations, batch updates, and reactive queries.
 */
interface CategoryRepository {

    /**
     * Subscribes to category changes with book counts.
     * 
     * @return Flow emitting list of categories with their book counts when data changes
     */
    fun subscribe(): Flow<List<CategoryWithCount>>
    
    /**
     * Retrieves all categories with their book counts.
     * 
     * @return List of categories with book counts
     */
    suspend fun findAll(): List<CategoryWithCount>

    /**
     * Retrieves a category by its unique identifier.
     * 
     * @param id The unique identifier of the category
     * @return The category if found, null otherwise
     */
    suspend fun get(id: Long): Category?

    /**
     * Retrieves all categories without book counts.
     * 
     * @return List of all categories
     */
    suspend fun getAll(): List<Category>

    /**
     * Subscribes to all category changes.
     * 
     * @return Flow emitting list of categories when data changes
     */
    fun getAllAsFlow(): Flow<List<Category>>

    /**
     * Retrieves categories assigned to a specific book.
     * 
     * @param mangaId The unique identifier of the book
     * @return List of categories assigned to the book
     */
    suspend fun getCategoriesByMangaId(mangaId: Long): List<Category>

    /**
     * Subscribes to category changes for a specific book.
     * 
     * @param mangaId The unique identifier of the book
     * @return Flow emitting list of categories assigned to the book
     */
    fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>>

    /**
     * Inserts a new category into the database.
     * 
     * @param category The category to insert
     */
    suspend fun insert(category: Category)
    
    /**
     * Inserts multiple categories in a batch operation.
     * 
     * @param category List of categories to insert
     */
    suspend fun insert(category: List<Category>)

    /**
     * Updates an existing category.
     * 
     * @param category The category with updated information
     */
    suspend fun update(category: Category)
    
    /**
     * Updates multiple categories in a batch operation.
     * This method is used for operations like reordering categories.
     * 
     * @param categories List of categories to update
     */
    suspend fun updateBatch(categories: List<Category>)

    /**
     * Updates only specific fields of a category.
     * 
     * @param update The category update with partial changes
     */
    suspend fun updatePartial(update: CategoryUpdate)

    /**
     * Updates specific fields of multiple categories.
     * 
     * @param updates List of category updates with partial changes
     */
    suspend fun updatePartial(updates: List<CategoryUpdate>)

    /**
     * Updates flags for all categories.
     * 
     * @param flags The flags value to set, or null to clear flags
     */
    suspend fun updateAllFlags(flags: Long?)

    /**
     * Deletes a category by its unique identifier.
     * 
     * @param categoryId The unique identifier of the category to delete
     */
    suspend fun delete(categoryId: Long)
    
    /**
     * Deletes all categories from the database.
     * WARNING: This operation cannot be undone.
     */
    suspend fun deleteAll()
}
