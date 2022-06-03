package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.HistoryWithRelations

@Dao
interface HistoryDao : BaseDao<org.ireader.common_models.entities.History> {

    @Query("SELECT * FROM history WHERE chapterId = :id LIMIT 1")
    suspend fun findHistory(id: Long): org.ireader.common_models.entities.History?

    @Query("SELECT history.* FROM history GROUP  By history.chapterId HAVING bookId = :bookId  ORDER BY history.readAt DESC LIMIT 1")
    suspend fun findHistoryByBookId(bookId: Long): org.ireader.common_models.entities.History?


    @Query("SELECT history.* FROM history GROUP  By history.chapterId HAVING bookId = :bookId  ORDER BY history.readAt DESC LIMIT 1")
    fun subscribeHistoryByBookId(bookId: Long): kotlinx.coroutines.flow.Flow<History?>

    @Query("SELECT * FROM history")
    suspend fun findHistories(): List<org.ireader.common_models.entities.History>

    @Query(
        """SELECT history.*, library.title as bookTitle, library.sourceId, library.cover, library.favorite, chapter.name as chapterTitle,
    date(ROUND(history.readAt / 1000), 'unixepoch', 'localtime') AS date,chapter.number as chapterNumber
    FROM history
    JOIN library ON history.bookId = library.id
    JOIN chapter ON history.chapterId = chapter.id
    WHERE bookTitle LIKE '%' || :query || '%' 
    ORDER BY history.readAt DESC"""
    )
    fun findHistoriesPaging(query: String): kotlinx.coroutines.flow.Flow<List<HistoryWithRelations>>

    @Query("DELETE FROM history WHERE chapterId = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()



    @Transaction
    suspend fun insertOrUpdate(objList: List<org.ireader.common_models.entities.History>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<org.ireader.common_models.entities.History>()


        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }
    @Transaction
    suspend fun insertOrUpdate(objList: org.ireader.common_models.entities.History) {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<org.ireader.common_models.entities.History>()


        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objectToInsert[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }

}
