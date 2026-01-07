package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import ireader.domain.usecases.backup.GoogleDriveOAuthHandler
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.backups.CloudBackupScreen
import ireader.presentation.ui.settings.backups.GoogleDriveViewModel

/**
 * Screen spec for Cloud Backup (Google Drive) that handles OAuth flow
 */
class GoogleDriveBackupScreenSpec {

    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: GoogleDriveViewModel = getIViewModel()
        
        // Check for pending OAuth callback data
        LaunchedEffect(Unit) {
            if (GoogleDriveOAuthHandler.hasPendingData()) {
                val authCode = GoogleDriveOAuthHandler.pendingAuthCode
                val error = GoogleDriveOAuthHandler.pendingError
                
                when {
                    error != null -> {
                        viewModel.onOAuthError("Authentication failed: $error")
                    }
                    authCode != null -> {
                        // Process the auth code - this will be handled by platform-specific code
                        // The ViewModel will call the authenticator to exchange the code for tokens
                        processOAuthCallback(viewModel, authCode)
                    }
                }
            }
        }
        
        CloudBackupScreen(
            onPopBackStack = { navController.safePopBackStack() },
            viewModel = viewModel,
            onStartOAuthFlow = {
                // This callback is triggered when user clicks "Connect" and OAuth flow is needed
                // Platform-specific code will handle the sign-in
                startOAuthFlowWithViewModel(viewModel)
            }
        )
    }
}

/**
 * Platform-specific function to start OAuth flow
 * On Android, this opens the Google Sign-In dialog
 */
expect fun startOAuthFlow()

/**
 * Platform-specific function to start OAuth flow with ViewModel reference
 * On Android, this opens the Google Sign-In dialog and handles the result
 */
expect fun startOAuthFlowWithViewModel(viewModel: GoogleDriveViewModel)

/**
 * Platform-specific function to process OAuth callback
 * On Android, this exchanges the auth code for tokens
 */
expect fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String)
