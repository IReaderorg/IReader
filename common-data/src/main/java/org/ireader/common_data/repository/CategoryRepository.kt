package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithCount

interface CategoryRepository {

    fun subscribeAll(): Flow<List<CategoryWithCount>>

    suspend fun findAll(): List<CategoryWithCount>

    suspend fun insert(category: Category): Long
    suspend fun insertAll(category: List<Category>): List<Long>
    suspend fun delete(category: Category)
    suspend fun deleteAll(category: List<Category>)


}