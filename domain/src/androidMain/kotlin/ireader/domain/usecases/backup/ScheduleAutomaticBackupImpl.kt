package ireader.domain.usecases.backup

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import ireader.domain.models.prefs.PreferenceValues
import java.util.concurrent.TimeUnit

/**
 * Android implementation of automatic backup scheduling using WorkManager
 */
class ScheduleAutomaticBackupImpl(
    private val context: Context
) : ScheduleAutomaticBackup {
    
    override fun schedule(frequency: PreferenceValues.AutomaticBackup) {
        if (frequency == PreferenceValues.AutomaticBackup.Off) {
            cancel()
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        
        val repeatInterval = when (frequency) {
            PreferenceValues.AutomaticBackup.Every6Hours -> 6L to TimeUnit.HOURS
            PreferenceValues.AutomaticBackup.Every12Hours -> 12L to TimeUnit.HOURS
            PreferenceValues.AutomaticBackup.Daily -> 1L to TimeUnit.DAYS
            PreferenceValues.AutomaticBackup.Every2Days -> 2L to TimeUnit.DAYS
            PreferenceValues.AutomaticBackup.Weekly -> 7L to TimeUnit.DAYS
            else -> return
        }
        
        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            repeatInterval.first,
            repeatInterval.second
        )
            .setConstraints(constraints)
            .addTag("automatic_backup")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "automatic_backup",
                ExistingPeriodicWorkPolicy.REPLACE,
                backupRequest
            )
        
        Log.i("ScheduleAutomaticBackup", "Scheduled automatic backup: $frequency")
    }
    
    override fun cancel() {
        WorkManager.getInstance(context)
            .cancelUniqueWork("automatic_backup")
        
        Log.i("ScheduleAutomaticBackup", "Cancelled automatic backup")
    }
    
    override fun isScheduled(): Boolean {
        return try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork("automatic_backup")
                .get()
            workInfos.any { workInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED || 
                workInfo.state == WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            Log.e("ScheduleAutomaticBackup", "Error checking schedule status", e)
            false
        }
    }
}
