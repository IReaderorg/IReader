package ireader.domain.usecases.services

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_BOOKS_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_Chapters_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_MODE
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_SERVICE_NAME
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.utils.toast



actual class StartDownloadServicesUseCase( private val context: Context) {
    actual fun start(
            bookIds: LongArray?,
            chapterIds: LongArray?,
            downloadModes: Boolean,
    ) {
        try {
            val work = OneTimeWorkRequestBuilder<DownloaderService>().apply {
                setInputData(
                    Data.Builder().apply {
                        bookIds?.let { bookIds ->
                            putLongArray(DOWNLOADER_BOOKS_IDS, bookIds)
                        }
                        chapterIds?.let { chapterIds ->
                            putLongArray(DOWNLOADER_Chapters_IDS, chapterIds)
                        }
                        if (downloadModes) {
                            putBoolean(DOWNLOADER_MODE, true)
                        }
                    }.build()
                )
                addTag(DOWNLOADER_SERVICE_NAME)
            }.build()
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
