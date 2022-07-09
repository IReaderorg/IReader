package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithCount

interface CategoryRepository {

    fun subscribeAll(): Flow<List<CategoryWithCount>>

    suspend fun findAll(): List<CategoryWithCount>
    suspend fun find(categoryId:Long): Category
    suspend fun findCategoriesOfBook(bookId:Long): List<Category>
    suspend fun updateAllFlags(flags:Long)

    suspend fun insertOrUpdate(category: Category): Long
    suspend fun insertOrUpdate(category: List<Category>): List<Long>
    suspend fun delete(category: Category)
    suspend fun delete(category: List<Category>)
    suspend fun deleteAll()


}