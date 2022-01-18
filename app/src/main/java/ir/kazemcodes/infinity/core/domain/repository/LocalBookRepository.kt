package ir.kazemcodes.infinity.core.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.ChapterEntity
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface LocalBookRepository {
    fun getBooks(): Flow<PagingData<BookEntity>>
    fun getAllInLibraryForPagingBooks(): PagingSource<Int, BookEntity>
    fun getAllBooks(): Flow<Resource<List<Book>>>
    fun getBooksById(id: String): Flow<Resource<Book>>

    fun getLocalBookByName(bookName: String): Flow<Resource<Book?>>
    fun searchInLibraryScreenBooks(query: String): Flow<PagingData<BookEntity>>
    fun searchBooksByPaging(query: String): PagingSource<Int, BookEntity>
    fun deleteChapters()
    suspend fun insertBook(book: Book)
    suspend fun deleteBook(id: String)
    suspend fun deleteAllBook()




    fun getAllExploreBook(): PagingSource<Int, ExploreBook>

    fun getExploreBookById(id: String): Flow<Resource<Book>>

    suspend fun insertAllExploreBook(bookEntity: List<ExploreBook>)

    suspend fun deleteAllExploreBook()

    fun getBookById(bookId: String): Flow<Resource<Book>>



    /***************DETAIL SCREEN*************/
    suspend fun updateLocalBook(book: Book)


}

interface LocalChapterRepository {

    fun getAllChapter(): Flow<Resource<List<Chapter>>>

    fun getChapterByChapter(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<ChapterEntity?>

    suspend fun insertChapters(chapters: List<Chapter>, book : Book, inLibrary : Boolean, source : Source)

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

    suspend fun updateChapter(chapterEntity: ChapterEntity)

    suspend fun updateChapters(chapters: List<Chapter>)

    suspend fun updateAddToLibraryChapters(
        chapterTitle: String,
        source: String,
        bookName: String,
    )

    fun getChapterByName(bookName: String, source: String): Flow<Resource<List<Chapter>>>


    suspend fun deleteChapters(bookName: String, source: String)

    suspend fun deleteNotInLibraryChapters()

    suspend fun deleteAllChapters()










}