package ireader.common.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Category
import ireader.common.models.entities.CategoryWithCount

interface CategoryRepository {

    fun subscribeAll(): Flow<List<CategoryWithCount>>

    suspend fun findAll(): List<CategoryWithCount>
    suspend fun find(categoryId: Long): Category
    suspend fun findCategoriesOfBook(bookId: Long): List<Category>
    suspend fun updateAllFlags(flags: Long)

    suspend fun insertOrUpdate(category: Category): Long
    suspend fun insertOrUpdate(category: List<Category>): List<Long>
    suspend fun delete(category: Category)
    suspend fun delete(category: List<Category>)
    suspend fun deleteAll()
}
