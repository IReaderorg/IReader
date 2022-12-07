package ireader.domain.data.repository

import ireader.common.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    suspend fun findAllBooks(): List<Book>

    fun subscribeBookById(id: Long): Flow<Book?>

    suspend fun findBookById(id: Long): Book?

    suspend fun find(key: String, sourceId: Long): Book?

    suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean = false,
        unreadFilter: Boolean = false,
    ): List<Book>

    suspend fun findBookByKey(key: String): Book?

    suspend fun findBooksByKey(key: String): List<Book>

    suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>

    suspend fun deleteBooks(book: List<Book>)

    suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)

    suspend fun deleteBookById(id: Long)
    suspend fun findDuplicateBook(title: String,sourceId: Long) : Book?

    suspend fun deleteAllBooks()
    suspend fun deleteNotInLibraryBooks()



    suspend fun updateBook(book: Book)
    suspend fun updateBook(book: LibraryBook, favorite: Boolean)
    suspend fun updateBook(book: List<Book>)
    suspend fun upsert(book: Book): Long
    suspend fun updatePartial(book: Book): Long
    suspend fun insertBooks(book: List<Book>): List<Long>
    suspend fun delete(key: String)


    suspend fun findFavoriteSourceIds(): List<Long>
}
