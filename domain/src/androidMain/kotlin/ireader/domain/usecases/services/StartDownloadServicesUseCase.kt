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

/**
 * Android implementation of StartDownloadServicesUseCase
 * 
 * ✅ CLEAN ARCHITECTURE: This use case correctly uses WorkManager to start
 * the download service. It only uses constants from DownloadServiceStateImpl
 * for backward compatibility with the WorkManager implementation.
 * 
 * Note: The constants (DOWNLOADER_BOOKS_IDS, etc.) are used for WorkManager
 * data passing and can be moved to a separate constants file in the future.
 */
actual class StartDownloadServicesUseCase( private val context: Context) {
    actual fun start(
            bookIds: LongArray?,
            chapterIds: LongArray?,
            downloadModes: Boolean,
    ) {
        try {
            // Create unique work name based on content to allow concurrent downloads
            val workName = if (downloadModes) {
                DOWNLOADER_SERVICE_NAME
            } else {
                val ids = (bookIds?.joinToString(",") ?: "") + (chapterIds?.joinToString(",") ?: "")
                "${DOWNLOADER_SERVICE_NAME}_${ids.hashCode()}"
            }
            
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
            
            // Use KEEP to allow multiple concurrent downloads
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.KEEP,
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
