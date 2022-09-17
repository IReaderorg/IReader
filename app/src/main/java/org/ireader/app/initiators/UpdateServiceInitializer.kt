package org.ireader.app.initiators

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ireader.app.BuildConfig
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.update_service.UpdateService
import org.koin.core.annotation.Factory

@Factory
class UpdateServiceInitializer(app: Application, appPreferences: AppPreferences) {

    init {
        if (appPreferences.appUpdater().get() && !BuildConfig.DEBUG && !BuildConfig.PREVIEW) {
            val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()
            val manager = WorkManager.getInstance(app)
            manager.enqueue(updateRequest)
        }
    }
}
