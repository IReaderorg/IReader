package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.BookCategory

interface BookCategoryRepository {
    fun subscribeAll(): Flow<List<BookCategory>>

    suspend fun findAll(): List<BookCategory>

    suspend fun insert(category: BookCategory)
    suspend fun insertAll(category: List<BookCategory>)
    suspend fun delete(category: BookCategory)
    suspend fun delete(bookId: Long)
    suspend fun deleteAll(category: List<BookCategory>)

}