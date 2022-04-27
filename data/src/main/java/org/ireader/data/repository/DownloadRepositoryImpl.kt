package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.data.local.dao.DownloadDao

class DownloadRepositoryImpl(private val dao: DownloadDao) :
    org.ireader.common_data.repository.DownloadRepository {


    override fun subscribeAllDownloads(): Flow<List<org.ireader.common_models.entities.SavedDownloadWithInfo>> {
        return dao.subscribeAllDownloads()
    }

    override suspend fun findAllDownloads(): List<org.ireader.common_models.entities.SavedDownload> {
       return dao.findAllDownloads()
    }

    override suspend fun findDownloads(downloadIds: List<Long>): List<org.ireader.common_models.entities.SavedDownload> {
        return dao.findDownloads(downloadIds)
    }

    override fun findSavedDownload(bookId: Long): Flow<org.ireader.common_models.entities.SavedDownload?> {
        return dao.findDownload(bookId)
    }

    override suspend fun insertDownload(savedDownload: org.ireader.common_models.entities.SavedDownload): Long {
        return dao.insert(savedDownload)
    }


    override suspend fun insertDownloads(savedDownloads: List<org.ireader.common_models.entities.SavedDownload>): List<Long> {
        return dao.insert(savedDownloads)
    }

    override suspend fun deleteSavedDownload(savedDownload: org.ireader.common_models.entities.SavedDownload) {
        dao.delete(savedDownload)
    }

    override suspend fun deleteSavedDownload(savedDownloads: List<org.ireader.common_models.entities.SavedDownload>) {
        return dao.delete(savedDownloads)
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        dao.deleteSavedDownloadByBookId(bookId)
    }

    override suspend fun deleteAllSavedDownload() {
        dao.deleteAllSavedDownload()
    }

}