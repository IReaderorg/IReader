package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.models.entities.SavedDownloadWithInfo

@Dao
interface DownloadDao : BaseDao<SavedDownload> {

    @Query("""
        SELECT download.*,length(chapter.content) > 10 AS isDownloaded FROM download
        JOIN chapter ON download.chapterId == chapter.id
    """)
    fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>>


    @Query("SELECT * FROM download")
    fun findAllDownloads(): List<SavedDownload>

    @Query("""
        DELETE FROM download where chapterId in (
        SELECT chapterId FROM chapter WHERE length(chapter.content) > 1
        )
    """)
    suspend fun deleteDownloadedBooks()


    @Query("SELECT * FROM download WHERE chapterId in (:downloadIds)")
    suspend fun findDownloads(downloadIds : List<Long>): List<SavedDownload>

    @Query("SELECT * FROM download WHERE bookId = :bookId")
    fun findDownload(bookId: Long): Flow<SavedDownload?>

    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()


}

