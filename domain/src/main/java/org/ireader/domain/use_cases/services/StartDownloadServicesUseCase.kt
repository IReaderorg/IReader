package org.ireader.domain.use_cases.services

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.domain.services.downloaderService.DownloadService
import org.ireader.domain.services.downloaderService.DownloadService.Companion.DOWNLOADER_MODE
import org.ireader.domain.utils.toast
import javax.inject.Inject

class StartDownloadServicesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        bookIds: LongArray? = null,
        chapterIds: LongArray? = null,
        downloadModes: Boolean = false,
    ) {

        try {
            val work = OneTimeWorkRequestBuilder<DownloadService>().apply {
                setInputData(
                    Data.Builder().apply {
                        bookIds?.let { bookIds ->
                            putLongArray(DownloadService.DOWNLOADER_BOOKS_IDS, bookIds)
                        }
                        chapterIds?.let { chapterIds ->
                            putLongArray(DownloadService.DOWNLOADER_Chapters_IDS, chapterIds)
                        }
                        if (downloadModes) {
                            putBoolean(DOWNLOADER_MODE, true)
                        }
                    }.build()
                )
                addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
            }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                DownloadService.DOWNLOADER_SERVICE_NAME,
                ExistingWorkPolicy.REPLACE,
                work
            )
        } catch (e: IllegalStateException) {
            context.toast(e.localizedMessage)

        } catch (e: Exception) {
            context.toast(e.localizedMessage)
        }
    }

}












