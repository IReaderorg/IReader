package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book

interface LocalBookRepository {


    /** Local GetUseCase**/

    fun subscribeBookById(id: Long): Flow<Book?>
    suspend fun findBookById(id: Long): Book?



    suspend fun findAllInLibraryBooks(
        sortType: SortType = SortType.LastRead,
        isAsc: Boolean = false,
        unreadFilter: Boolean = false,
    ): List<Book>

    fun getBooksByQueryByPagingSource(query: String): PagingSource<Int, Book>

    fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book>

    
    fun subscribeAllInLibrary(
        sortByAbs: Boolean,
        sortByDateAdded: Boolean,
        sortByLastRead: Boolean,
        dateFetched: Boolean,
        sortByTotalChapters: Boolean,
        dateAdded: Boolean,
        latestChapter: Boolean,
        unread: Boolean,
        downloaded: Boolean,
        complete: Boolean,
        isAsc: Boolean,
    ): Flow<List<Book>>


    fun getAllExploreBookPagingSource(): PagingSource<Int, Book>

    suspend fun findBookByKey(key: String): Book?

    suspend fun findBooksByKey(key: String): List<Book>

    /****************************************************/

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllExploreBook()


    suspend fun deleteBookById(id: Long)


    suspend fun deleteAllBooks()


    /****************************************************/

    suspend fun insertBook(book: Book): Long
    suspend fun insertBooks(book: List<Book>): List<Long>

    /**************************************************/

    suspend fun findFavoriteSourceIds(): List<Long>

    suspend fun deleteAllExploredBook()

    suspend fun convertExploredTOLibraryBooks()

}

