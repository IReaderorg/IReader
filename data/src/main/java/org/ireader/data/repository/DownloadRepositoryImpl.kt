package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.data.local.dao.DownloadDao
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository

class DownloadRepositoryImpl(private val dao: DownloadDao) : DownloadRepository {


    override fun findAllDownloads(): Flow<List<SavedDownload>> {
        return dao.findAllDownloads()
    }

    override fun findOneSavedDownload(bookId: Long): Flow<SavedDownload?> {
        return dao.findDownload(bookId)
    }

    override suspend fun insertDownload(savedDownload: SavedDownload): Long {
        return dao.insert(savedDownload)
    }

    override suspend fun insertDownloads(savedDownloads: List<SavedDownload>): List<Long> {
        return dao.insert(savedDownloads)
    }

    override suspend fun deleteSavedDownload(savedDownload: SavedDownload) {
        dao.delete(savedDownload)
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        dao.deleteSavedDownloadByBookId(bookId)
    }

    override suspend fun deleteAllSavedDownload() {
        dao.deleteAllSavedDownload()
    }

}