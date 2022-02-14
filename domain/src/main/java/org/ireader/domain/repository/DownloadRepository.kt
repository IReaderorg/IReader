package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload

interface DownloadRepository {

    fun getAllDownloads(): Flow<List<SavedDownload>>

    fun getAllDownloadsByPaging(): PagingSource<Int, SavedDownload>

    fun getOneSavedDownload(bookId: Long): Flow<SavedDownload?>

    suspend fun insertDownload(savedDownload: SavedDownload)

    suspend fun insertDownloads(savedDownloads: List<SavedDownload>)

    suspend fun deleteSavedDownload(savedDownload: SavedDownload)

    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    suspend fun deleteAllSavedDownload()

}