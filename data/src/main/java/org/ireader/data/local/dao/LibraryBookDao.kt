package org.ireader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.BookItem
import org.ireader.domain.models.entities.Chapter

@Dao
interface LibraryBookDao : BaseDao<Book> {

    @Query("SELECT * FROM library")
    suspend fun findAllBooks(): List<Book>

    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  * FROM library WHERE favorite = 1 """)
    fun subscribeAllLocalBooks(): Flow<List<Book>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""SELECT  library.* ,
        MAX(chapter.readAt) as lastRead,
        COUNT(DISTINCT chapter.id) AS totalChapters,
        SUM(chapter.read) as isRead,
        SUM(length(chapter.content) > 10) AS totalDownload
        FROM  library
        LEFT JOIN chapter ON library.id = chapter.bookId
        GROUP BY library.id
        HAVING library.favorite = 1
        ORDER BY 
        CASE WHEN :desc = 1 AND :sortByAbs = 1 THEN library.title END DESC,
        CASE WHEN :desc = 0 AND :sortByAbs = 1 THEN  library.title END ASC,
        CASE WHEN :desc = 1 AND :sortByDateAdded = 1 THEN library.dataAdded END DESC,
        CASE WHEN :desc = 0 AND :sortByDateAdded = 1 THEN  library.dataAdded END ASC,
        CASE WHEN :desc = 1 AND :sortByLastRead = 1 THEN lastRead END DESC,
        CASE WHEN :desc = 0 AND :sortByLastRead = 1 THEN  lastRead END ASC,
        CASE WHEN :desc = 1 AND :dateFetched = 1 THEN dateFetch END DESC,
        CASE WHEN :desc = 0 AND :dateFetched = 1 THEN  dateFetch END ASC,
        CASE WHEN :desc = 1 AND :dateAdded = 1 THEN dataAdded END DESC,
        CASE WHEN :desc = 0 AND :dateAdded = 1 THEN  dataAdded END ASC,
        CASE WHEN :desc = 1 AND :sortByTotalChapter = 1 THEN  totalChapters END DESC,
        CASE WHEN :desc = 0 AND :sortByTotalChapter = 1 THEN  totalChapters END ASC,
        CASE WHEN :desc = 1 AND :lastChecked = 1 THEN  lastUpdated END DESC,
        CASE WHEN :desc = 0 AND :lastChecked = 1 THEN  lastUpdated END ASC
        """)
    fun subscribeAllInLibraryBooks(
        sortByAbs: Boolean = false,
        sortByDateAdded: Boolean = false,
        sortByLastRead: Boolean = false,
        sortByTotalChapter: Boolean = false,
        dateFetched: Boolean = false,
        dateAdded: Boolean = false,
        lastChecked: Boolean = false,
        desc: Boolean = false,
    ): Flow<List<BookItem>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*,
    MAX(history.readAt) AS max,
    SUM(length(chapter.content) > 10) AS totalDownload
    FROM library
    LEFT JOIN chapter
    ON library.id = chapter.bookId
    LEFT JOIN history
    ON chapter.id = history.chapterId
    WHERE library.favorite = 1
    GROUP BY library.id
    ORDER BY
    CASE WHEN :desc = 1 THEN  max END DESC,
    CASE WHEN :desc = 0 THEN  max END ASC
""")
    fun subscribeLatestRead(desc: Boolean): Flow<List<BookItem>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*, MAX(chapter.dateUpload) AS max,
    SUM(length(chapter.content) > 10) AS totalDownload
    FROM library
    LEFT JOIN chapter
    ON library.id = chapter.bookId
    GROUP BY library.id
    ORDER by
    CASE WHEN :desc = 1 THEN  max END DESC,
    CASE WHEN :desc = 0 THEN  max END ASC
""")
    fun subscribeLatestChapter(desc: Boolean): Flow<List<BookItem>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*, SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread,
    SUM(length(chapter.content) > 10) AS totalDownload
    FROM library
    JOIN chapter
    ON library.id = chapter.bookId
    GROUP BY library.id
    HAVING library.favorite = 1
    ORDER by 
    CASE WHEN :desc = 1 THEN  COUNT(*) END DESC,
    CASE WHEN :desc = 0 THEN  COUNT(*) END ASC
""")
    fun subscribeTotalChapter(desc: Boolean): Flow<List<BookItem>>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*, 
    SUM(length(chapter.content) > 10) AS totalDownload,
    SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread,
    COUNT(*) AS total
    FROM library
    LEFT JOIN chapter
    ON library.id = chapter.bookId
    GROUP BY library.id
    HAVING library.favorite = 1 AND unread == total
""")
    suspend fun findUnreadBooks(): List<BookItem>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*, 
    SUM(length(chapter.content) > 10) as total_download,
    SUM(length(chapter.content) > 10) AS totalDownload,
    COUNT(*) AS total
    FROM library
    LEFT JOIN chapter
    ON library.id = chapter.bookId
    GROUP BY library.id
    HAVING library.favorite = 1 AND total_download == total
""")
    suspend fun findCompletedBooks(): List<BookItem>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
    SELECT library.*, 
    SUM(length(chapter.content) > 10) AS totalDownload,
    SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread,
    COUNT(*) AS total
    FROM library
    LEFT JOIN chapter
    ON library.id = chapter.bookId
    GROUP BY library.id
    HAVING library.favorite = 1 AND unread == 0
""")
    suspend fun findDownloadedBooks(): List<BookItem>


    @Query("SELECT * FROM library WHERE favorite = 1")
    suspend fun findAllInLibraryBooks(): List<Book>

    @Query("SELECT * FROM library WHERE id = :bookId")
    fun subscribeLocalBook(bookId: Long): Flow<List<Book>?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    fun subscribeBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    suspend fun findBookById(bookId: Long): Book?

    @Query("SELECT * FROM library WHERE id in (:bookIds)")
    suspend fun findBookByIds(bookIds: List<Long>): List<Book>

    @Query("SELECT * FROM library WHERE link = :key Limit 1")
    suspend fun findBookByKey(key: String): Book?

    @Query("SELECT * FROM library WHERE link = :key")
    suspend fun findBooksByKey(key: String): List<Book>

    @Query("SELECT * FROM library WHERE link = :key or title = :title")
    fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>

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

    @Transaction
    suspend fun deleteBookAndChapterByBookIds(bookIds: List<Long>) {
        deleteChaptersByBookIds(bookIds)
        deleteBooksByIds(bookIds)
        deleteAllUpdates(bookIds)
    }

    @Query("Delete FROM updates WHERE bookId in (:bookIds)")
    suspend fun deleteAllUpdates(bookIds: List<Long>)

    @Transaction
    suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>) {
        insert(books)
        insertChapters(chapters)
    }

    @Insert(entity = Chapter::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertChapters(chapters: List<Chapter>)

    @Query("""
        DELETE FROM library
        WHERE id in (:bookIds)
    """)
    suspend fun deleteBooksByIds(bookIds: List<Long>)

    @Query("DELETE FROM chapter WHERE bookId in (:bookIds)")
    suspend fun deleteChaptersByBookIds(bookIds: List<Long>)


}

