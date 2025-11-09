package ireader.presentation.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.settings.backups.CloudBackupScreen
import ireader.presentation.ui.settings.backups.CloudBackupViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class CloudBackupScreenSpec : VoyagerScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: CloudBackupViewModel = getIViewModel()

        val backupCreator = koinInject<ireader.domain.usecases.backup.CreateBackup>()
        
        val selectedProvider by viewModel.selectedProvider
        val isAuthenticated by viewModel.isAuthenticated
        val cloudBackups by viewModel.cloudBackups.collectAsState()
        val errorMessage by viewModel.errorMessage
        val successMessage by viewModel.successMessage
        val isLoading by viewModel.isLoading
        
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        // Show error/success messages
        LaunchedEffect(errorMessage) {
            errorMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessages()
            }
        }
        
        LaunchedEffect(successMessage) {
            successMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessages()
            }
        }
        
        CloudBackupScreen(
            onPopBackStack = { popBackStack(navigator) },
            selectedProvider = selectedProvider,
            isAuthenticated = isAuthenticated,
            cloudBackups = cloudBackups,
            onProviderSelected = { provider ->
                viewModel.selectProvider(provider)
            },
            onAuthenticate = {
                viewModel.authenticate()
            },
            onSignOut = {
                viewModel.signOut()
            },
            onUploadBackup = {
                if (isLoading) return@CloudBackupScreen
                
                scope.launch {
                    try {
                        // Create a temporary backup file
                        val timestamp = System.currentTimeMillis()
                        val fileName = "IReader_cloud_backup_$timestamp.proto.gz"
                        
                        // Get platform-specific temp directory
                        val tempDir = getTempBackupDirectory()
                        val backupFilePath = "$tempDir/$fileName"
                        
                        // Create the backup file
                        val uri = ireader.domain.models.common.Uri.parse(backupFilePath)
                        var backupSuccess = false
                        var errorMsg: String? = null
                        
                        backupCreator.saveTo(
                            uri = uri,
                            onError = { error ->
                                errorMsg = error.toString()
                            },
                            onSuccess = {
                                backupSuccess = true
                            },
                            currentEvent = { /* Progress updates */ }
                        )
                        
                        if (backupSuccess) {
                            // Upload the backup to cloud
                            viewModel.uploadBackup(backupFilePath, fileName)
                        } else {
                            snackbarHostState.showSnackbar(
                                errorMsg ?: "Failed to create backup file"
                            )
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            "Failed to create backup: ${e.message}"
                        )
                    }
                }
            },
            onDownloadBackup = { backup ->
                if (isLoading) return@CloudBackupScreen
                
                scope.launch {
                    try {
                        // Get platform-specific download directory
                        val downloadDir = getDownloadDirectory()
                        val localFilePath = "$downloadDir/${backup.fileName}"
                        
                        viewModel.downloadBackup(backup.fileName, localFilePath)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            "Failed to download backup: ${e.message}"
                        )
                    }
                }
            },
            onDeleteBackup = { backup ->
                viewModel.deleteBackup(backup)
            }
        )
    }
    
    /**
     * Get platform-specific temporary directory for backup files
     */
    private fun getTempBackupDirectory(): String {
        return try {
            // Try to get system temp directory
            System.getProperty("java.io.tmpdir") ?: "/tmp"
        } catch (e: Exception) {
            "/tmp"
        }
    }
    
    /**
     * Get platform-specific download directory
     */
    private fun getDownloadDirectory(): String {
        return try {
            // Try to get user home directory
            val userHome = System.getProperty("user.home") ?: "."
            "$userHome/Downloads/IReader"
        } catch (e: Exception) {
            "./downloads"
        }
    }
}
