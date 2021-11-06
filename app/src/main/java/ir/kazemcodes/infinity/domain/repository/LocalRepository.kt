package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.domain.model.book.BookEntity
import kotlinx.coroutines.flow.Flow

interface LocalRepository {

    fun getBooks() : Flow<List<BookEntity>>

    suspend fun getBookById(id : Int): BookEntity?

    suspend fun insertBook(bookEntity : BookEntity)

    suspend fun deleteBook(bookEntity: BookEntity)

    suspend fun deleteAllBook()


}