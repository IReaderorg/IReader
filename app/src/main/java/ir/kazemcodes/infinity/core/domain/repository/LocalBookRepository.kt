package ir.kazemcodes.infinity.core.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.flow.Flow

interface LocalBookRepository {

    fun getBookById(id: Int) : Flow<Resource<Book>>

    fun getLocalBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: FilterType,
    ): Flow<PagingData<Book>>

    fun getAllInLibraryForPagingBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book>

    fun getAllLocalBooks(): Flow<Resource<List<Book>>>
    fun getLocalBooksById(id: Int): Flow<Resource<Book>>

    fun getLocalBookByName(bookName: String,sourceName:String): Flow<Resource<Book?>>
    fun searchInLibraryScreenBooks(query: String): Flow<PagingData<Book>>
    fun searchBooksByPaging(query: String): PagingSource<Int, Book>
    fun deleteChapters()
    suspend fun insertBook(book: Book)
    suspend fun deleteAllBook()


    fun getAllExploreBook(): PagingSource<Int, Book>

    fun getExploreBookById(id: Int): Flow<Resource<Book>>

    suspend fun insertAllExploreBook(bookEntity: List<Book>)

    suspend fun deleteAllExploreBook()



    /***************DETAIL SCREEN*************/
    suspend fun updateLocalBook(book: Book)


    suspend fun deleteBook(id: Int)
}

interface LocalChapterRepository {

    fun getAllChapter(): Flow<Resource<List<Chapter>>>

    fun getChapterByName(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<Resource<Chapter>>

    suspend fun insertChapters(
        chapters: List<Chapter>,
        book: Book,
        inLibrary: Boolean,
        source: Source,
    )

    suspend fun deleteLastReadChapter(
        bookName: String,
        source: String,
    )

    suspend fun setLastReadChapter(
        bookName: String,
        chapterTitle: String,
        source: String,
    )

    fun getLastReadChapter(bookName: String, source: String): Flow<Resource<Chapter>>

    suspend fun updateChapter(
        readingContent: String,
        haveBeenRead: Boolean,
        bookName: String,
        chapterTitle: String,
        lastRead: Boolean,
        source: String,
    )

    suspend fun updateChapter(chapter: Chapter)

    suspend fun updateChapters(chapters: List<Chapter>)

    suspend fun updateAddToLibraryChapters(
        chapterTitle: String,
        source: String,
        bookName: String,
    )

    fun getChapterByName(bookName: String, source: String): Flow<Resource<List<Chapter>>>


    fun getLocalChaptersByPaging(
        bookName: String, source: String,isAsc: Boolean
    ): Flow<PagingData<Chapter>>

    fun getLocalChapterByPaging(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<PagingData<Chapter>>

    suspend fun deleteChapters(bookName: String, source: String)

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()


}