package org.ireader.domain.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload

@Dao
interface DownloadDao {

    @Query("SELECT * FROM download_table")
    fun getAllDownloads(): Flow<List<SavedDownload>>

    @Query("SELECT * FROM download_table WHERE bookId = :bookId")
    fun getOneDownloads(bookId: Long): Flow<SavedDownload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(savedDownload: SavedDownload)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(savedDownloads: List<SavedDownload>)


    @Delete
    suspend fun deleteSavedDownload(savedDownload: SavedDownload)


    @Query("DELETE FROM download_table WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download_table")
    suspend fun deleteAllSavedDownload()


}