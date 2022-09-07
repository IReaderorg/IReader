package ireader.data.repository

import ireader.common.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Download
import ireader.common.models.entities.SavedDownloadWithInfo
import ireader.data.local.dao.DownloadDao

class DownloadRepositoryImpl(private val dao: DownloadDao) :
    DownloadRepository {

    override fun subscribeAllDownloads(): Flow<List<ireader.common.models.entities.SavedDownloadWithInfo>> {
        return dao.subscribeAllDownloads()
    }

    override suspend fun findAllDownloads(): List<SavedDownloadWithInfo> {
        return dao.findAllDownloads()
    }

    override suspend fun insertDownload(savedDownload: Download) {
        return dao.insertOrUpdate(savedDownload)
    }

    override suspend fun insertDownloads(savedDownloads: List<Download>) {
        return dao.insertOrUpdate(savedDownloads)
    }

    override suspend fun deleteSavedDownload(savedDownload: Download) {
        dao.delete(savedDownload)
    }

    override suspend fun deleteSavedDownload(savedDownloads: List<Download>) {
        return dao.delete(savedDownloads)
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        dao.deleteSavedDownloadByBookId(bookId)
    }

    override suspend fun deleteAllSavedDownload() {
        dao.deleteAllSavedDownload()
    }
}
