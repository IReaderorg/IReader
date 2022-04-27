package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao : BaseDao<org.ireader.common_models.entities.SavedDownload> {

    @Query("""
        SELECT download.*,length(chapter.content) > 10 AS isDownloaded FROM download
        JOIN chapter ON download.chapterId == chapter.id
    """)
    fun subscribeAllDownloads(): Flow<List<org.ireader.common_models.entities.SavedDownloadWithInfo>>


    @Query("SELECT * FROM download")
    fun findAllDownloads(): List<org.ireader.common_models.entities.SavedDownload>

    @Query("""
        DELETE FROM download where chapterId in (
        SELECT chapterId FROM chapter WHERE length(chapter.content) > 1
        )
    """)
    suspend fun deleteDownloadedBooks()


    @Query("SELECT * FROM download WHERE chapterId in (:downloadIds)")
    suspend fun findDownloads(downloadIds : List<Long>): List<org.ireader.common_models.entities.SavedDownload>

    @Query("SELECT * FROM download WHERE bookId = :bookId")
    fun findDownload(bookId: Long): Flow<org.ireader.common_models.entities.SavedDownload?>

    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()


}

