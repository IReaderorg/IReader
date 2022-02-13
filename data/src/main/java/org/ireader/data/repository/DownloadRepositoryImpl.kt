package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.local.dao.DownloadDao
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository

class DownloadRepositoryImpl(private val dao: DownloadDao) : DownloadRepository {


    override fun getAllDownloads(): Flow<List<SavedDownload>> {
        return dao.getAllDownloads()
    }

    override fun getOneSavedDownload(bookId: Long): Flow<SavedDownload> {
        return dao.getOneDownloads(bookId)
    }

    override suspend fun insertDownload(savedDownload: SavedDownload) {
        dao.insertDownload(savedDownload = savedDownload)
    }

    override suspend fun insertDownloads(savedDownloads: List<SavedDownload>) {
        dao.insertDownloads(savedDownloads)
    }

    override suspend fun deleteSavedDownload(savedDownload: SavedDownload) {
        dao.deleteSavedDownload(savedDownload)
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        dao.deleteSavedDownloadByBookId(bookId)
    }

    override suspend fun deleteAllSavedDownload() {
        dao.deleteAllSavedDownload()
    }

}