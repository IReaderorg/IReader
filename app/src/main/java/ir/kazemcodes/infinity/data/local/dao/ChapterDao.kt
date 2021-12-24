package ir.kazemcodes.infinity.data.local.dao

import androidx.room.*
import ir.kazemcodes.infinity.domain.models.ChapterEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapterEntities: List<ChapterEntity>)

    @Query("UPDATE chapter_entity SET haveBeenRead = :haveBeenRead, content = :readingContent, lastRead = :lastRead WHERE bookName = :bookName AND title = :chapterTitle")
    suspend fun updateChapter(
        readingContent: String,
        bookName: String,
        chapterTitle: String,
        haveBeenRead: Boolean,
        lastRead: Boolean,
    )

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChapters(
        chapterEntities : List<ChapterEntity>
    )

    @Query("SELECT * FROM chapter_entity WHERE bookName= :bookName")
    fun getChapters(bookName: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_entity WHERE bookName = :bookName Limit 1")
    fun getChapter(bookName: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapter_entity WHERE title = :chapterTitle AND bookName = :bookName AND content Not Null Limit 1")
    fun getChapterByChapter(chapterTitle: String, bookName: String): Flow<ChapterEntity?>

    @Query("DELETE FROM chapter_entity WHERE bookName = :bookName ")
    fun deleteLocalChaptersByName(bookName: String)

}