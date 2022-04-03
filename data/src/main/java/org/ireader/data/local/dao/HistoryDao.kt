package org.ireader.data.local.dao

import androidx.room.*
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History

@Dao
interface HistoryDao {

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(history: List<History>): List<Long>

    @Delete
    suspend fun deleteAllHistories(history: List<History>)

    @Query("DELETE FROM history WHERE chapterId = :id")
    suspend fun deleteHistory(id: Long)
}