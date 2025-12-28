package ireader.presentation.ui.settings.tracking

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ireader.core.log.Log
import java.util.concurrent.TimeUnit

/**
 * Android-specific scheduler for tracking sync using WorkManager.
 */
class AndroidTrackingSyncScheduler(private val context: Context) : TrackingSyncScheduler {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic tracking sync.
     * @param intervalMinutes Interval between syncs in minutes
     * @param requireWifi Whether to only sync over WiFi
     */
    override fun schedule(intervalMinutes: Int, requireWifi: Boolean) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<TrackingSyncWorker>(
                intervalMinutes.toLong(),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                TrackingSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            
            Log.info { "Tracking sync scheduled every $intervalMinutes minutes (WiFi only: $requireWifi)" }
        } catch (e: Exception) {
            Log.error(e, "Failed to schedule tracking sync")
        }
    }
    
    /**
     * Cancel scheduled tracking sync.
     */
    override fun cancel() {
        try {
            workManager.cancelUniqueWork(TrackingSyncWorker.WORK_NAME)
            Log.info { "Tracking sync cancelled" }
        } catch (e: Exception) {
            Log.error(e, "Failed to cancel tracking sync")
        }
    }
    
    /**
     * Check if tracking sync is currently scheduled.
     */
    override fun isScheduled(): Boolean {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(TrackingSyncWorker.WORK_NAME).get()
            workInfos.any { !it.state.isFinished }
        } catch (e: Exception) {
            false
        }
    }
}
