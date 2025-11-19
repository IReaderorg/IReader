package ireader.core.update

import ireader.core.log.IReaderLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.ExperimentalTime

/**
 * App update checker for automatic update detection and management
 */
@OptIn(ExperimentalTime::class)
class AppUpdateChecker(
    private val currentVersion: String,
    private val updateRepository: UpdateRepository
) {
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private var lastCheckTime: kotlin.time.Instant? = null
    private val minCheckIntervalMs = 6L * 60 * 60 * 1000 // 6 hours
    
    /**
     * Check for available updates
     */
    suspend fun checkForUpdates(force: Boolean = false): UpdateCheckResult {
        // Check if we should skip based on last check time
        if (!force && lastCheckTime != null) {
            val timeSinceLastCheck = kotlin.time.Clock.System.now().toEpochMilliseconds() -
                lastCheckTime!!.toEpochMilliseconds()
            if (timeSinceLastCheck < minCheckIntervalMs) {
                IReaderLog.info("Skipping update check, last check was recent", tag = "AppUpdate")
                return UpdateCheckResult.SkippedRecentCheck
            }
        }
        
        _updateState.value = UpdateState.Checking
        lastCheckTime = kotlin.time.Clock.System.now()
        
        return try {
            val latestVersion = updateRepository.fetchLatestVersion()
            
            if (isNewerVersion(latestVersion.versionName, currentVersion)) {
                val updateInfo = UpdateInfo(
                    versionName = latestVersion.versionName,
                    versionCode = latestVersion.versionCode,
                    releaseNotes = latestVersion.releaseNotes,
                    downloadUrl = latestVersion.downloadUrl,
                    fileSize = latestVersion.fileSize,
                    isRequired = latestVersion.isRequired,
                    releaseDate = latestVersion.releaseDate
                )
                
                _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                IReaderLog.info(
                    "Update available: ${latestVersion.versionName}",
                    tag = "AppUpdate"
                )
                UpdateCheckResult.UpdateAvailable(updateInfo)
            } else {
                _updateState.value = UpdateState.UpToDate
                IReaderLog.info("App is up to date", tag = "AppUpdate")
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            IReaderLog.error("Error checking for updates", e, tag = "AppUpdate")
            UpdateCheckResult.Error(e)
        }
    }
    
    /**
     * Start downloading an update
     */
    suspend fun downloadUpdate(updateInfo: UpdateInfo): DownloadResult {
        _updateState.value = UpdateState.Downloading(0f)
        
        return try {
            updateRepository.downloadUpdate(
                url = updateInfo.downloadUrl,
                onProgress = { progress ->
                    _updateState.value = UpdateState.Downloading(progress)
                }
            )
            
            _updateState.value = UpdateState.Downloaded
            IReaderLog.info("Update downloaded successfully", tag = "AppUpdate")
            DownloadResult.Success
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Download failed")
            IReaderLog.error("Error downloading update", e, tag = "AppUpdate")
            DownloadResult.Error(e)
        }
    }
    
    /**
     * Install the downloaded update
     */
    suspend fun installUpdate(): InstallResult {
        return try {
            updateRepository.installUpdate()
            IReaderLog.info("Update installation initiated", tag = "AppUpdate")
            InstallResult.Success
        } catch (e: Exception) {
            IReaderLog.error("Error installing update", e, tag = "AppUpdate")
            InstallResult.Error(e)
        }
    }
    
    /**
     * Cancel update download
     */
    fun cancelDownload() {
        updateRepository.cancelDownload()
        _updateState.value = UpdateState.Idle
        IReaderLog.info("Update download cancelled", tag = "AppUpdate")
    }
    
    /**
     * Reset update state
     */
    fun reset() {
        _updateState.value = UpdateState.Idle
        lastCheckTime = null
    }
    
    /**
     * Compare version strings to determine if new version is newer
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(newParts.size, currentParts.size)) {
            val newPart = newParts.getOrNull(i) ?: 0
            val currentPart = currentParts.getOrNull(i) ?: 0
            
            when {
                newPart > currentPart -> return true
                newPart < currentPart -> return false
            }
        }
        
        return false
    }
}

/**
 * Update state sealed class
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    object Downloaded : UpdateState()
    data class Error(val message: String) : UpdateState()
}

/**
 * Update check result sealed class
 */
sealed class UpdateCheckResult {
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object SkippedRecentCheck : UpdateCheckResult()
    data class Error(val exception: Throwable) : UpdateCheckResult()
}

/**
 * Download result sealed class
 */
sealed class DownloadResult {
    object Success : DownloadResult()
    data class Error(val exception: Throwable) : DownloadResult()
}

/**
 * Install result sealed class
 */
sealed class InstallResult {
    object Success : InstallResult()
    data class Error(val exception: Throwable) : InstallResult()
}

/**
 * Update information data class
 */
data class UpdateInfo @OptIn(ExperimentalTime::class) constructor(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val isRequired: Boolean,
    val releaseDate: kotlin.time.Instant
)

/**
 * Update repository interface
 */
interface UpdateRepository {
    suspend fun fetchLatestVersion(): UpdateInfo
    suspend fun downloadUpdate(url: String, onProgress: (Float) -> Unit)
    suspend fun installUpdate()
    fun cancelDownload()
}
