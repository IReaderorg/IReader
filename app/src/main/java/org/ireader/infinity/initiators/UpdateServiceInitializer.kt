package org.ireader.infinity.initiators

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ireader.domain.services.downloaderService.DownloadService
import org.ireader.domain.services.update_service.UpdateService
import javax.inject.Inject


class UpdateServiceInitializer @Inject constructor(app: Application) {

    init {
        val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()
        val manager = WorkManager.getInstance(app)
        manager.cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
        manager.enqueue(updateRequest)
    }
}