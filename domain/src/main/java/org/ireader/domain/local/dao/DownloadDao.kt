package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload

@Dao
interface DownloadDao {

    @Query("SELECT * FROM download")
    fun getAllDownloads(): Flow<List<SavedDownload>>

    @Query("SELECT * FROM download")
    fun getAllDownloadsByPaging(): PagingSource<Int, SavedDownload>

    @Query("SELECT * FROM download WHERE bookId = :bookId")
    fun getOneDownloads(bookId: Long): Flow<SavedDownload?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(savedDownload: SavedDownload)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(savedDownloads: List<SavedDownload>)


    @Delete
    suspend fun deleteSavedDownload(savedDownload: SavedDownload)


    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()


}