package org.ireader.domain.use_cases.services

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopServiceUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        workTag: String
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
    }
}
