package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book

@Dao
interface LibraryBookDao {


    @Query("""SELECT * FROM  book_table LEFT JOIN chapter_table ON chapter_table.bookId = book_table.id WHERE book_table.inLibrary = 1 AND chapter_table.haveBeenRead = :unread GROUP BY book_table.id
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN book_table.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  book_table.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN book_table.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  book_table.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN book_table.lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  book_table.lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalDownload = 1 THEN  COUNT(book_table.id = chapter_table.bookId) END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalDownload = 1 THEN  COUNT(book_table.id = chapter_table.bookId) END DESC,
        CASE WHEN :isAsc = 1 AND :unread = 1 THEN  SUM(CASE WHEN chapter_table.haveBeenRead == 0 THEN 1 ELSE 0 END) = 0 END ASC,
        CASE WHEN :isAsc = 0 AND :unread = 1 THEN  SUM(CASE WHEN chapter_table.haveBeenRead == 0 THEN 1 ELSE 0 END) = 0  END DESC
""")
    fun getAllLocalBooksForPagingSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByTotalDownload: Boolean = false,
        unread: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>

    @Query("""SELECT * FROM book_table WHERE inLibrary = 1
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC
""")
    suspend fun getAllLocalBookSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        isAsc: Boolean = false,
    ): List<Book>?


    @Query("""SELECT * FROM  book_table LEFT JOIN chapter_table ON chapter_table.bookId = book_table.id WHERE chapter_table.haveBeenRead = 0 GROUP BY book_table.id
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN book_table.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  book_table.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN book_table.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  book_table.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN book_table.lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  book_table.lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalDownload = 1 THEN  COUNT(book_table.id = chapter_table.bookId) END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalDownload = 1 THEN  COUNT(book_table.id = chapter_table.bookId) END DESC,
        CASE WHEN :isAsc = 1 AND :unread = 1 THEN  SUM(CASE WHEN chapter_table.haveBeenRead == 0 THEN 1 ELSE 0 END) = 0 END ASC,
        CASE WHEN :isAsc = 0 AND :unread = 1 THEN  SUM(CASE WHEN chapter_table.haveBeenRead == 0 THEN 1 ELSE 0 END) = 0  END DESC
""")
    fun getAllLocalBooksForPagingSortedBySortAndFilter(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByTotalDownload: Boolean = false,
        unread: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>


    @Query("SELECT * FROM book_table WHERE inLibrary = 1")
    fun getAllInLibraryBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book_table WHERE inLibrary = 1")
    suspend fun getAllInLibraryBooksForPaging(): List<Book>

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    fun getLocalBook(bookId: Long): Flow<List<Book>?>

    @Query("SELECT * FROM book_table WHERE id = :bookId Limit 1")
    fun getBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM book_table WHERE title LIKE '%' || :query || '%' AND inLibrary = 1")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)


    @Query("DELETE FROM book_table WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Long)

    @Query("DELETE FROM book_table")
    suspend fun deleteAllBook()

}

