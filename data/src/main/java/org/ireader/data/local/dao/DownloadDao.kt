package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload

@Dao
interface DownloadDao : BaseDao<SavedDownload> {

    @Query("SELECT * FROM download")
    fun findAllDownloads(): Flow<List<SavedDownload>>

    @Query("SELECT * FROM download WHERE bookId = :bookId")
    fun findDownload(bookId: Long): Flow<SavedDownload?>

    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()


}

