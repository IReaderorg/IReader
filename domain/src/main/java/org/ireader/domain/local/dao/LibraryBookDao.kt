package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book

@Dao
interface LibraryBookDao {

    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  *
        FROM  library
        WHERE library.favorite = 1 AND NOT (CASE WHEN :unread= 1 THEN library.lastRead != 0 ELSE 0 END)
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN library.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  library.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN library.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  library.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN library.lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  library.lastRead END DESC
""")
    fun getAllLocalBooksForPagingSortedBySort(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        unread: Boolean = false,
        isAsc: Boolean = false,
    ): PagingSource<Int, Book>

    @Query("""SELECT  *
        FROM  library
        WHERE library.favorite = 1 AND NOT (CASE WHEN :unread= 1 THEN library.lastRead != 0 ELSE 0 END)
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN library.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  library.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN library.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  library.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN library.lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  library.lastRead END DESC""")
    fun getAllInLibraryBooks(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        unread: Boolean = false,
        isAsc: Boolean = false,
    ): Flow<List<Book>>


    @Query("""SELECT  library.*,COUNT(DISTINCT chapter.id) AS total_chapter
        FROM  library,page_key_table
        LEFT JOIN chapter ON library.id = chapter.bookId  
        GROUP BY library.id
        HAVING library.favorite = 1 AND NOT (CASE WHEN :unread= 1 THEN SUM(chapter.read != 0) ELSE 0 END)
        ORDER BY
        CASE WHEN :isAsc = 1 THEN  total_chapter END ASC,
        CASE WHEN :isAsc = 0 THEN  total_chapter END DESC""")
    fun findLibraryBooksByTotalDownload(
        isAsc: Boolean = false,
        unread: Boolean = false,
    ): Flow<List<Book>>

    @Query("SELECT * FROM library WHERE favorite = 1")
    suspend fun getAllInLibraryBooksForPaging(): List<Book>

    @Query("SELECT * FROM library WHERE id = :bookId")
    fun getLocalBook(bookId: Long): Flow<List<Book>?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    fun getBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM library WHERE title LIKE '%' || :query || '%' AND favorite = 1")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)


    @Query("DELETE FROM library WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Long)

    @Query("DELETE FROM library")
    suspend fun deleteAllBook()

    @Query("DELETE FROM library WHERE favorite = 0")
    suspend fun deleteNotInLibraryBooks()

}

