package ireader.presentation.core.ui

import ireader.presentation.ui.settings.backups.GoogleDriveViewModel

/**
 * Desktop stub for OAuth flow - not implemented on desktop
 */
actual fun startOAuthFlow() {
    // Desktop doesn't support Google Drive OAuth yet
    println("Google Drive OAuth is not supported on desktop")
}

actual fun startOAuthFlowWithViewModel(viewModel: GoogleDriveViewModel) {
    // Desktop doesn't support Google Drive OAuth yet
    viewModel.onOAuthError("Google Drive is not supported on desktop")
}

actual fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String) {
    // Desktop doesn't support Google Drive OAuth yet
    viewModel.onOAuthError("Google Drive is not supported on desktop")
}
