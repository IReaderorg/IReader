package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.library.LibrarySort

interface LocalBookRepository {

    /***************************************************/

    /** Local GetUseCase**/
    suspend fun findAllBooks(): List<Book>
    fun subscribeBookById(id: Long): Flow<Book?>
    suspend fun findBookById(id: Long): Book?


    suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean = false,
        unreadFilter: Boolean = false,
    ): List<Book>

    fun getBooksByQueryPagingSource(query: String): Flow<Book>

    fun subscribeAllInLibrary(
        sortByAbs: Boolean,
        sortByDateAdded: Boolean,
        sortByLastRead: Boolean,
        dateFetched: Boolean,
        sortByTotalChapters: Boolean,
        dateAdded: Boolean,
        latestChapter: Boolean,
        lastChecked: Boolean,
        desc: Boolean,
    ): Flow<List<BookItem>>

    suspend fun findUnreadBooks(): List<BookItem>

    suspend fun findCompletedBooks(): List<BookItem>

    suspend fun findDownloadedBooks(): List<BookItem>

    suspend fun findBookByKey(key: String): Book?

    suspend fun findBooksByKey(key: String): List<Book>

    suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>

    /****************************************************/

    suspend fun deleteAllExploreBook()

    suspend fun deleteBooks(book: List<Book>)

    suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)

    suspend fun deleteBookById(id: Long)

    suspend fun deleteAllBooks()

    /****************************************************/

    suspend fun insertBook(book: Book): Long
    suspend fun insertBooks(book: List<Book>): List<Long>

    /**************************************************/

    suspend fun findFavoriteSourceIds(): List<Long>
}
