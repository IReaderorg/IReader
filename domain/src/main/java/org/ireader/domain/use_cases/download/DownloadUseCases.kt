package org.ireader.domain.use_cases.download

import org.ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloads
import org.ireader.domain.use_cases.download.get.FindAllDownloadsUseCase
import org.ireader.domain.use_cases.download.get.FindDownloadsUseCase
import org.ireader.domain.use_cases.download.get.SubscribeDownloadsUseCase
import org.ireader.domain.use_cases.download.insert.InsertDownload
import org.ireader.domain.use_cases.download.insert.InsertDownloads

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
