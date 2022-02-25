package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.models.entities.SavedDownload

internal class FakeDownloadRepository : DownloadRepository {

    val downloads = mutableListOf<SavedDownload>()

    override fun findAllDownloads(): Flow<List<SavedDownload>> = flow {
        val result = downloads
        emit(result)
    }

    override fun findAllDownloadsByPaging(): PagingSource<Int, SavedDownload> {
        throw UnsupportedOperationException("unsupported")
    }

    override fun findOneSavedDownload(bookId: Long): Flow<SavedDownload?> = flow {
        val result = downloads.find { it.bookId == bookId }
        emit(result)
    }


    override suspend fun insertDownload(savedDownload: SavedDownload): Long {
        downloads.add(savedDownload)
        return 1L
    }

    override suspend fun insertDownloads(savedDownloadList: List<SavedDownload>): List<Long> {
        downloads.addAll(savedDownloadList)
        return listOf(1L)
    }

    override suspend fun deleteSavedDownload(savedDownload: SavedDownload) {
        downloads.removeIf { it.bookId == savedDownload.bookId }
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        downloads.removeIf { it.bookId == bookId }
    }

    override suspend fun deleteAllSavedDownload() {
        downloads.clear()
    }
}