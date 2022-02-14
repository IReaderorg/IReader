package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book

@Dao
interface LibraryBookDao {

    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  library.*,page_key_table.*,COUNT(chapter.id) AS unread
        FROM  library,page_key_table
        LEFT JOIN chapter ON library.id = chapter.bookId  AND chapter.read = 1
        GROUP BY library.id
        HAVING library.inLibrary = 1 AND (CASE WHEN :unread = 1 THEN unread = 0 ELSE 1 END)
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN library.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  library.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN library.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  library.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN library.lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  library.lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalDownload = 1 THEN  COUNT(library.id = chapter.bookId) END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalDownload = 1 THEN  COUNT(library.id = chapter.bookId) END DESC,
        CASE WHEN :isAsc = 1 AND :unread = 1 THEN  SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) = 0 END ASC,
        CASE WHEN :isAsc = 0 AND :unread = 1 THEN  SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) = 0  END DESC
""")
    fun getAllLocalBooksForPagingSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByTotalDownload: Boolean = false,
        unread: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>

    @Query("SELECT * FROM library WHERE inLibrary = 1")
    fun getAllInLibraryBooks(): Flow<List<Book>>

    @Query("SELECT * FROM library WHERE inLibrary = 1")
    suspend fun getAllInLibraryBooksForPaging(): List<Book>

    @Query("SELECT * FROM library WHERE id = :bookId")
    fun getLocalBook(bookId: Long): Flow<List<Book>?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    fun getBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM library WHERE title LIKE '%' || :query || '%' AND inLibrary = 1")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)


    @Query("DELETE FROM library WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Long)

    @Query("DELETE FROM library")
    suspend fun deleteAllBook()

}

