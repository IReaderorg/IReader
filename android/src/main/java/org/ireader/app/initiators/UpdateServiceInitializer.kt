package org.ireader.app.initiators

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ireader.app.BuildConfig
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.update_service.UpdateService
import ireader.core.log.Log



class UpdateServiceInitializer(app: Application, appPreferences: AppPreferences) {

    init {
        val appUpdaterEnabled = appPreferences.appUpdater().get()
        val isDebug = BuildConfig.DEBUG
        val isPreview = BuildConfig.PREVIEW
        
        Log.info { "UpdateServiceInitializer: appUpdater=$appUpdaterEnabled, DEBUG=$isDebug, PREVIEW=$isPreview" }
        
        if (appUpdaterEnabled && !isDebug && !isPreview) {
            Log.info { "UpdateServiceInitializer: Scheduling update check" }
            val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()
            val manager = WorkManager.getInstance(app)
            manager.enqueue(updateRequest)
        } else {
            Log.info { "UpdateServiceInitializer: Update check skipped - " +
                when {
                    !appUpdaterEnabled -> "app updater is disabled in settings"
                    isDebug -> "running in DEBUG mode"
                    isPreview -> "running in PREVIEW mode"
                    else -> "unknown reason"
                }
            }
        }
    }
}
