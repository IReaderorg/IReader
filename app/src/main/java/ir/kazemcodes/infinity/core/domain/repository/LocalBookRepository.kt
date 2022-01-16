package ir.kazemcodes.infinity.core.domain.repository

import ir.kazemcodes.infinity.core.domain.models.BookEntity
import kotlinx.coroutines.flow.Flow

interface LocalBookRepository {

    fun getAllBooks(): Flow<List<BookEntity>>

    fun getInLibraryBooks(): Flow<List<BookEntity>>

    fun getBookById(bookId: Int): Flow<BookEntity?>

    fun getBookByName(bookName: String): Flow<BookEntity?>


    suspend fun insertBook(bookEntity: BookEntity)

    suspend fun updateBook(bookEntity: BookEntity)

    suspend fun insertBooks(bookEntities: List<BookEntity>)

    suspend fun deleteBook(bookName: String)

    suspend fun deleteAllBook()

    suspend fun deleteNotInLibraryBooks()


}