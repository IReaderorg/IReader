package ireader.presentation.core.ui

import ireader.domain.services.backup.GoogleDriveBackupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object GoogleDriveOAuthLauncher : KoinComponent {
    private val googleDriveService: GoogleDriveBackupService by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    actual fun launchOAuthFlow(
        onSuccess: (email: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        scope.launch {
            try {
                val result = googleDriveService.authenticate()
                withContext(Dispatchers.Main) {
                    result.onSuccess { email ->
                        onSuccess(email)
                    }.onFailure { error ->
                        onError(error.message ?: "Authentication failed")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Authentication error")
                }
            }
        }
    }
}
