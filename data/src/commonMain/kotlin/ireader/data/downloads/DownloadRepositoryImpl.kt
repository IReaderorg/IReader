package ireader.data.downloads

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.data.util.BaseDao
import ireader.data.core.DatabaseHandler


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
    
    override suspend fun updateDownloadPriority(chapterId: Long, priority: Int) {
        handler.await {
            downloadQueries.updatePriority(priority, chapterId)
        }
    }
    
    override suspend fun markDownloadAsFailed(chapterId: Long, errorMessage: String) {
        // Failed downloads are tracked in DownloadServiceState
        // This method is here for future database schema updates
    }
    
    override suspend fun retryFailedDownload(chapterId: Long) {
        // Retry is handled by re-adding to download queue
        // This method is here for future enhancements
    }


}
