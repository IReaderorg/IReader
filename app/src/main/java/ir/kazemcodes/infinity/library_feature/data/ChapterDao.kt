package ir.kazemcodes.infinity.library_feature.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity


@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapterEntities: List<ChapterEntity>)

    @Query("SELECT * FROM chapter_entity WHERE bookName= :bookName")
    suspend fun getChapters(bookName : String) : List<ChapterEntity>

    @Query("SELECT * FROM chapter_entity WHERE bookName = :bookName")
    suspend fun getChapter(bookName: String) : ChapterEntity

}