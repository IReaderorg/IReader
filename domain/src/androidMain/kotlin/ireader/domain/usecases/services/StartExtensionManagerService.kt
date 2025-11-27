package ireader.domain.usecases.services

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.utils.toast


actual class StartExtensionManagerService(
        val context: Context
) {
    actual fun start(
    ) {
        try {
            val work = OneTimeWorkRequestBuilder<ExtensionManagerService>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                    DOWNLOADER_SERVICE_NAME,
                    ExistingWorkPolicy.REPLACE,
                    work
            )
        } catch (e: IllegalStateException) {
            context.toast(e.localizedMessage)
        } catch (e: Throwable) {
            context.toast(e.localizedMessage)
        }
    }

    actual fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(DOWNLOADER_SERVICE_NAME)
    }
}