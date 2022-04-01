package org.ireader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book

@Dao
interface LibraryBookDao {


    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  * FROM library WHERE favorite = 1 """)
    fun subscribeAllLocalBooks(): Flow<List<Book>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  library.*, MAX(chapter.readAt) as lastRead,COUNT(DISTINCT chapter.id) AS total_chapter, COUNT(chapter.read) as isRead, COUNT(length(chapter.content) > 10) AS total_download
        FROM  library
        LEFT JOIN chapter ON library.id = chapter.bookId
        GROUP BY library.id
        HAVING library.favorite = 1 
        AND (CASE WHEN :unread= 1 THEN lastRead is NULL ELSE 1 END)
        AND (CASE WHEN :complete= 1 THEN total_chapter == isRead ELSE 1 END)
        AND (CASE WHEN :downloaded= 1 THEN total_chapter == total_download ELSE 1 END)
        ORDER BY 
        CASE WHEN :isAsc = 1 AND :sortByAbs = 1 THEN library.title END ASC,
        CASE WHEN :isAsc = 0 AND :sortByAbs = 1 THEN  library.title END DESC,
        CASE WHEN :isAsc = 1 AND :sortByDateAdded = 1 THEN library.dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :sortByDateAdded = 1 THEN  library.dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :sortByLastRead = 1 THEN lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :sortByLastRead = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :dateFetched = 1 THEN dateFetch END ASC,
        CASE WHEN :isAsc = 0 AND :dateFetched = 1 THEN  dateFetch END DESC,
        CASE WHEN :isAsc = 1 AND :dateAdded = 1 THEN dataAdded END ASC,
        CASE WHEN :isAsc = 0 AND :dateAdded = 1 THEN  dataAdded END DESC,
        CASE WHEN :isAsc = 1 AND :latestChapter = 1 THEN  dateUpload END ASC,
        CASE WHEN :isAsc = 0 AND :latestChapter = 1 THEN  dateUpload END DESC,
        CASE WHEN :isAsc = 1 AND :unread = 1 THEN  lastRead END ASC,
        CASE WHEN :isAsc = 0 AND :unread = 1 THEN  lastRead END DESC,
        CASE WHEN :isAsc = 1 AND :sortByTotalChapter = 1 THEN  total_chapter END ASC,
        CASE WHEN :isAsc = 0 AND :sortByTotalChapter = 1 THEN  total_chapter END DESC
        """)
    fun subscribeAllInLibraryBooks(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByTotalChapter: Boolean = false,
        dateFetched: Boolean = false,
        dateAdded: Boolean = false,
        latestChapter: Boolean = false,
        unread: Boolean = false,
        downloaded: Boolean = false,
        complete: Boolean = false,
        isAsc: Boolean = false,
    ): Flow<List<Book>>


    @Query("SELECT * FROM library WHERE favorite = 1")
    suspend fun findAllInLibraryBooks(): List<Book>

    @Query("SELECT * FROM library WHERE id = :bookId")
    fun subscribeLocalBook(bookId: Long): Flow<List<Book>?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    fun subscribeBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    suspend fun findBookById(bookId: Long): Book?

    @Query("SELECT * FROM library WHERE link = :key Limit 1")
    suspend fun findBookByKey(key: String): Book?

    @Query("SELECT * FROM library WHERE link = :key")
    suspend fun findBooksByKey(key: String): List<Book>

    @Query("SELECT * FROM library WHERE title LIKE '%' || :query || '%' AND favorite = 1")
    fun searchBook(query: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long


    @Query("DELETE FROM library WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Long)

    @Query("DELETE FROM library")
    suspend fun deleteAllBook()

    @Query("DELETE FROM library WHERE favorite = 0")
    suspend fun deleteNotInLibraryBooks()

    @Query("SELECT sourceId FROM library GROUP BY sourceId ORDER BY COUNT(sourceId) DESC")
    suspend fun findFavoriteSourceIds(): List<Long>

    @Query("DELETE  FROM library WHERE favorite = 0")
    suspend fun deleteExploredBooks()

    @Query("UPDATE library SET tableId = 0 WHERE tableId != 0 AND favorite = 1")
    suspend fun convertExploredTOLibraryBooks()
}

