package ir.kazemcodes.infinity.library_feature.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapterEntities: List<ChapterEntity>)

    @Query("UPDATE chapter_entity SET content = :readingContent WHERE bookName = :bookName AND title = :chapterTitle")
    suspend fun updateChapter(readingContent : String , bookName: String , chapterTitle: String)

    @Query("SELECT * FROM chapter_entity WHERE bookName= :bookName")
    fun getChapters(bookName : String) : Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_entity WHERE bookName = :bookName Limit 1")
    fun getChapter(bookName: String) : Flow<ChapterEntity?>
    @Query("SELECT * FROM chapter_entity WHERE title = :chapterTitle AND bookName = :bookName AND content Not Null Limit 1")
    fun getChapterByChapter(chapterTitle : String, bookName: String) : Flow<ChapterEntity?>

}