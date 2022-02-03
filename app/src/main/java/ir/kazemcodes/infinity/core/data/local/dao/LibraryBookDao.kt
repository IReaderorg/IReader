package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.core.domain.models.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryBookDao {




    @Query("""SELECT * FROM book_table WHERE inLibrary = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN isDownloaded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  isDownloaded END DESC,
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
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN isDownloaded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  isDownloaded END DESC,
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
    ): List<Book>?



    @Query("""SELECT * FROM book_table WHERE inLibrary = 1 AND unread = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN isDownloaded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  isDownloaded END DESC,
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


    @Query("""SELECT * FROM book_table WHERE isDownloaded = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN bookName END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  bookName END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDownloads = 1 THEN isDownloaded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDownloads = 1 THEN  isDownloaded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalChapter = 1 THEN totalChapters END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalChapter = 1 THEN  totalChapters END DESC
""")
    fun getAllInDownloads(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByDownloads: Boolean = false,
        sortByTotalChapter: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>


    @Query("SELECT * FROM book_table")
    fun getAllInLibraryBooks(): Flow<List<Book>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    fun getLocalBook(bookId: Int): Flow<List<Book>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId Limit 1")
    fun getBookById(bookId: Int): Flow<Book?>

    @Query("SELECT * FROM book_table WHERE bookName LIKE '%' || :query || '%' AND inLibrary = 1")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)


    @Query("DELETE FROM book_table WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Int)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()

}

data class InLibraryUpdate(
    val id: Int,
    val inLibrary: Boolean,
    val totalChapters: Int,
    val lastRead: Long,
    val unread: Boolean,
)
