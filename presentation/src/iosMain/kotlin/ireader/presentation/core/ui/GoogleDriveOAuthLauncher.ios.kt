package ireader.presentation.core.ui

actual object GoogleDriveOAuthLauncher {
    actual fun launchOAuthFlow(
        onSuccess: (email: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        onError("Google Drive is not supported on iOS")
    }
}
