package org.ireader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter


@Dao
interface LibraryChapterDao {

    @Query("SELECT * FROM chapter WHERE id = :chapterId Limit 1")
    fun subscribeChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    @Query("SELECT * FROM chapter WHERE id = :chapterId Limit 1")
    suspend fun findChapterById(
        chapterId: Long,
    ): Chapter?

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *,library.favorite
FROM chapter 
 JOIN library ON library.id = chapter.bookId 
WHERE library.favorite = 1
    """)
    suspend fun findAllInLibraryChapters(): List<Chapter>


    @Query("""SELECT * FROM chapter WHERE bookId= :bookId ORDER BY
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC
    """)
    fun subscribeChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>>

    @Query("""SELECT * FROM chapter WHERE bookId= :bookId ORDER BY
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC
    """)
    suspend fun findChaptersByBookId(bookId: Long, isAsc: Boolean): List<Chapter>

    @Query("""SELECT * FROM chapter WHERE link= :key
    """)
    suspend fun findChaptersByKey(key: String): List<Chapter>

    @Query("""SELECT * FROM chapter WHERE link= :key LIMIT 1
    """)
    suspend fun findChapterByKey(key: String): Chapter?

    @Query("""SELECT * FROM chapter WHERE bookId = :bookId AND title LIKE '%' || :query || '%' ORDER BY 
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC""")
    fun getChaptersForPaging(
        bookId: Long,
        isAsc: Boolean,
        query: String,
    ): PagingSource<Int, Chapter>


    @Query("SELECT * FROM chapter WHERE id = :chapterId AND bookId = :bookId Limit 1")
    fun getOneChapterForPaging(
        chapterId: Int,
        bookId: Int,
    ): PagingSource<Int, Chapter>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *
        from chapter
        GROUP BY id
        HAVING chapter.bookId == :bookId
        ORDER BY readAt DESC
        LIMIT 1
    """)
    fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?>

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *
        from chapter
        GROUP BY id
        HAVING chapter.bookId == :bookId
        ORDER BY readAt DESC
        LIMIT 1
    """)
    suspend fun findLastReadChapter(bookId: Long): Chapter?

    @Query("SELECT * from chapter WHERE bookId = :bookId LIMIT 1")
    fun subscribeFirstChapter(bookId: Long): Flow<Chapter?>

    @Query("SELECT * from chapter WHERE bookId = :bookId LIMIT 1")
    suspend fun findFirstChapter(bookId: Long): Chapter?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long


    @Query("DELETE FROM chapter WHERE bookId = :bookId")
    suspend fun deleteChaptersById(bookId: Long)


    @Delete
    suspend fun deleteChaptersById(chapters: List<Chapter>)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM chapter ")
    suspend fun deleteAllChapters()

    @Query("""
        DELETE FROM chapter
        WHERE bookId IN (
        SELECT chapter.ROWID FROM chapter a
        INNER JOIN library b
         ON (a.bookId=b.id)
         WHERE b.favorite = 1
        )
    """)
    suspend fun deleteNotInLibraryChapters()
}
