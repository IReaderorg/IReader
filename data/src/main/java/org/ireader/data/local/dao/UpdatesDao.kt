package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Update
import org.ireader.domain.models.entities.UpdateWithInfo


@Dao
interface UpdatesDao : BaseDao<Update> {


    @Query("""
        SELECT 
        updates.id,
        updates.chapterId,
        updates.bookId,
        library.sourceId,
        chapter.link as chapterLink,
        library.title as bookTitle,
        library.cover,
        library.favorite,
        chapter.dateUpload as chapterDateUpload,
        chapter.title as chapterTitle,
        chapter.read,
        chapter.number,
        length(chapter.content) > 10 as downloaded,
        updates.date
        FROM updates
         JOIN library ON library.id == updates.bookId 
         JOIN chapter ON chapter.id == updates.chapterId

    """)
    fun subscribeUpdates(): Flow<List<UpdateWithInfo>>


    @Query("Delete FROM updates")
    suspend fun deleteAllUpdates()
}