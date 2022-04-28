package org.ireader.domain.use_cases.services

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.domain.services.downloaderService.DownloaderService
import org.ireader.domain.services.downloaderService.DownloaderService.Companion.DOWNLOADER_MODE
import org.ireader.domain.utils.toast
import javax.inject.Inject

class StartDownloadServicesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        bookIds: LongArray? = null,
        chapterIds: LongArray? = null,
        downloadModes: Boolean = false,
    ) {

        try {
            val work = OneTimeWorkRequestBuilder<DownloaderService>().apply {
                setInputData(
                    Data.Builder().apply {
                        bookIds?.let { bookIds ->
                            putLongArray(DownloaderService.DOWNLOADER_BOOKS_IDS, bookIds)
                        }
                        chapterIds?.let { chapterIds ->
                            putLongArray(DownloaderService.DOWNLOADER_Chapters_IDS, chapterIds)
                        }
                        if (downloadModes) {
                            putBoolean(DOWNLOADER_MODE, true)
                        }
                    }.build()
                )
                addTag(DownloaderService.DOWNLOADER_SERVICE_NAME)
            }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                DownloaderService.DOWNLOADER_SERVICE_NAME,
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
