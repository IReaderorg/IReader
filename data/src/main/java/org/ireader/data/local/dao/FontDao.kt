package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
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


    @Transaction
    suspend fun insertOrUpdate(objList: List<org.ireader.common_models.entities.FontEntity>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<org.ireader.common_models.entities.FontEntity>()


        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }
    @Transaction
    suspend fun insertOrUpdate(objList: org.ireader.common_models.entities.FontEntity) {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<org.ireader.common_models.entities.FontEntity>()


        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objectToInsert[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }

}