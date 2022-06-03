package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Download
import org.ireader.common_models.entities.SavedDownloadWithInfo
import org.ireader.data.local.dao.DownloadDao

class DownloadRepositoryImpl(private val dao: DownloadDao) :
    org.ireader.common_data.repository.DownloadRepository {

    override fun subscribeAllDownloads(): Flow<List<org.ireader.common_models.entities.SavedDownloadWithInfo>> {
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
