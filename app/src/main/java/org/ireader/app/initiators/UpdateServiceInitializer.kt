package org.ireader.app.initiators

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ireader.app.BuildConfig
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.domain.services.update_service.UpdateService
import javax.inject.Inject

class UpdateServiceInitializer @Inject constructor(app: Application, appPreferences: AppPreferences) {


    init {
        if (appPreferences.appUpdater().get() && !BuildConfig.DEBUG && !BuildConfig.PREVIEW) {
            val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()
            val manager = WorkManager.getInstance(app)
            manager.enqueue(updateRequest)
        }
    }
}
