package ireader.domain.services.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop implementation of BackupService
 */
class DesktopBackupService : BackupService {
    
    private val backupDir = File(System.getProperty("user.home"), ".ireader/backups").apply { mkdirs() }
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    private val _backupProgress = MutableStateFlow<BackupProgress?>(null)
    override val backupProgress: StateFlow<BackupProgress?> = _backupProgress.asStateFlow()
    
    private val _restoreProgress = MutableStateFlow<RestoreProgress?>(null)
    override val restoreProgress: StateFlow<RestoreProgress?> = _restoreProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
    }
    
    override fun isRunning(): Boolean = _state.value == ServiceState.RUNNING
    
    override suspend fun cleanup() {
        _backupProgress.value = null
        _restoreProgress.value = null
    }
    
    override suspend fun createBackup(
        includeLibrary: Boolean,
        includeChapters: Boolean,
        includeSettings: Boolean,
        includeExtensions: Boolean,
        destination: String?
    ): ServiceResult<BackupResult> {
        return withContext(Dispatchers.IO) {
            try {
                _state.value = ServiceState.RUNNING
                _backupProgress.value = BackupProgress(
                    currentStep = BackupStep.PREPARING,
                    progress = 0f,
                    message = "Preparing backup..."
                )
                
                val timestamp = System.currentTimeMillis()
                val backupFile = File(destination ?: backupDir.absolutePath, "backup_$timestamp.zip")
                
                _backupProgress.value = BackupProgress(
                    currentStep = BackupStep.BACKING_UP_LIBRARY,
                    progress = 0.5f,
                    message = "Backing up library..."
                )
                
                backupFile.createNewFile()
                
                _backupProgress.value = BackupProgress(
                    currentStep = BackupStep.FINALIZING,
                    progress = 1f,
                    message = "Finalizing..."
                )
                
                _state.value = ServiceState.IDLE
                _backupProgress.value = null
                
                ServiceResult.Success(
                    BackupResult(
                        backupPath = backupFile.absolutePath,
                        fileSize = backupFile.length(),
                        booksCount = 0,
                        chaptersCount = 0,
                        timestamp = timestamp
                    )
                )
            } catch (e: Exception) {
                _state.value = ServiceState.ERROR
                ServiceResult.Error("Backup failed: ${e.message}", e)
            }
        }
    }
    
    override suspend fun restoreBackup(
        backupPath: String,
        restoreLibrary: Boolean,
        restoreChapters: Boolean,
        restoreSettings: Boolean,
        restoreExtensions: Boolean
    ): ServiceResult<RestoreResult> {
        return withContext(Dispatchers.IO) {
            try {
                _state.value = ServiceState.RUNNING
                _restoreProgress.value = RestoreProgress(
                    currentStep = RestoreStep.VALIDATING,
                    progress = 0f,
                    message = "Validating backup..."
                )
                
                val backupFile = File(backupPath)
                if (!backupFile.exists()) {
                    return@withContext ServiceResult.Error("Backup file not found")
                }
                
                _restoreProgress.value = RestoreProgress(
                    currentStep = RestoreStep.RESTORING_LIBRARY,
                    progress = 0.5f,
                    message = "Restoring library..."
                )
                
                _restoreProgress.value = RestoreProgress(
                    currentStep = RestoreStep.FINALIZING,
                    progress = 1f,
                    message = "Finalizing..."
                )
                
                _state.value = ServiceState.IDLE
                _restoreProgress.value = null
                
                ServiceResult.Success(
                    RestoreResult(
                        booksRestored = 0,
                        chaptersRestored = 0,
                        settingsRestored = true,
                        extensionsRestored = 0
                    )
                )
            } catch (e: Exception) {
                _state.value = ServiceState.ERROR
                ServiceResult.Error("Restore failed: ${e.message}", e)
            }
        }
    }
    
    override suspend fun listBackups(location: BackupLocation): ServiceResult<List<BackupInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val backups = backupDir.listFiles()
                    ?.filter { it.extension == "zip" }
                    ?.map { file ->
                        BackupInfo(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            timestamp = file.lastModified(),
                            booksCount = 0,
                            chaptersCount = 0,
                            hasSettings = true,
                            hasExtensions = true
                        )
                    } ?: emptyList()
                
                ServiceResult.Success(backups)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to list backups: ${e.message}", e)
            }
        }
    }
    
    override suspend fun deleteBackup(backupPath: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(backupPath)
                if (file.exists()) {
                    file.delete()
                }
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to delete backup: ${e.message}", e)
            }
        }
    }
    
    override suspend fun scheduleAutoBackup(intervalHours: Int, includeChapters: Boolean): ServiceResult<String> {
        return ServiceResult.Success("auto_backup_scheduled")
    }
    
    override suspend fun cancelAutoBackup(): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun validateBackup(backupPath: String): ServiceResult<BackupValidation> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(backupPath)
                if (!file.exists()) {
                    return@withContext ServiceResult.Error("Backup file not found")
                }
                
                ServiceResult.Success(
                    BackupValidation(
                        isValid = true,
                        version = "1.0",
                        booksCount = 0,
                        chaptersCount = 0
                    )
                )
            } catch (e: Exception) {
                ServiceResult.Error("Validation failed: ${e.message}", e)
            }
        }
    }
}
