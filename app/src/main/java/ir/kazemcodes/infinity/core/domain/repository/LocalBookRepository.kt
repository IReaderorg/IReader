package ir.kazemcodes.infinity.core.domain.repository

import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.flow.Flow

interface LocalBookRepository {


    /** Local GetUseCase**/

    fun getBookById(id: Int): Flow<Resource<Book>>

    fun getAllInLibraryBooks(): Flow<List<Book>?>

    fun getBooksByQueryByPagingSource(query: String): PagingSource<Int, Book>

    fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book>


    fun getAllInLibraryPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book>

    fun getAllInDownloadPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book>


    fun getAllExploreBookPagingSource(): PagingSource<Int, Book>



    /****************************************************/

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllExploreBook()


    suspend fun deleteBookById(id: Int)

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

