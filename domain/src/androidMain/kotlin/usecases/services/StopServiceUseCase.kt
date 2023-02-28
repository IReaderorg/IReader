package ireader.domain.usecases.services

import android.content.Context
import androidx.work.WorkManager



class StopServiceUseCase( private val context: Context) {
    operator fun invoke(
        workTag: String
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
    }
}
