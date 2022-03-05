package org.ireader.infinity.initiators

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.feature_services.updater_service.UpdateService
import timber.log.Timber
import javax.inject.Inject


class UpdateService @Inject constructor(app: Application) {

    init {
        Timber.e("Update service activated.")
        val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()
        val manager = WorkManager.getInstance(app)
        manager.cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
        manager.enqueue(updateRequest)
    }
}