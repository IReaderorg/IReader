package ir.kazemcodes.infinity.core.data.local.dao

import androidx.room.*
import ir.kazemcodes.infinity.core.domain.models.ChapterEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface LibraryChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapterEntities: List<ChapterEntity>)

    @Query("UPDATE chapter_table SET haveBeenRead = :haveBeenRead, content = :readingContent, lastRead = :lastRead WHERE bookName = :bookName AND title = :chapterTitle AND source =:source")
    suspend fun updateChapter(
        readingContent: String,
        bookName: String,
        chapterTitle: String,
        haveBeenRead: Boolean,
        lastRead: Boolean,
        source: String,
    )

    @Query("UPDATE chapter_table SET lastRead = 0 WHERE bookName = :bookName AND lastRead = 1 AND source = :source")
    suspend fun deleteLastReadChapter(
        bookName: String,
        source: String,
    )
    @Query("UPDATE chapter_table SET lastRead = 1, haveBeenRead = 1 WHERE title = :chapterTitle AND bookName = :bookName AND source = :source")
    suspend fun setLastReadChapter(
        bookName: String,
        chapterTitle: String,
        source: String,
    )

    @Query("SELECT * from chapter_table WHERE bookName = :bookName AND source =:source AND lastRead = 1 LIMIT 1")
    fun getLastReadChapter(bookName: String, source: String): Flow<ChapterEntity?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChapter(chapterEntities: ChapterEntity)

    @Update(entity = ChapterEntity::class,onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChapters(
        chapterEntities: List<ChapterEntity>,
    )
    @Query("UPDATE chapter_table SET inLibrary = 1 WHERE title = :chapterTitle AND source = :source AND bookName = :bookName")
    suspend fun updateAddToLibraryChapters(
        chapterTitle: String,
        source: String,
        bookName: String
    )

    //    @Query("SELECT * FROM chapter_entity WHERE bookName= :bookName")
//    fun getChapters(bookName: String): Flow<List<ChapterEntity>>
    @Query("SELECT * FROM chapter_table WHERE bookName= :bookName AND source = :source")
    fun getChapters(bookName: String, source: String): Flow<List<ChapterEntity>?>

    @Query("SELECT * FROM chapter_table")
    fun getAllChapters(): Flow<List<ChapterEntity>?>


    @Query("SELECT * FROM chapter_table WHERE title = :chapterTitle AND bookName = :bookName AND source = :source AND content Not Null Limit 1")
    fun getChapterByChapter(
        chapterTitle: String,
        bookName: String,
        source: String,
    ): Flow<ChapterEntity?>

    @Query("DELETE FROM chapter_table WHERE bookName = :bookName AND source = :source")
    fun deleteLocalChaptersByName(bookName: String, source: String)

    @Query("DELETE FROM chapter_table ")
    fun deleteAllChapters()

    @Query("DELETE FROM chapter_table WHERE inLibrary = 0")
    fun deleteLibraryChapters()
}
