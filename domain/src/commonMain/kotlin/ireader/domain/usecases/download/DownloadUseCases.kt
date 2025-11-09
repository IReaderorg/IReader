package ireader.domain.usecases.download

import ireader.domain.usecases.download.delete.DeleteAllSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownloadByBookId
import ireader.domain.usecases.download.delete.DeleteSavedDownloads
import ireader.domain.usecases.download.get.FindAllDownloadsUseCase
import ireader.domain.usecases.download.get.FindDownloadsUseCase
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.download.insert.InsertDownload
import ireader.domain.usecases.download.insert.InsertDownloads
import ireader.domain.usecases.download.update.UpdateDownloadPriority

data class DownloadUseCases(
    val subscribeDownloadsUseCase: SubscribeDownloadsUseCase,
    val findAllDownloadsUseCase: FindAllDownloadsUseCase,
    val findDownloadsUseCase: FindDownloadsUseCase,
    val deleteAllSavedDownload: DeleteAllSavedDownload,
    val deleteSavedDownload: DeleteSavedDownload,
    val deleteSavedDownloads: DeleteSavedDownloads,
    val deleteSavedDownloadByBookId: DeleteSavedDownloadByBookId,
    val insertDownload: InsertDownload,
    val insertDownloads: InsertDownloads,
    val updateDownloadPriority: UpdateDownloadPriority,
)
