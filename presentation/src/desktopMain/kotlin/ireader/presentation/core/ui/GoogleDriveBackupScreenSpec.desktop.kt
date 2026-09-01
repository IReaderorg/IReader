package ireader.presentation.core.ui

import ireader.presentation.ui.settings.backups.GoogleDriveViewModel

/**
 * Desktop implementation for OAuth flow
 */
actual fun startOAuthFlow() {
}

actual fun startOAuthFlowWithViewModel(viewModel: GoogleDriveViewModel) {
    GoogleDriveOAuthLauncher.launchOAuthFlow(viewModel::onOAuthSuccess, viewModel::onOAuthError)
}

actual fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String) {
    GoogleDriveOAuthLauncher.launchOAuthFlow(viewModel::onOAuthSuccess, viewModel::onOAuthError)
}

