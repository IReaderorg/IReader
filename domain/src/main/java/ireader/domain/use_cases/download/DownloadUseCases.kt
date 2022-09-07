package ireader.domain.use_cases.download

import ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import ireader.domain.use_cases.download.delete.DeleteSavedDownload
import ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import ireader.domain.use_cases.download.delete.DeleteSavedDownloads
import ireader.domain.use_cases.download.get.FindAllDownloadsUseCase
import ireader.domain.use_cases.download.get.FindDownloadsUseCase
import ireader.domain.use_cases.download.get.SubscribeDownloadsUseCase
import ireader.domain.use_cases.download.insert.InsertDownload
import ireader.domain.use_cases.download.insert.InsertDownloads

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
)
