package ireader.data.repository.consolidated

import ireader.core.log.IReaderLog
import ireader.data.category.categoryMapper
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.consolidated.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryUpdate
import ireader.domain.models.errors.IReaderError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * CategoryRepository implementation following Mihon's DatabaseHandler pattern.
 * 
 * This implementation provides proper category-book relationship management
 * and batch operations with comprehensive error handling.
 */
class CategoryRepositoryImpl(
    private val handler: DatabaseHandler,
) : CategoryRepository {

    override suspend fun getAll(): List<Category> {
        return try {
            handler.awaitList { 
                categoryQueries.getCategories(categoryMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get all categories", e, "CategoryRepository")
            emptyList()
        }
    }

    override fun getAllAsFlow(): Flow<List<Category>> {
        return handler.subscribeToList { 
            categoryQueries.getCategories(categoryMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to all categories", e, "CategoryRepository")
            emit(emptyList())
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return try {
            handler.awaitOneOrNull { 
                categoryQueries.getCategory(id, categoryMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get category by id: $id", e, "CategoryRepository")
            null
        }
    }

    override suspend fun getCategoriesByBookId(bookId: Long): List<Category> {
        return try {
            handler.awaitList { 
                categoryQueries.getCategoriesByMangaId(bookId, categoryMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get categories for book: $bookId", e, "CategoryRepository")
            emptyList()
        }
    }

    override fun getCategoriesByBookIdAsFlow(bookId: Long): Flow<List<Category>> {
        return handler.subscribeToList { 
            categoryQueries.getCategoriesByMangaId(bookId, categoryMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to categories for book: $bookId", e, "CategoryRepository")
            emit(emptyList())
        }
    }

    override suspend fun insert(name: String, order: Long): Category? {
        return try {
            var insertedId: Long = 0
            handler.await {
                categoryQueries.insert(
                    name = name,
                    order = order,
                    flags = 0L
                )
                insertedId = categoryQueries.selectLastInsertedRowId().executeAsOne()
            }
            val category = Category(
                id = insertedId,
                name = name,
                order = order,
                flags = 0L
            )
            IReaderLog.debug("Successfully inserted category: $name", "CategoryRepository")
            category
        } catch (e: Exception) {
            IReaderLog.error("Failed to insert category: $name", e, "CategoryRepository")
            null
        }
    }

    override suspend fun update(update: CategoryUpdate): Boolean {
        return try {
            partialUpdate(update)
            IReaderLog.debug("Successfully updated category: ${update.id}", "CategoryRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update category: ${update.id}", e, "CategoryRepository")
            false
        }
    }

    override suspend fun updatePartial(updates: List<CategoryUpdate>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                updates.forEach { update ->
                    partialUpdate(update)
                }
            }
            IReaderLog.debug("Successfully updated ${updates.size} categories", "CategoryRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update ${updates.size} categories", e, "CategoryRepository")
            false
        }
    }

    override suspend fun delete(categoryId: Long): Boolean {
        return try {
            handler.await(inTransaction = true) {
                // Remove book-category relationships first
                bookcategoryQueries.delete(categoryId)
                
                // Then delete the category
                categoryQueries.delete(categoryId)
            }
            IReaderLog.debug("Successfully deleted category: $categoryId", "CategoryRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to delete category: $categoryId", e, "CategoryRepository")
            false
        }
    }

    override suspend fun updateAllFlags(flags: Long?): Boolean {
        return try {
            handler.await { 
                categoryQueries.updateAllFlags(flags) 
            }
            IReaderLog.debug("Successfully updated all category flags", "CategoryRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update all category flags", e, "CategoryRepository")
            false
        }
    }

    override suspend fun reorderCategories(categories: List<Category>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                categories.forEachIndexed { index, category ->
                    categoryQueries.update(
                        categoryId = category.id,
                        name = category.name,
                        order = index.toLong(),
                        flags = category.flags
                    )
                }
            }
            IReaderLog.debug("Successfully reordered ${categories.size} categories", "CategoryRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to reorder ${categories.size} categories", e, "CategoryRepository")
            false
        }
    }

    private suspend fun partialUpdate(update: CategoryUpdate) {
        handler.await {
            categoryQueries.update(
                categoryId = update.id,
                name = update.name,
                order = update.order,
                flags = update.flags
            )
        }
    }
}