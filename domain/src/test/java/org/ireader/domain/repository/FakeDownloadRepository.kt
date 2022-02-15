package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.models.entities.SavedDownload

class FakeDownloadRepository : DownloadRepository {

    val downloads = mutableListOf<SavedDownload>()

    override fun findAllDownloads(): Flow<List<SavedDownload>> = flow {
        val result = downloads
        emit(result)
    }

    override fun findAllDownloadsByPaging(): PagingSource<Int, SavedDownload> {
        TODO("Not yet implemented")
    }

    override fun findOneDownload(bookId: Long): Flow<SavedDownload?> = flow {
        val result = downloads.find { it.bookId == bookId }
        emit(result)
    }

    override suspend fun insertDownload(savedDownload: SavedDownload) {
        downloads.add(savedDownload)
    }

    override suspend fun insertDownloads(savedDownloadList: List<SavedDownload>) {
        downloads.addAll(savedDownloadList)
    }

    override suspend fun deleteDownload(savedDownload: SavedDownload) {
        downloads.removeIf { it.bookId == savedDownload.bookId }
    }

    override suspend fun deleteDownloadByBookId(bookId: Long) {
        downloads.removeIf { it.bookId == bookId }
    }

    override suspend fun deleteAllDownload() {
        downloads.clear()
    }
}