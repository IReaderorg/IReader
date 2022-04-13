package org.ireader.domain.use_cases.download

import androidx.annotation.Keep
import org.ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import org.ireader.domain.use_cases.download.get.GetOneSavedDownload
import org.ireader.domain.use_cases.download.get.SubscribeDownloadsUseCase
import org.ireader.domain.use_cases.download.insert.InsertDownload
import org.ireader.domain.use_cases.download.insert.InsertDownloads
import javax.inject.Inject

@Keep
data class DownloadUseCases @Inject constructor(
    val subscribeDownloadsUseCase: SubscribeDownloadsUseCase,
    val getOneSavedDownload: GetOneSavedDownload,
    val deleteAllSavedDownload: DeleteAllSavedDownload,
    val deleteSavedDownload: DeleteSavedDownload,
    val deleteSavedDownloadByBookId: DeleteSavedDownloadByBookId,
    val insertDownload: InsertDownload,
    val insertDownloads: InsertDownloads,
)
