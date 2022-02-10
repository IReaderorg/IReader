package org.ireader.domain.book.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book

interface BookRepository {

    fun subscribe(bookId: Long): Flow<Book?>

    fun subscribe(key: String, sourceId: Long): Flow<Book?>

    suspend fun findFavorites(): List<Book>

    suspend fun find(bookId: Long): Book?

    suspend fun find(key: String, sourceId: Long): Book?

    suspend fun insert(book: Book): Long

    suspend fun updatePartial(update: Book)

    suspend fun deleteNonFavorite()

}
