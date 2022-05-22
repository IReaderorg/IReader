package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category

interface CategoryRepository {

    fun subscribeAll(): Flow<List<Category>>

    suspend fun findAll(): List<Category>

    suspend fun insert(category: Category): Long
    suspend fun insertAll(category: List<Category>): List<Long>
    suspend fun delete(category: Category)
    suspend fun deleteAll(category: List<Category>)


}