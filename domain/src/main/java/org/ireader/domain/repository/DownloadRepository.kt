package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.models.entities.SavedDownloadWithInfo

interface DownloadRepository {

    fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>>
    suspend fun findAllDownloads(): List<SavedDownload>
    suspend fun findDownloads(downloadIds : List<Long>): List<SavedDownload>


    fun findSavedDownload(bookId: Long): Flow<SavedDownload?>

    suspend fun insertDownload(savedDownload: SavedDownload): Long


    suspend fun insertDownloads(savedDownloads: List<SavedDownload>): List<Long>

    suspend fun deleteSavedDownload(savedDownload: SavedDownload)

    suspend fun deleteSavedDownload(savedDownloads: List<SavedDownload>)

    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    suspend fun deleteAllSavedDownload()

}