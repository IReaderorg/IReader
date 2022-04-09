package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import org.ireader.domain.feature_service.io.HistoryWithRelations
import org.ireader.domain.models.entities.History

@Dao
interface HistoryDao : BaseDao<History> {

    @Query("SELECT * FROM history WHERE chapterId = :id LIMIT 1")
    suspend fun findHistory(id: Long): History?

    @Query("SELECT history.* FROM history GROUP  By history.chapterId HAVING bookId = :bookId  ORDER BY history.readAt DESC LIMIT 1")
    suspend fun findHistoryByBookId(bookId: Long): History?

    @Query("SELECT * FROM history")
    suspend fun findHistories(): List<History>


    @Query("""SELECT history.*, library.title as bookTitle, library.sourceId, library.cover, library.favorite, chapter.title as chapterTitle,
    date(ROUND(history.readAt / 1000), 'unixepoch', 'localtime') AS date,chapter.number as chapterNumber
    FROM history
    JOIN library ON history.bookId = library.id
    JOIN chapter ON history.chapterId = chapter.id
    WHERE bookTitle LIKE '%' || :query || '%' 
    ORDER BY history.readAt DESC""")
    fun findHistoriesPaging(query: String): kotlinx.coroutines.flow.Flow<List<HistoryWithRelations>>


    @Query("DELETE FROM history WHERE chapterId = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
}