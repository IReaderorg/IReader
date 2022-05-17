package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.FontEntity

@Dao
interface FontDao: BaseDao<FontEntity> {

    @Query("SELECT * FROM fonts WHERE fontName = :fontName")
    fun findFontEntity(fontName:String) : FontEntity

    @Query("SELECT * FROM fonts")
    fun subscribeFontEntities() : Flow<List<FontEntity>>
    @Query("SELECT * FROM fonts")
    suspend fun findAllFontEntities() : List<FontEntity>



    @Query("DELETE FROM fonts")
    fun deleteAllFonts()
}