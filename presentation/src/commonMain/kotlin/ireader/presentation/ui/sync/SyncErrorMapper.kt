package ireader.presentation.ui.sync

import ireader.domain.models.sync.SyncError

/**
 * Maps SyncError domain models to user-friendly error messages and actionable suggestions.
 * 
 * This mapper provides:
 * - Clear, non-technical error messages
 * - Actionable suggestions for resolving the error
 * - Consistent error messaging across the app
 * 
 * Error Message Standards:
 * - Use present tense for error descriptions ("WiFi connection lost", not "WiFi connection was lost")
 * - Keep messages concise and user-friendly (avoid technical jargon)
 * - Provide specific context when available (versions, storage amounts, device names)
 * - Suggestions should be actionable and start with verbs ("Check...", "Ensure...", "Try...")
 * - Use null for suggestions when no action is needed (e.g., Cancelled errors)
 * 
 * TODO: Standardize error messages across other features (Quote, CharacterArt) to follow
 * the same patterns established here. Consider extracting to a shared ErrorMessageFormatter
 * utility if error mapping becomes common across multiple features.
 * 
 * Following TDD methodology - tests written first before implementation.
 */
object SyncErrorMapper {
    
    /**
     * Data class containing error message and optional suggestion.
     */
    data class ErrorInfo(
        val message: String,
        val suggestion: String? = null
    )
    
    /**
     * Map a SyncError to user-friendly error message and suggestion.
     * 
     * @param error The sync error to map
     * @param deviceName Optional device name for context
     * @return ErrorInfo containing message and suggestion
     */
    fun mapError(error: SyncError, deviceName: String? = null): ErrorInfo {
        return when (error) {
            is SyncError.NetworkUnavailable -> ErrorInfo(
                message = "WiFi connection lost",
                suggestion = "Check your WiFi connection and try again"
            )
            
            is SyncError.ConnectionFailed -> ErrorInfo(
                message = "Failed to connect to device",
                suggestion = "Ensure both devices are on the same WiFi network"
            )
            
            is SyncError.AuthenticationFailed -> ErrorInfo(
                message = "Device authentication failed",
                suggestion = "Try pairing with the device again"
            )
            
            is SyncError.IncompatibleVersion -> ErrorInfo(
                message = "App versions are incompatible (local: ${error.localVersion}, remote: ${error.remoteVersion})",
                suggestion = "Update the app on both devices to the latest version"
            )
            
            is SyncError.TransferFailed -> ErrorInfo(
                message = "Data transfer failed: ${error.message}",
                suggestion = "Check your WiFi connection and try again"
            )
            
            is SyncError.ConflictResolutionFailed -> ErrorInfo(
                message = "Failed to resolve data conflicts",
                suggestion = "Try resolving conflicts manually in settings"
            )
            
            is SyncError.InsufficientStorage -> {
                val requiredMB = error.required / (1024 * 1024)
                val availableMB = error.available / (1024 * 1024)
                ErrorInfo(
                    message = "Insufficient storage space (need ${requiredMB}MB, have ${availableMB}MB)",
                    suggestion = "Free up storage space and try again"
                )
            }
            
            is SyncError.DeviceNotFound -> ErrorInfo(
                message = "Device not found or no longer available",
                suggestion = "Ensure the device is still on the network and try again"
            )
            
            is SyncError.Cancelled -> ErrorInfo(
                message = "Sync operation was cancelled",
                suggestion = null
            )
            
            is SyncError.Unknown -> ErrorInfo(
                message = error.message,
                suggestion = "Please try again or contact support if the problem persists"
            )
        }
    }
}
