package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao : BaseDao<org.ireader.common_models.entities.Download> {

    @Query(
        """
        SELECT download.*, library.id, library.title as bookName,
        chapter.'key' as chapterKey, chapter.name as chapterName,
        chapter.translator,library.sourceId,length(chapter.content) > 10 AS isDownloaded
        FROM download
        JOIN library ON download.bookId = library.id
        JOIN chapter ON download.chapterId = chapter.id;
    """
    )
    fun subscribeAllDownloads(): Flow<List<org.ireader.common_models.entities.SavedDownloadWithInfo>>

    @Query(
        """
                SELECT download.*, library.id,
            library.title as bookName, chapter.'key' as chapterKey,
            chapter.name as chapterName,
        chapter.translator,library.sourceId,length(chapter.content) > 10 AS isDownloaded
        FROM download
        JOIN library ON download.bookId = library.id
        JOIN chapter ON download.chapterId = chapter.id;
    """
    )
    fun findAllDownloads(): List<org.ireader.common_models.entities.SavedDownloadWithInfo>

    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()
}
