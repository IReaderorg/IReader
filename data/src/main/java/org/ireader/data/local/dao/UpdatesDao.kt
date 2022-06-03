package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UpdatesDao : BaseDao<org.ireader.common_models.entities.Update> {

    @Query(
        """
        SELECT 
        updates.id,
        updates.chapterId,
        updates.bookId,
        library.sourceId,
        chapter.`key` as chapterLink,
        library.title as bookTitle,
        library.cover,
        library.favorite,
        chapter.dateUpload as chapterDateUpload,
        chapter.name as chapterTitle,
        chapter.read,
        date(ROUND(updates.date / 1000), 'unixepoch', 'localtime') AS date,
        chapter.number,
        length(chapter.content) > 10 as downloaded
        FROM updates
         JOIN library ON library.id == updates.bookId 
         JOIN chapter ON chapter.id == updates.chapterId

    """
    )
    fun subscribeUpdates(): Flow<List<org.ireader.common_models.entities.UpdateWithInfo>>

    @Query("Delete FROM updates")
    suspend fun deleteAllUpdates()
    @Transaction
    suspend fun insertOrUpdate(objList: List<org.ireader.common_models.entities.Update>): List<Long> {
        val insertResult = insert(objList)
        val updateList = mutableListOf<org.ireader.common_models.entities.Update>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
                idList.add(objList[i].id)
            } else {
                idList.add(insertResult[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
        return idList
    }
    @Transaction
    suspend fun insertOrUpdate(objList: org.ireader.common_models.entities.Update): Long {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<org.ireader.common_models.entities.Update>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objectToInsert[i])
                idList.add(objectToInsert[i].id)
            } else {
                idList.add(insertResult[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
        return idList.firstOrNull()?:-1
    }

}
