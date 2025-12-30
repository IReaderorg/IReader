package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.common.Uri
import platform.BackgroundTasks.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS implementation of automatic backup scheduling using BGTaskScheduler
 * 
 * Uses iOS Background Tasks framework for scheduling periodic backups.
 * Injects CreateBackup use case via Koin for actual backup functionality.
 * 
 * ## Limitations
 * - iOS limits background execution time
 * - Tasks may be deferred by the system based on battery, network, etc.
 * - Minimum interval is approximately 15 minutes (system enforced)
 * 
 * ## Info.plist Requirements
 * Add to BGTaskSchedulerPermittedIdentifiers:
 * - com.ireader.backup.automatic
 * - com.ireader.backup.refresh
 * 
 * ## Usage
 * Register background tasks in AppDelegate:
 * ```swift
 * registerAutomaticBackupTasks(backupScheduler)
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
class ScheduleAutomaticBackupImpl : ScheduleAutomaticBackup, KoinComponent {
    
    private val createBackup: CreateBackup by inject()
    
    private var backupJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        const val BACKUP_TASK_ID = "com.ireader.backup.automatic"
        const val BACKUP_REFRESH_TASK_ID = "com.ireader.backup.refresh"
        
        // Store current frequency for background task handler
        private var currentFrequency: PreferenceValues.AutomaticBackup = PreferenceValues.AutomaticBackup.Off
        private var isScheduledFlag = false
    }
    
    override fun schedule(frequency: PreferenceValues.AutomaticBackup) {
        // Cancel any existing scheduled backup
        cancel()
        
        if (frequency == PreferenceValues.AutomaticBackup.Off) {
            return
        }
        
        currentFrequency = frequency
        isScheduledFlag = true
        
        // Calculate interval in seconds
        val intervalSeconds = when (frequency) {
            PreferenceValues.AutomaticBackup.Every6Hours -> 6.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Every12Hours -> 12.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Daily -> 24.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Every2Days -> 48.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Weekly -> 7.0 * 24.0 * 60.0 * 60.0
            else -> return
        }
        
        scheduleBackgroundTask(intervalSeconds)
        println("[AutoBackup] Scheduled automatic backup: $frequency (interval: ${intervalSeconds}s)")
    }
    
    override fun cancel() {
        backupJob?.cancel()
        backupJob = null
        isScheduledFlag = false
        currentFrequency = PreferenceValues.AutomaticBackup.Off
        
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(BACKUP_TASK_ID)
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(BACKUP_REFRESH_TASK_ID)
        
        println("[AutoBackup] Cancelled automatic backup")
    }
    
    override fun isScheduled(): Boolean {
        return isScheduledFlag
    }
    
    /**
     * Schedule a background processing task
     */
    private fun scheduleBackgroundTask(intervalSeconds: Double) {
        // Try BGProcessingTaskRequest first (longer execution time)
        val processingRequest = BGProcessingTaskRequest(identifier = BACKUP_TASK_ID).apply {
            requiresNetworkConnectivity = false
            requiresExternalPower = false
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(intervalSeconds)
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(processingRequest, null)
            println("[AutoBackup] Background processing task scheduled")
        } catch (e: Exception) {
            println("[AutoBackup] Failed to schedule processing task: ${e.message}")
            // Fallback to app refresh task
            scheduleRefreshTask(intervalSeconds)
        }
    }
    
    /**
     * Fallback: Schedule a background app refresh task
     */
    private fun scheduleRefreshTask(intervalSeconds: Double) {
        val refreshRequest = BGAppRefreshTaskRequest(identifier = BACKUP_REFRESH_TASK_ID).apply {
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(intervalSeconds)
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(refreshRequest, null)
            println("[AutoBackup] Background refresh task scheduled")
        } catch (e: Exception) {
            println("[AutoBackup] Failed to schedule refresh task: ${e.message}")
        }
    }
    
    /**
     * Handle background task execution
     * Called by the system when the background task runs
     */
    fun handleBackgroundTask(task: BGTask) {
        // Set expiration handler
        task.setExpirationHandler {
            backupJob?.cancel()
            println("[AutoBackup] Background task expired")
        }
        
        backupJob = scope.launch {
            try {
                println("[AutoBackup] Starting automatic backup...")
                performBackup()
                task.setTaskCompletedWithSuccess(true)
                println("[AutoBackup] Automatic backup completed successfully")
            } catch (e: CancellationException) {
                task.setTaskCompletedWithSuccess(false)
                throw e
            } catch (e: Exception) {
                println("[AutoBackup] Automatic backup failed: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
            
            // Reschedule for next interval
            if (currentFrequency != PreferenceValues.AutomaticBackup.Off) {
                schedule(currentFrequency)
            }
        }
    }
    
    /**
     * Perform the actual backup using CreateBackup use case
     */
    private suspend fun performBackup() {
        // Get backup directory
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: throw Exception("Cannot find documents directory")
        
        val backupDir = "$documentsDir/backups"
        val fileManager = NSFileManager.defaultManager
        
        // Create backups directory if needed
        if (!fileManager.fileExistsAtPath(backupDir)) {
            fileManager.createDirectoryAtPath(
                backupDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        
        // Generate backup filename with timestamp
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd_HH-mm-ss"
        }
        val timestamp = dateFormatter.stringFromDate(NSDate())
        val backupPath = "$backupDir/ireader_backup_$timestamp.ireader"
        
        // Create backup using injected CreateBackup use case
        val uri = Uri(backupPath)
        var backupError: String? = null
        
        createBackup.saveTo(
            uri = uri,
            onError = { error -> 
                backupError = error.toString()
            },
            onSuccess = {
                println("[AutoBackup] Backup saved to: $backupPath")
            },
            currentEvent = { event ->
                println("[AutoBackup] $event")
            }
        )
        
        if (backupError != null) {
            throw Exception("Backup failed: $backupError")
        }
        
        // Clean up old backups (keep last 5)
        cleanupOldBackups(backupDir, keepCount = 5)
        
        println("[AutoBackup] Backup performed at ${NSDate()}")
    }
    
    /**
     * Remove old backup files, keeping only the most recent ones
     */
    private fun cleanupOldBackups(backupDir: String, keepCount: Int) {
        try {
            val fileManager = NSFileManager.defaultManager
            val contents = fileManager.contentsOfDirectoryAtPath(backupDir, null) as? List<String> ?: return
            
            val backupFiles = contents
                .filter { it.endsWith(".ireader") }
                .sortedDescending() // Most recent first (due to timestamp naming)
            
            if (backupFiles.size > keepCount) {
                backupFiles.drop(keepCount).forEach { filename ->
                    val filePath = "$backupDir/$filename"
                    fileManager.removeItemAtPath(filePath, null)
                    println("[AutoBackup] Removed old backup: $filename")
                }
            }
        } catch (e: Exception) {
            println("[AutoBackup] Failed to cleanup old backups: ${e.message}")
        }
    }
    
    /**
     * Get the next scheduled backup time
     */
    fun getNextBackupTime(): NSDate? {
        if (!isScheduledFlag) return null
        
        val intervalSeconds = when (currentFrequency) {
            PreferenceValues.AutomaticBackup.Every6Hours -> 6.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Every12Hours -> 12.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Daily -> 24.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Every2Days -> 48.0 * 60.0 * 60.0
            PreferenceValues.AutomaticBackup.Weekly -> 7.0 * 24.0 * 60.0 * 60.0
            else -> return null
        }
        
        return NSDate.dateWithTimeIntervalSinceNow(intervalSeconds)
    }
}

/**
 * Register automatic backup background tasks
 * 
 * Call this in AppDelegate.didFinishLaunchingWithOptions:
 * ```swift
 * registerAutomaticBackupTasks(backupScheduler)
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
fun registerAutomaticBackupTasks(backupScheduler: ScheduleAutomaticBackupImpl) {
    // Register processing task
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = ScheduleAutomaticBackupImpl.BACKUP_TASK_ID,
        usingQueue = null
    ) { task ->
        if (task != null) {
            backupScheduler.handleBackgroundTask(task)
        }
    }
    
    // Register refresh task (fallback)
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = ScheduleAutomaticBackupImpl.BACKUP_REFRESH_TASK_ID,
        usingQueue = null
    ) { task ->
        if (task != null) {
            backupScheduler.handleBackgroundTask(task)
        }
    }
    
    println("[AutoBackup] Background tasks registered")
}
