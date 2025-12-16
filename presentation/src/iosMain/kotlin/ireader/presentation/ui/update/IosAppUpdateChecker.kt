package ireader.presentation.ui.update

import ireader.core.log.Log
import ireader.domain.models.update_service_models.Release

/**
 * iOS implementation of AppUpdateChecker
 * iOS apps are updated through the App Store
 */
class IosAppUpdateChecker : AppUpdateChecker {
    
    companion object {
        private const val TAG = "IosAppUpdateChecker"
    }
    
    override suspend fun checkForUpdate(): Result<Release?> {
        // iOS apps are updated through App Store
        Log.info { "$TAG: iOS updates handled by App Store" }
        return Result.success(null)
    }
    
    override suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        // Not applicable for iOS
        onError("iOS apps are updated through the App Store")
    }
    
    override fun installApk(filePath: String) {
        // Not applicable for iOS
    }
    
    override fun cancelDownload() {
        // No-op for iOS
    }
}
