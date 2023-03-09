package ireader.domain.usecases.services

import android.content.Context
import androidx.work.WorkManager



actual class StopServiceUseCase( private val context: Context) {
    actual operator fun invoke(
        workTag: String
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
    }
}
