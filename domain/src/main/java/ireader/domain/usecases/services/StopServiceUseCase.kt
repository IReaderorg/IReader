package ireader.domain.usecases.services

import android.content.Context
import androidx.work.WorkManager
import org.koin.core.annotation.Factory

@Factory
class StopServiceUseCase( private val context: Context) {
    operator fun invoke(
        workTag: String
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
    }
}
