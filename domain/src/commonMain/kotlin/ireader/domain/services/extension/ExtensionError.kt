package ireader.domain.services.extension

/**
 * Sealed class representing all possible errors in extension operations.
 * Used for type-safe error handling across the Extension Controller.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
sealed class ExtensionError {
    /**
     * Failed to load extensions from the catalog store.
     */
    data class LoadFailed(val message: String) : ExtensionError()
    
    /**
     * Failed to install an extension.
     */
    data class InstallFailed(val pkgName: String, val message: String) : ExtensionError()
    
    /**
     * Failed to uninstall an extension.
     */
    data class UninstallFailed(val pkgName: String, val message: String) : ExtensionError()
    
    /**
     * Failed to update an extension.
     */
    data class UpdateFailed(val pkgName: String, val message: String) : ExtensionError()
    
    /**
     * Network error occurred during extension operations.
     */
    data class NetworkError(val message: String) : ExtensionError()
    
    /**
     * Failed to check for updates.
     */
    data class CheckUpdatesFailed(val message: String) : ExtensionError()
    
    /**
     * Failed to refresh extensions from remote.
     */
    data class RefreshFailed(val message: String) : ExtensionError()
    
    /**
     * Returns a user-friendly error message.
     * Requirements: 4.4
     */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load extensions: $message"
        is InstallFailed -> "Failed to install $pkgName: $message"
        is UninstallFailed -> "Failed to uninstall $pkgName: $message"
        is UpdateFailed -> "Failed to update $pkgName: $message"
        is NetworkError -> "Network error: $message"
        is CheckUpdatesFailed -> "Failed to check for updates: $message"
        is RefreshFailed -> "Failed to refresh extensions: $message"
    }
}
