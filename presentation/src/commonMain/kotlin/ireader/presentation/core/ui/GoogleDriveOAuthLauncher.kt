package ireader.presentation.core.ui

/**
 * Unified platform-agnostic launcher for Google Drive OAuth flows.
 * Handles Android Activity-based Sign-In and Desktop Browser-based Loopback OAuth.
 */
expect object GoogleDriveOAuthLauncher {
    fun launchOAuthFlow(
        onSuccess: (email: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    )
}
