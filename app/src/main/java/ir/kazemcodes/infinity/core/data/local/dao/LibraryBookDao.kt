package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.domain.models.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryBookDao {

    @Query("SELECT * FROM explore_books_table")
    fun getAllExploreBookByPaging(): PagingSource<Int, ExploreBook>

    @Query("SELECT * FROM explore_books_table")
    suspend fun getAllExploreBook(): List<ExploreBook?>

    @Query("SELECT * FROM explore_books_table WHERE id =:id")
    fun getExploreBookById(id: String): Flow<ExploreBook?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllExploredBook(bookEntity: List<ExploreBook?>)

    @Query("DELETE FROM explore_books_table")
    fun deleteAllExploredBook()


    @Query("""SELECT * FROM book_table WHERE inLibrary = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN download END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  download END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalChapter = 1 THEN totalChapters END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalChapter = 1 THEN  totalChapters END DESC
""")
    fun getAllLocalBooksForPagingSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByDownloads: Boolean = false,
        sortByTotalChapter: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>

    @Query("""SELECT * FROM book_table WHERE inLibrary = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN download END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  download END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalChapter = 1 THEN totalChapters END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalChapter = 1 THEN  totalChapters END DESC
""")
    suspend fun getAllLocalBookSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByDownloads: Boolean = false,
        sortByTotalChapter: Boolean = false,
        isAsc: Boolean = false,
    ): List<Book>



    @Query("""SELECT * FROM book_table WHERE inLibrary = 1 AND unread = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN download END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  download END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalChapter = 1 THEN totalChapters END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalChapter = 1 THEN  totalChapters END DESC
""")
    fun getAllLocalBooksForPagingSortedBySortAndFilter(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByDownloads: Boolean = false,
        sortByTotalChapter: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>


    @Query("SELECT * FROM book_table")
    fun getAllBooks(): Flow<List<Book>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    fun getLocalBook(bookId: String): Flow<List<Book>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId Limit 1")
    fun getBookById(bookId: String): Flow<Book?>

    @Query("SELECT * FROM book_table WHERE bookName = :bookName AND source = :sourceName Limit 1")
    fun getBookByName(bookName: String,sourceName:String): Flow<Book?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Query("SELECT * FROM book_table WHERE bookName LIKE'%' || :query || '%'")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Query("DELETE FROM book_table WHERE id = :bookId ")
    suspend fun deleteBook(bookId: String)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()

    @Update(entity = Book::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBook(inLibraryUpdate: InLibraryUpdate)


}

data class InLibraryUpdate(
    val id: String,
    val inLibrary: Boolean,
    val totalChapters: Int,
    val lastRead: Long,
    val unread: Boolean,
)
