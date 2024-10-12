package ireader.data.downloads

import ireader.data.core.DatabaseHandler
import ireader.data.util.BaseDao
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo
import kotlinx.coroutines.flow.Flow


class DownloadRepositoryImpl(private val handler: DatabaseHandler) :
    DownloadRepository, BaseDao<Download>() {
    override fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>> {
        return handler.subscribeToList {
            downloadQueries.findAll(downloadMapper)
        }
    }

    override suspend fun findAllDownloads(): List<SavedDownloadWithInfo> {
        return handler.awaitList {
            downloadQueries.findAll(downloadMapper)
        }
    }

    override suspend fun insertDownload(savedDownload: Download) {
        handler.await {
            downloadQueries.updatePriority(
                savedDownload.priority,
                savedDownload.chapterId,
                savedDownload.bookId
            )
            if (downloadQueries.selectChanges().executeAsOne() == 0L)
                downloadQueries.insert(
                    savedDownload.chapterId,
                    savedDownload.bookId,
                    savedDownload.priority
                )

        }
    }

    override suspend fun insertDownloads(savedDownloads: List<Download>) {
        handler.await {
            dbOperation(savedDownloads) {
                downloadQueries.updatePriority(it.priority, it.chapterId, it.bookId)
                if (downloadQueries.selectChanges().executeAsOne() == 0L)
                    downloadQueries.insert(it.chapterId, it.bookId, it.priority)
            }
        }
    }

    override suspend fun deleteSavedDownload(savedDownload: Download) {
        handler.await {
            downloadQueries.deleteByChapterId(savedDownload.chapterId)
        }
    }

    override suspend fun deleteSavedDownload(savedDownloads: List<Download>) {
        handler.await(true) {
            savedDownloads.forEach { savedDownload ->
                downloadQueries.deleteByChapterId(savedDownload.chapterId)
            }
        }
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        handler.await {
            downloadQueries.deleteByBookId(bookId)
        }
    }

    override suspend fun deleteAllSavedDownload() {
        handler.await {
            downloadQueries.deleteAll()
        }
    }


}
