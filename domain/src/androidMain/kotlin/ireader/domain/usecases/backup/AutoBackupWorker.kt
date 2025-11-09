package ireader.domain.usecases.backup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.domain.models.common.Uri
import ireader.domain.preferences.prefs.UiPreferences
import java.io.File
import android.net.Uri as AndroidUri

/**
 * AutoBackupWorker - WorkManager worker for automatic backups
 * 
 * This worker is scheduled by ScheduleAutomaticBackupImpl and executes
 * automatic backups in the background according to the user's configured frequency.
 */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val createBackup: CreateBackup,
    private val uiPreferences: UiPreferences
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.i(TAG, "Starting automatic backup")
            
            val maxFiles = uiPreferences.maxAutomaticBackupFiles().get()
            val backupDir = getBackupDirectory()
            
            // Create backup
            val timestamp = System.currentTimeMillis()
            val fileName = "IReader_auto_backup_$timestamp.gz"
            val backupFile = File(backupDir, fileName)
            
            var backupSuccess = false
            
            createBackup.saveTo(
                Uri(AndroidUri.fromFile(backupFile)),
                onError = { error ->
                    Log.e(TAG, "Backup failed: $error")
                },
                onSuccess = {
                    Log.i(TAG, "Backup created: $fileName")
                    backupSuccess = true
                    cleanOldBackups(backupDir, maxFiles)
                    
                    // Update last backup time
                    uiPreferences.lastBackUpTime().set(timestamp)
                },
                currentEvent = { event ->
                    Log.d(TAG, "Backup progress: $event")
                }
            )
            
            if (backupSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup error", e)
            Result.retry()
        }
    }
    
    /**
     * Get or create the backup directory
     */
    private fun getBackupDirectory(): File {
        val backupDir = File(applicationContext.filesDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    /**
     * Clean old backups to maintain only the configured maximum number of files
     */
    private fun cleanOldBackups(backupDir: File, maxFiles: Int) {
        val backupFiles = backupDir.listFiles { file ->
            file.name.startsWith("IReader_auto_backup_")
        }?.sortedByDescending { it.lastModified() } ?: return
        
        // Delete old backups beyond maxFiles
        backupFiles.drop(maxFiles).forEach { file ->
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Deleted old backup: ${file.name}")
            } else {
                Log.w(TAG, "Failed to delete old backup: ${file.name}")
            }
        }
    }
    
    companion object {
        private const val TAG = "AutoBackupWorker"
    }
}
