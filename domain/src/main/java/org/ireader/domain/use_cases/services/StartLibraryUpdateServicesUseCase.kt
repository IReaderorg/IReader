package org.ireader.domain.use_cases.services

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.domain.services.library_update_service.LibraryUpdatesService
import javax.inject.Inject

class StartLibraryUpdateServicesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        forceUpdate:Boolean = false
    ) {

        val work =
            OneTimeWorkRequestBuilder<LibraryUpdatesService>().apply {
                addTag(LibraryUpdatesService.LibraryUpdateTag)
                setInputData(
                    Data.Builder().apply {
                        putBoolean(LibraryUpdatesService.FORCE_UPDATE, forceUpdate)
                    }.build()
                )
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LibraryUpdatesService.LibraryUpdateTag,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}
