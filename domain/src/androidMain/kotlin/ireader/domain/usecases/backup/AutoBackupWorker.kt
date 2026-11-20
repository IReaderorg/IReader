package ireader.domain.usecases.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ireader.domain.notification.NotificationsIds.CHANNEL_BACKUP_RESTORE_PROGRESS
import ireader.domain.notification.NotificationsIds.ID_BACKUP_PROGRESS
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.v2.BackupOrchestrator
import ireader.domain.usecases.backup.v2.BackupProgress
import ireader.domain.utils.DrawableResources
import java.util.concurrent.atomic.AtomicBoolean

/**
 * WorkManager worker that performs automatic backups in the background.
 *
 * Uses a foreground notification so the user can see progress while the app is
 * in the background. Backups go to `<selected IReader folder>/Backups/Automatic`
 * through the same SAF folder used for Extensions/js-plugins.
 * `lastBackUpTime` is only updated after the file was written and verified.
 */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val backupOrchestrator: BackupOrchestrator,
    private val uiPreferences: UiPreferences,
    private val autoBackupStorage: AutoBackupStorage
) : CoroutineWorker(context, params) {

    @Volatile
    private var progressText = "Preparing backup…"

    @Volatile
    private var finished = false

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = buildNotification()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                ID_BACKUP_PROGRESS,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(ID_BACKUP_PROGRESS, notification)
        }
    }

    override suspend fun doWork(): Result {
        if (!RUNNING.compareAndSet(false, true)) {
            Log.i(TAG, "Another automatic backup is already running, skipping")
            return Result.success()
        }

        try {
            val frequency = uiPreferences.automaticBackupTime().get()
            val now = System.currentTimeMillis()

            if (!AutomaticBackupPolicy.isDue(frequency, uiPreferences.lastBackUpTime().get(), now)) {
                Log.i(TAG, "Backup is not due yet, skipping")
                return Result.success()
            }

            // Keep the worker alive and visible while a large backup is being written.
            runCatching { setForeground(getForegroundInfo()) }
                .onFailure { Log.w(TAG, "Could not start foreground notification", it) }

            val fileName = AutomaticBackupPolicy.fileName(now)
            val uri = try {
                autoBackupStorage.createBackupUri(fileName)
            } catch (e: Exception) {
                Log.e(TAG, "Could not create backup destination", e)
                finished = true
                updateNotification("Backup failed")
                return Result.retry()
            }

            val result = backupOrchestrator.createBackup(
                uri = uri,
                options = AutomaticBackupPolicy.backupOptions()
            ) { progress ->
                progressText = progress.toNotificationText()
                Log.d(TAG, "Backup progress: $progressText")
                updateNotification(progressText)
            }

            autoBackupStorage.finishBackup(uri, result.isSuccess)
            if (!result.isSuccess) {
                Log.e(TAG, "Backup file was not created or could not be validated", result.exceptionOrNull())
                finished = true
                updateNotification("Backup failed")
                return Result.retry()
            }

            uiPreferences.lastBackUpTime().set(now)
            autoBackupStorage.rotateAutomaticBackups(
                BackupRetentionPolicy.fromPreferences(uiPreferences)
            )
            finished = true
            updateNotification("Backup completed")
            Log.i(TAG, "Backup created in Backups/Automatic: $fileName")
            return Result.success()
        } finally {
            RUNNING.set(false)
        }
    }

    private fun updateNotification(text: String = progressText) {
        progressText = text
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return
        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(ID_BACKUP_PROGRESS, buildNotification())
        }.onFailure { Log.w(TAG, "Could not update backup notification", it) }
    }

    private fun buildNotification(): android.app.Notification {
        ensureChannel()
        return NotificationCompat.Builder(applicationContext, CHANNEL_BACKUP_RESTORE_PROGRESS)
            .setContentTitle("IReader automatic backup")
            .setContentText(progressText)
            .setSmallIcon(DrawableResources.ic_downloading)
            .setOngoing(!finished)
            .setOnlyAlertOnce(true)
            .setProgress(if (finished) 100 else 0, 100, !finished)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_BACKUP_RESTORE_PROGRESS) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BACKUP_RESTORE_PROGRESS,
                "Backup progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
        )
    }

    private fun BackupProgress.toNotificationText(): String = when (this) {
        is BackupProgress.Collecting -> "Collecting library…"
        is BackupProgress.Serializing -> "Backing up $bookName ($bookIndex/$totalBooks)"
        is BackupProgress.Compressing -> "Compressing backup…"
        is BackupProgress.Writing -> "Writing backup file…"
        is BackupProgress.Verifying -> "Verifying backup…"
        is BackupProgress.Complete -> "Backup completed"
    }

    companion object {
        private const val TAG = "AutoBackupWorker"
        private val RUNNING = AtomicBoolean(false)
    }
}
