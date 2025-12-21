package ireader.presentation.ui.update

import androidx.compose.runtime.Stable
import ireader.core.log.Log
import ireader.domain.models.update_service_models.Release
import ireader.domain.models.update_service_models.ReleaseAsset
import ireader.domain.preferences.prefs.AppPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * State for the app update screen
 */
@Stable
data class AppUpdateState(
    val isLoading: Boolean = false,
    val release: Release? = null,
    val currentVersion: String = "",
    val newVersion: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val apkAsset: ReleaseAsset? = null,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val isConnecting: Boolean = false, // True during HTTP connection phase before actual download starts
    val isDownloaded: Boolean = false,
    val downloadedFilePath: String? = null,
    val error: String? = null,
    val shouldShowDialog: Boolean = false,
)

/**
 * ViewModel for managing app updates with modern UI
 */
class AppUpdateViewModel(
    private val appPreferences: AppPreferences,
    private val updateChecker: AppUpdateChecker,
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(AppUpdateState())
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "AppUpdateViewModel"
        private const val REMIND_LATER_DAYS = 7L
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    init {
        loadCurrentVersion()
    }
    
    private fun loadCurrentVersion() {
        val currentVersion = ireader.i18n.BuildKonfig.VERSION_NAME
        _state.value = _state.value.copy(currentVersion = currentVersion)
    }
    
    /**
     * Check for updates and determine if dialog should be shown
     */
    fun checkForUpdates() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = updateChecker.checkForUpdate()
                
                result.onSuccess { release ->
                    if (release != null && shouldShowUpdateDialog(release)) {
                        val apkAsset = findApkAsset(release.assets)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            release = release,
                            newVersion = release.tag_name ?: "",
                            releaseNotes = parseReleaseNotes(release.body),
                            downloadUrl = release.html_url,
                            apkAsset = apkAsset,
                            shouldShowDialog = true,
                        )
                        Log.info { "$TAG: Update available: ${release.tag_name}" }
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            shouldShowDialog = false,
                        )
                        Log.info { "$TAG: No update available or dialog suppressed" }
                    }
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message,
                    )
                    Log.error("$TAG: Failed to check for updates", error)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message,
                )
                Log.error("$TAG: Exception checking for updates", e)
            }
        }
    }
    
    /**
     * Determine if update dialog should be shown based on user preferences
     */
    @OptIn(ExperimentalTime::class)
    private fun shouldShowUpdateDialog(release: Release): Boolean {
        val tagName = release.tag_name ?: return false
        val currentVersion = _state.value.currentVersion
        
        // Check if this is actually a newer version
        if (!ireader.domain.models.update_service_models.Version.isNewVersion(tagName, currentVersion)) {
            return false
        }
        
        // Check if user skipped this version
        val skippedVersion = appPreferences.skippedUpdateVersion().get()
        if (skippedVersion == tagName) {
            Log.info { "$TAG: User skipped version $tagName" }
            return false
        }
        
        // Check if "remind me later" is still active
        val remindLaterTime = appPreferences.updateRemindLaterTime().get()
        if (remindLaterTime > 0) {
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val daysSinceRemind = (now - remindLaterTime) / MILLIS_PER_DAY
            if (daysSinceRemind < REMIND_LATER_DAYS) {
                Log.info { "$TAG: Remind later active, $daysSinceRemind days since last remind" }
                return false
            }
        }
        
        // Check if update dialog is enabled
        if (!appPreferences.showUpdateDialog().get()) {
            return false
        }
        
        return true
    }
    
    /**
     * Find the APK asset from release assets
     */
    private fun findApkAsset(assets: List<ReleaseAsset>): ReleaseAsset? {
        return assets.find { asset ->
            asset.name.endsWith(".apk") && 
            !asset.name.contains("debug", ignoreCase = true)
        } ?: assets.find { it.name.endsWith(".apk") }
    }
    
    /**
     * Parse and format release notes from GitHub markdown
     */
    private fun parseReleaseNotes(body: String?): String {
        if (body.isNullOrBlank()) return ""
        
        return body
            .replace(Regex("^#+\\s*"), "") // Remove markdown headers
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // Remove bold
            .replace(Regex("\\*(.+?)\\*"), "$1") // Remove italic
            .replace(Regex("^-\\s*", RegexOption.MULTILINE), "• ") // Convert list items
            .replace(Regex("^\\*\\s*", RegexOption.MULTILINE), "• ")
            .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1") // Remove links but keep text
            .trim()
    }
    
    /**
     * User clicked "Remind me later"
     */
    @OptIn(ExperimentalTime::class)
    fun remindLater() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        appPreferences.updateRemindLaterTime().set(now)
        _state.value = _state.value.copy(shouldShowDialog = false)
        Log.info { "$TAG: User chose remind later" }
    }
    
    /**
     * User clicked "Skip this version"
     */
    fun skipVersion() {
        val version = _state.value.newVersion
        if (version.isNotEmpty()) {
            appPreferences.skippedUpdateVersion().set(version)
        }
        _state.value = _state.value.copy(shouldShowDialog = false)
        Log.info { "$TAG: User skipped version $version" }
    }
    
    /**
     * Dismiss the update dialog
     */
    fun dismissDialog() {
        _state.value = _state.value.copy(shouldShowDialog = false)
    }
    
    /**
     * Start downloading the APK
     */
    fun startDownload() {
        val asset = _state.value.apkAsset ?: return
        
        scope.launch {
            _state.value = _state.value.copy(
                isDownloading = true,
                downloadProgress = 0f,
                error = null,
            )
            
            try {
                updateChecker.downloadApk(
                    url = asset.browserDownloadUrl,
                    fileName = asset.name,
                    onProgress = { progress ->
                        _state.value = _state.value.copy(downloadProgress = progress)
                    },
                    onComplete = { filePath ->
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            isDownloaded = true,
                            downloadedFilePath = filePath,
                        )
                        Log.info { "$TAG: Download complete: $filePath" }
                    },
                    onError = { error ->
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            error = error,
                        )
                        Log.error { "$TAG: Download failed: $error" }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDownloading = false,
                    error = e.message,
                )
                Log.error("$TAG: Download exception", e)
            }
        }
    }
    
    /**
     * Install the downloaded APK
     */
    fun installApk() {
        val filePath = _state.value.downloadedFilePath ?: return
        
        try {
            updateChecker.installApk(filePath)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
            Log.error("$TAG: Install failed", e)
        }
    }
    
    /**
     * Cancel ongoing download
     */
    fun cancelDownload() {
        updateChecker.cancelDownload()
        _state.value = _state.value.copy(
            isDownloading = false,
            downloadProgress = 0f,
        )
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
