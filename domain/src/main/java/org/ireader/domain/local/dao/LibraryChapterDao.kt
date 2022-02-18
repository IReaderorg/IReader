package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter


@Dao
interface LibraryChapterDao {

    @Query("SELECT * FROM chapter WHERE id = :chapterId Limit 1")
    fun findChapterById(
        chapterId: Long,
    ): Flow<Chapter?>


    @Query("""SELECT * FROM chapter WHERE bookId= :bookId ORDER BY
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC
    """)
    fun findChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>>

    @Query("""SELECT * FROM chapter WHERE bookId = :bookId ORDER BY 
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC""")
    fun getChaptersForPaging(bookId: Long, isAsc: Boolean): PagingSource<Int, Chapter>


    @Query("SELECT * FROM chapter WHERE id = :chapterId AND bookId = :bookId Limit 1")
    fun getOneChapterForPaging(
        chapterId: Int,
        bookId: Int,
    ): PagingSource<Int, Chapter>


    @Query("SELECT * from chapter WHERE bookId = :bookId AND lastRead = 1 LIMIT 1")
    fun findLastReadChapter(bookId: Long): Flow<Chapter?>

    @Query("SELECT * from chapter WHERE bookId = :bookId LIMIT 1")
    fun findFirstChapter(bookId: Long): Flow<Chapter?>

    @Query("UPDATE chapter SET lastRead = 0 WHERE bookId = :bookId")
    suspend fun setLastReadToFalse(bookId: Long)

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

    @Query("DELETE FROM chapter WHERE inLibrary = 0")
    suspend fun deleteNotInLibraryChapters()
}
