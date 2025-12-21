package ireader.presentation.core

import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.update.AppUpdateChecker
import ireader.presentation.ui.update.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class ScreenContentViewModel(
    private val uiPreferences: UiPreferences,
    private val appPreferences: AppPreferences,
    private val appUpdateChecker: AppUpdateChecker,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var showHistory = uiPreferences.showHistoryInButtonBar().asState()
    var confirmExit = uiPreferences.confirmExit().asState()
    
    // App update state
    private val _appUpdateState = MutableStateFlow(AppUpdateState())
    val appUpdateState: StateFlow<AppUpdateState> = _appUpdateState.asStateFlow()
    
    // Platform-specific download event handler
    private val downloadEventHandler = createDownloadEventHandler(_appUpdateState)
    
    companion object {
        private const val TAG = "ScreenContentViewModel"
        private const val REMIND_LATER_DAYS = 7L
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    init {
        checkForAppUpdates()
    }
    
    /**
     * Check for app updates on startup
     */
    private fun checkForAppUpdates() {
        if (!appPreferences.appUpdater().get()) return
        
        scope.launch {
            _appUpdateState.value = _appUpdateState.value.copy(
                isLoading = true,
                currentVersion = ireader.i18n.BuildKonfig.VERSION_NAME
            )
            
            try {
                val result = appUpdateChecker.checkForUpdate()
                
                result.onSuccess { release ->
                    if (release != null && shouldShowUpdateDialog(release.tag_name)) {
                        val apkAsset = release.assets.find { asset ->
                            asset.name.endsWith(".apk") && 
                            !asset.name.contains("debug", ignoreCase = true)
                        } ?: release.assets.find { it.name.endsWith(".apk") }
                        
                        _appUpdateState.value = _appUpdateState.value.copy(
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
                        _appUpdateState.value = _appUpdateState.value.copy(
                            isLoading = false,
                            shouldShowDialog = false,
                        )
                        Log.info { "$TAG: No update needed or dialog conditions not met" }
                    }
                }.onFailure { error ->
                    _appUpdateState.value = _appUpdateState.value.copy(
                        isLoading = false,
                        error = error.message,
                    )
                    Log.error("$TAG: Failed to check for updates", error)
                }
            } catch (e: Exception) {
                _appUpdateState.value = _appUpdateState.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun shouldShowUpdateDialog(tagName: String?): Boolean {
        if (tagName == null) return false
        
        val currentVersion = _appUpdateState.value.currentVersion
        
        // Check if this is actually a newer version
        if (!ireader.domain.models.update_service_models.Version.isNewVersion(tagName, currentVersion)) {
            return false
        }
        
        // Check if user skipped this version
        val skippedVersion = appPreferences.skippedUpdateVersion().get()
        if (skippedVersion == tagName) {
            return false
        }
        
        // Check if "remind me later" is still active
        val remindLaterTime = appPreferences.updateRemindLaterTime().get()
        if (remindLaterTime > 0) {
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val daysSinceRemind = (now - remindLaterTime) / MILLIS_PER_DAY
            if (daysSinceRemind < REMIND_LATER_DAYS) {
                return false
            }
        }
        
        return true
    }
    
    private fun parseReleaseNotes(body: String?): String {
        if (body.isNullOrBlank()) return ""
        
        return body
            .replace(Regex("^#+\\s*"), "")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("^-\\s*", RegexOption.MULTILINE), "• ")
            .replace(Regex("^\\*\\s*", RegexOption.MULTILINE), "• ")
            .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")
            .trim()
    }
    
    /**
     * User clicked "Remind me later"
     */
    @OptIn(ExperimentalTime::class)
    fun remindLater() {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        appPreferences.updateRemindLaterTime().set(now)
        _appUpdateState.value = _appUpdateState.value.copy(shouldShowDialog = false)
        Log.info { "$TAG: User chose remind later" }
    }
    
    /**
     * User clicked "Skip this version"
     */
    fun skipVersion() {
        val version = _appUpdateState.value.newVersion
        if (version.isNotEmpty()) {
            appPreferences.skippedUpdateVersion().set(version)
        }
        _appUpdateState.value = _appUpdateState.value.copy(shouldShowDialog = false)
        Log.info { "$TAG: User skipped version $version" }
    }
    
    /**
     * Dismiss the update dialog
     */
    fun dismissUpdateDialog() {
        _appUpdateState.value = _appUpdateState.value.copy(shouldShowDialog = false)
    }
    
    /**
     * Start downloading the APK
     */
    fun startDownload() {
        val asset = _appUpdateState.value.apkAsset ?: return
        
        // Immediately set connecting state for instant UI feedback
        _appUpdateState.value = _appUpdateState.value.copy(
            isConnecting = true,
            isDownloading = true,
            downloadProgress = 0f,
            error = null,
        )
        
        scope.launch {
            try {
                Log.info { "$TAG: Starting download for ${asset.name}" }
                
                appUpdateChecker.downloadApk(
                    url = asset.browserDownloadUrl,
                    fileName = asset.name,
                    onProgress = { progress ->
                        // This will be handled by broadcast receiver on Android
                        // On other platforms, we handle it here
                        _appUpdateState.value = _appUpdateState.value.copy(
                            isConnecting = false,
                            downloadProgress = progress
                        )
                    },
                    onComplete = { filePath ->
                        // Note: On Android, this will be handled by the broadcast receiver
                        // On other platforms, we handle it here
                        _appUpdateState.value = _appUpdateState.value.copy(
                            isConnecting = false,
                            isDownloading = false,
                            isDownloaded = true,
                            downloadedFilePath = filePath,
                        )
                        Log.info { "$TAG: Download complete: $filePath" }
                    },
                    onError = { error ->
                        // Note: On Android, this will be handled by the broadcast receiver
                        // On other platforms, we handle it here
                        _appUpdateState.value = _appUpdateState.value.copy(
                            isConnecting = false,
                            isDownloading = false,
                            error = error,
                        )
                        Log.error { "$TAG: Download failed: $error" }
                    }
                )
            } catch (e: Exception) {
                _appUpdateState.value = _appUpdateState.value.copy(
                    isConnecting = false,
                    isDownloading = false,
                    error = e.message,
                )
                Log.error("$TAG: Failed to start download", e)
            }
        }
    }
    
    /**
     * Install the downloaded APK
     */
    fun installApk() {
        val filePath = _appUpdateState.value.downloadedFilePath ?: return
        
        try {
            appUpdateChecker.installApk(filePath)
        } catch (e: Exception) {
            _appUpdateState.value = _appUpdateState.value.copy(error = e.message)
        }
    }
    
    /**
     * Cancel ongoing download
     */
    fun cancelDownload() {
        appUpdateChecker.cancelDownload()
        _appUpdateState.value = _appUpdateState.value.copy(
            isConnecting = false,
            isDownloading = false,
            downloadProgress = 0f,
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        downloadEventHandler.cleanup()
    }
}

/**
 * Platform-specific interface for handling download events
 */
expect class DownloadEventHandler(updateState: MutableStateFlow<AppUpdateState>) {
    fun cleanup()
}

/**
 * Create platform-specific download event handler
 */
expect fun createDownloadEventHandler(updateState: MutableStateFlow<AppUpdateState>): DownloadEventHandler
