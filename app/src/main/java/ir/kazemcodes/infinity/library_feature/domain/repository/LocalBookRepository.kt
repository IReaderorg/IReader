package ir.kazemcodes.infinity.library_feature.domain.repository

import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import kotlinx.coroutines.flow.Flow

interface LocalBookRepository {

    fun getBooks() : Flow<List<BookEntity>>

    suspend fun getBookById(bookId : Int): BookEntity?

    suspend fun getBookByName(bookName : String): BookEntity?


    suspend fun insertBook(bookEntity : BookEntity)

    suspend fun insertBooks(bookEntities : List<BookEntity>)

    suspend fun deleteBook(bookId: Int)

    suspend fun deleteAllBook()


}