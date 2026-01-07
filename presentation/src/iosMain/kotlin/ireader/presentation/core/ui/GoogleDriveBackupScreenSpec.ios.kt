package ireader.presentation.core.ui

import ireader.presentation.ui.settings.backups.GoogleDriveViewModel

/**
 * iOS stub for OAuth flow - not implemented on iOS yet
 */
actual fun startOAuthFlow() {
    // iOS doesn't support Google Drive OAuth yet
    println("Google Drive OAuth is not supported on iOS")
}

actual fun startOAuthFlowWithViewModel(viewModel: GoogleDriveViewModel) {
    // iOS doesn't support Google Drive OAuth yet
    viewModel.onOAuthError("Google Drive is not supported on iOS")
}

actual fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String) {
    // iOS doesn't support Google Drive OAuth yet
    viewModel.onOAuthError("Google Drive is not supported on iOS")
}
