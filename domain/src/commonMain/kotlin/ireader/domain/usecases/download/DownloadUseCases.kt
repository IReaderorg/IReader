package ireader.domain.usecases.download

import ireader.domain.usecases.download.delete.DeleteAllSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownloads
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.download.insert.InsertDownload
import ireader.domain.usecases.download.insert.InsertDownloads
import ireader.domain.usecases.download.update.UpdateDownloadPriority
import ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase

/**
 * Aggregate class for all download-related use cases
 * Provides a single point of access for download operations
 */
data class DownloadUseCases(
    val downloadChapter: DownloadChapterUseCase,
    val downloadChapters: DownloadChaptersUseCase,
    val downloadUnreadChapters: DownloadUnreadChaptersUseCase,
    val cancelDownload: CancelDownloadUseCase,
    val pauseDownload: PauseDownloadUseCase,
    val resumeDownload: ResumeDownloadUseCase,
    val getDownloadStatus: GetDownloadStatusUseCase,
    val subscribeDownloadsUseCase: SubscribeDownloadsUseCase,
    val insertDownload: InsertDownload,
    val insertDownloads: InsertDownloads,
    val deleteSavedDownload: DeleteSavedDownload,
    val deleteAllSavedDownload: DeleteAllSavedDownload,
    val deleteSavedDownloads: DeleteSavedDownloads,
    val updateDownloadPriority: UpdateDownloadPriority
)
