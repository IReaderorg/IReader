package ireader.domain.js.update

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Android implementation of plugin update scheduler using WorkManager.
 */
actual class JSPluginUpdateScheduler(
    private val context: Context,
    private val updateChecker: JSPluginUpdateChecker,
    private val updateNotifier: JSPluginUpdateNotifier
) {
    
    companion object {
        private const val WORK_NAME = "js_plugin_update_check"
        const val KEY_UPDATE_CHECKER = "update_checker"
        const val KEY_UPDATE_NOTIFIER = "update_notifier"
    }
    
    actual fun schedulePeriodicCheck(intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val updateRequest = PeriodicWorkRequestBuilder<PluginUpdateWorker>(
            intervalHours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }
    
    actual fun cancelPeriodicCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    
    actual fun isScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}

/**
 * Worker that performs plugin update checks.
 * Note: This requires dependency injection setup for Workers.
 * Dependencies should be injected via WorkManager's WorkerFactory.
 */
class PluginUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Get dependencies from Koin or DI container
            // This is a simplified implementation - in production, use WorkerFactory
            // to inject JSPluginUpdateChecker and JSPluginUpdateNotifier
            
            // For now, return success as the actual implementation
            // requires proper DI setup in the app module
            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            Result.retry()
        }
    }
}
