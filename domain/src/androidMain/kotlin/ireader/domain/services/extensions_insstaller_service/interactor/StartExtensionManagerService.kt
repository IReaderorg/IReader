package ireader.domain.services.extensions_insstaller_service.interactor

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.utils.toast



class StartExtensionManagerService(
    private val context: Context
) {
    operator fun invoke(
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
}