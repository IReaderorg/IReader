package ir.kazemcodes.infinity.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import ir.kazemcodes.infinity.core.domain.models.Chapter
import kotlinx.coroutines.flow.Flow


@Dao
interface LibraryChapterDao {

    @Query("SELECT * FROM chapter_table WHERE chapterId = :chapterId Limit 1")
    fun getChapterById(
        chapterId:Int
    ): Flow<Chapter?>


    @Query("""SELECT * FROM chapter_table WHERE bookId= :bookId ORDER BY
        CASE WHEN :isAsc = 1 THEN chapterId END ASC,
        CASE WHEN :isAsc = 0 THEN  chapterId END DESC
    """)
    fun getChaptersByBookId(bookId: Int, isAsc:Boolean): Flow<List<Chapter>?>

    @Query("""SELECT * FROM chapter_table WHERE bookId = :bookId ORDER BY 
        CASE WHEN :isAsc = 1 THEN chapterId END ASC,
        CASE WHEN :isAsc = 0 THEN  chapterId END DESC""")
    fun getChaptersForPaging(bookId: Int, isAsc:Boolean): PagingSource<Int,Chapter>




    @Query("SELECT * FROM chapter_table WHERE chapterId = :chapterId AND bookId = :bookId Limit 1")
    fun getOneChapterForPaging(
        chapterId: Int,
        bookId: Int,
    ): PagingSource<Int,Chapter>


    @Query("SELECT * from chapter_table WHERE bookId = :bookId AND lastRead = 1 LIMIT 1")
    fun getLastReadChapter(bookId: Int): Flow<Chapter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)




    @Query("DELETE FROM chapter_table WHERE bookId = :bookId")
    suspend fun deleteChaptersById(bookId: Int)

    @Delete
    suspend fun deleteChaptersById(chapters: List<Chapter>)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM chapter_table ")
    suspend fun deleteAllChapters()

    @Query("DELETE FROM chapter_table WHERE inLibrary = 0")
    suspend fun deleteNotInLibraryChapters()
}
