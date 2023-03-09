package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo

interface DownloadRepository {

    fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>>
    suspend fun findAllDownloads(): List<SavedDownloadWithInfo>

    suspend fun insertDownload(savedDownload: Download)

    suspend fun insertDownloads(savedDownloads: List<Download>)

    suspend fun deleteSavedDownload(savedDownload: Download)

    suspend fun deleteSavedDownload(savedDownloads: List<Download>)

    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    suspend fun deleteAllSavedDownload()
}
