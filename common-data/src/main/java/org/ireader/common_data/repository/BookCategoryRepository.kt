package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.BookCategory

interface BookCategoryRepository {
    fun subscribeAll(): Flow<List<BookCategory>>

    suspend fun findAll(): List<BookCategory>

    suspend fun insert(category: BookCategory): Long
    suspend fun insertAll(category: List<BookCategory>): List<Long>
    suspend fun delete(category: BookCategory)
    suspend fun deleteAll(category: List<BookCategory>)

}