package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.common.Uri
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.v2.BackupOrchestrator
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
    
    private val backupOrchestrator: BackupOrchestrator by inject()
    private val uiPreferences: UiPreferences by inject()
    private val backupRetentionStorage: BackupRetentionStorage by inject()
    
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
        val intervalSeconds = AutomaticBackupPolicy.interval(frequency)?.inWholeMilliseconds?.div(1000.0) ?: return
        
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
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        if (!AutomaticBackupPolicy.isDue(currentFrequency, uiPreferences.lastBackUpTime().get(), now)) {
            println("[AutoBackup] Backup is not due yet, skipping")
            return
        }

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
        
        // Use the shared filename convention so retention rules work on every platform.
        val backupPath = "$backupDir/${AutomaticBackupPolicy.fileName(now)}"
        
        // Create backup using injected CreateBackup use case
        val uri = Uri.parse(backupPath)
        val result = backupOrchestrator.createBackup(uri, AutomaticBackupPolicy.backupOptions())
        if (!result.isSuccess) {
            throw Exception("Backup file was not created or could not be validated", result.exceptionOrNull())
        }

        uiPreferences.lastBackUpTime().set(now)

        // Apply all retention rules (count, age and total size).
        backupRetentionStorage.rotateAutomaticBackups(BackupRetentionPolicy.fromPreferences(uiPreferences))
        
        println("[AutoBackup] Backup performed at ${NSDate()}")
    }
    
    /**
     * Get the next scheduled backup time
     */
    fun getNextBackupTime(): NSDate? {
        if (!isScheduledFlag) return null
        
        val intervalSeconds = AutomaticBackupPolicy.interval(currentFrequency)?.inWholeMilliseconds?.div(1000.0)
            ?: return null
        
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

/**
 * Swift-callable entry point. Call once from AppDelegate after Koin is initialized.
 */
@OptIn(ExperimentalForeignApi::class)
object IosAutoBackupRegistration : KoinComponent {
    private val uiPreferences: UiPreferences by inject()
    private val scheduleAutomaticBackup: ScheduleAutomaticBackup by inject()

    fun registerBackgroundTasks() {
        registerAutomaticBackupTasks(ScheduleAutomaticBackupImpl())

        val frequency = uiPreferences.automaticBackupTime().get()
        if (frequency != PreferenceValues.AutomaticBackup.Off) {
            scheduleAutomaticBackup.schedule(frequency)
        }
    }
}
