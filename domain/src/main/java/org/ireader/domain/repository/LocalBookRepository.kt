package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book

interface LocalBookRepository {


    /** Local GetUseCase**/

    fun getBookById(id: Long): Flow<Book?>

    fun getAllInLibraryBooks(): Flow<List<Book>>

    fun getBooksByQueryByPagingSource(query: String): PagingSource<Int, Book>

    fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book>


    fun getAllInLibraryPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book>



    fun getAllExploreBookPagingSource(): PagingSource<Int, Book>


    /****************************************************/

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllExploreBook()


    suspend fun deleteBookById(id: Long)

    /**
     * Remove the Unused ExploreMode Books from database.
     */
    suspend fun setExploreModeOffForInLibraryBooks()

    suspend fun deleteAllBooks()


    /****************************************************/

    suspend fun insertBook(book: Book)
    suspend fun insertBooks(book: List<Book>)
    /**************************************************/


}

