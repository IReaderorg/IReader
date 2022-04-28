package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.ireader.common_models.entities.HistoryWithRelations

@Dao
interface HistoryDao : BaseDao<org.ireader.common_models.entities.History> {

    @Query("SELECT * FROM history WHERE chapterId = :id LIMIT 1")
    suspend fun findHistory(id: Long): org.ireader.common_models.entities.History?

    @Query("SELECT history.* FROM history GROUP  By history.chapterId HAVING bookId = :bookId  ORDER BY history.readAt DESC LIMIT 1")
    suspend fun findHistoryByBookId(bookId: Long): org.ireader.common_models.entities.History?

    @Query("SELECT * FROM history")
    suspend fun findHistories(): List<org.ireader.common_models.entities.History>

    @Query(
        """SELECT history.*, library.title as bookTitle, library.sourceId, library.cover, library.favorite, chapter.title as chapterTitle,
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
}
