package ireader.presentation.ui.update

import ireader.domain.models.update_service_models.Release

/**
 * Interface for checking and downloading app updates
 * Platform-specific implementations handle the actual download and install
 */
interface AppUpdateChecker {
    
    /**
     * Check for available updates
     * @return Result containing the latest Release if available, null if up to date
     */
    suspend fun checkForUpdate(): Result<Release?>
    
    /**
     * Download the APK file
     * @param url Download URL for the APK
     * @param fileName Name for the downloaded file
     * @param onProgress Progress callback (0.0 to 1.0)
     * @param onComplete Callback when download completes with file path
     * @param onError Callback when download fails with error message
     */
    suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
    )
    
    /**
     * Install the downloaded APK
     * @param filePath Path to the downloaded APK file
     */
    fun installApk(filePath: String)
    
    /**
     * Cancel ongoing download
     */
    fun cancelDownload()
}
