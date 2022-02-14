package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.models.entities.SavedDownload

class FakeDownloadRepository : DownloadRepository {

    val downloads = mutableListOf<SavedDownload>()
    override fun getAllDownloads(): Flow<List<SavedDownload>> = flow {
        val result = downloads
        emit(result)
    }

    override fun getAllDownloadsByPaging(): PagingSource<Int, SavedDownload> {
        TODO("Not yet implemented")
    }

    override fun getOneSavedDownload(bookId: Long): Flow<SavedDownload?> = flow {
        val result = downloads.find { it.bookId == bookId }
        emit(result)
    }

    override suspend fun insertDownload(savedDownload: SavedDownload) {
        downloads.add(savedDownload)
    }

    override suspend fun insertDownloads(savedDownloads: List<SavedDownload>) {
        downloads.addAll(savedDownloads)
    }

    override suspend fun deleteSavedDownload(savedDownload: SavedDownload) {
        downloads.removeIf { it.id == savedDownload.id }
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        downloads.removeIf { it.bookId == bookId }
    }

    override suspend fun deleteAllSavedDownload() {
        downloads.clear()
    }
}