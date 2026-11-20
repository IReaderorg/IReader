package ireader.domain.usecases.backup

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import ireader.domain.models.prefs.PreferenceValues
import java.util.concurrent.TimeUnit

/**
 * Android implementation of automatic backup scheduling using WorkManager.
 */
class ScheduleAutomaticBackupImpl(
    private val context: Context
) : ScheduleAutomaticBackup {
    private val workManager get() = WorkManager.getInstance(context)

    override fun schedule(frequency: PreferenceValues.AutomaticBackup) {
        if (frequency == PreferenceValues.AutomaticBackup.Off) {
            cancel()
            return
        }

        val interval = AutomaticBackupPolicy.interval(frequency)?.inWholeMilliseconds ?: return
        // Do not add a battery constraint: some devices report "low" for long periods
        // and would never run the backup.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresStorageNotLow(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            interval,
            TimeUnit.MILLISECONDS
        )
            // Without an initial delay the periodic worker can fire at the same time as
            // the catch-up request below, producing two backups.
            .setInitialDelay(interval, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(PERIODIC_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // One-time request performs an overdue backup on startup without waiting a full interval.
        // The worker itself checks lastBackUpTime, so repeated scheduling cannot create duplicates.
        val immediateRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
            .setConstraints(constraints)
            .addTag(PERIODIC_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            immediateRequest
        )

        Log.i("ScheduleAutomaticBackup", "Scheduled automatic backup: $frequency")
    }

    override fun cancel() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(IMMEDIATE_WORK_NAME)
        Log.i("ScheduleAutomaticBackup", "Cancelled automatic backup")
    }

    override fun isScheduled(): Boolean = try {
        workManager.getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
            .get()
            .any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    } catch (e: Exception) {
        Log.e("ScheduleAutomaticBackup", "Error checking schedule status", e)
        false
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "automatic_backup"
        private const val IMMEDIATE_WORK_NAME = "automatic_backup_now"
        private const val PERIODIC_TAG = "automatic_backup"
    }
}
