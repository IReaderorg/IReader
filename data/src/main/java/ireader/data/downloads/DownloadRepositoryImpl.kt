package ireader.data.downloads

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Download
import ireader.common.models.entities.SavedDownloadWithInfo
import ireader.data.local.DatabaseHandler
import ireader.data.util.BaseDao


class DownloadRepositoryImpl(private val handler: DatabaseHandler,) :
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
                downloadQueries.upsert(savedDownload.chapterId,savedDownload.bookId,savedDownload.priority)
        }
    }

    override suspend fun insertDownloads(savedDownloads: List<Download>) {
        handler.await {
            dbOperation(savedDownloads) {
                downloadQueries.upsert(it.chapterId,it.bookId,it.priority)
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
            savedDownloads.forEach {  savedDownload ->
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
