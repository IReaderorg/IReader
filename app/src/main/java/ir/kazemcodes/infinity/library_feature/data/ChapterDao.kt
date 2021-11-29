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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapterEntities: ChapterEntity)

    @Query("SELECT * FROM chapter_entity WHERE bookName= :bookName")
    fun getChapters(bookName : String) : Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter_entity WHERE bookName = :bookName")
    fun getChapter(bookName: String) : Flow<ChapterEntity>
    @Query("SELECT * FROM chapter_entity WHERE title = :chapterTitle AND bookName = :bookName")
    fun getChapterByChapter(chapterTitle : String, bookName: String) : Flow<ChapterEntity>

}