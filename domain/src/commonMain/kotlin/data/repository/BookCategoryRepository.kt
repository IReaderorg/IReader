package ireader.domain.data.repository

import ireader.domain.models.entities.BookCategory
import kotlinx.coroutines.flow.Flow

interface BookCategoryRepository {
    fun subscribeAll(): Flow<List<BookCategory>>

    suspend fun findAll(): List<BookCategory>

    suspend fun insert(category: BookCategory)
    suspend fun insertAll(categories: List<BookCategory>)
    suspend fun delete(category: List<BookCategory>)
    suspend fun delete(bookId: Long)
    suspend fun deleteAll(category: List<BookCategory>)
}
