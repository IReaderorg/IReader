package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.data.repository.SourceCredentialsRepository
import ireader.domain.usecases.backup.CloudBackupFile
import ireader.domain.usecases.backup.CloudBackupManager
import ireader.domain.usecases.backup.CloudProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing cloud backup state and operations
 */
class CloudBackupViewModel(
    private val cloudBackupManager: CloudBackupManager,
    private val credentialsRepository: SourceCredentialsRepository
) : StateScreenModel<CloudBackupViewModel.State>(State()) {

    data class State(
        val selectedProvider: CloudProvider? = null,
        val isAuthenticated: Boolean = false,
        val cloudBackups: List<CloudBackupFile> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    private val _selectedProvider = mutableStateOf<CloudProvider?>(null)
    val selectedProvider: androidx.compose.runtime.State<CloudProvider?> get() = _selectedProvider

    private val _isAuthenticated = mutableStateOf(false)
    val isAuthenticated: androidx.compose.runtime.State<Boolean> get() = _isAuthenticated

    private val _cloudBackups = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupFile>> = _cloudBackups.asStateFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: androidx.compose.runtime.State<Boolean> get() = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: androidx.compose.runtime.State<String?> get() = _errorMessage

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: androidx.compose.runtime.State<String?> get() = _successMessage

    /**
     * Select a cloud provider
     */
    fun selectProvider(provider: CloudProvider) {
        screenModelScope.launch {
            _selectedProvider.value = provider
            _isAuthenticated.value = false
            _cloudBackups.value = emptyList()
            
            // Check if already authenticated
            checkAuthentication(provider)
            
            mutableState.value = state.value.copy(
                selectedProvider = provider,
                isAuthenticated = _isAuthenticated.value,
                cloudBackups = emptyList()
            )
        }
    }

    /**
     * Check if authenticated with the selected provider
     */
    private suspend fun checkAuthentication(provider: CloudProvider) {
        try {
            val isAuth = cloudBackupManager.isAuthenticated(provider)
            _isAuthenticated.value = isAuth
            
            // If authenticated, load backups
            if (isAuth) {
                loadCloudBackups()
            }
        } catch (e: Exception) {
            _isAuthenticated.value = false
        }
    }

    /**
     * Authenticate with the selected provider
     */
    fun authenticate() {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = cloudBackupManager.authenticate(provider)
                
                result.onSuccess {
                    _isAuthenticated.value = true
                    _successMessage.value = "Successfully connected to ${getProviderName(provider)}"
                    
                    // Save authentication status to credentials repository
                    // Using a special sourceId for cloud providers
                    val sourceId = when (provider) {
                        CloudProvider.GOOGLE_DRIVE -> -1L
                        CloudProvider.DROPBOX -> -2L
                        CloudProvider.LOCAL -> -3L
                    }
                    credentialsRepository.storeCredentials(
                        sourceId = sourceId,
                        username = "authenticated",
                        password = System.currentTimeMillis().toString()
                    )
                    
                    // Load backups after successful authentication
                    loadCloudBackups()
                    
                    mutableState.value = state.value.copy(
                        isAuthenticated = true,
                        successMessage = _successMessage.value
                    )
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Authentication failed"
                    mutableState.value = state.value.copy(
                        errorMessage = _errorMessage.value
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign out from the selected provider
     */
    fun signOut() {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = cloudBackupManager.signOut(provider)
                
                result.onSuccess {
                    _isAuthenticated.value = false
                    _cloudBackups.value = emptyList()
                    _successMessage.value = "Signed out from ${getProviderName(provider)}"
                    
                    // Remove credentials from repository
                    val sourceId = when (provider) {
                        CloudProvider.GOOGLE_DRIVE -> -1L
                        CloudProvider.DROPBOX -> -2L
                        CloudProvider.LOCAL -> -3L
                    }
                    credentialsRepository.removeCredentials(sourceId)
                    
                    mutableState.value = state.value.copy(
                        isAuthenticated = false,
                        cloudBackups = emptyList(),
                        successMessage = _successMessage.value
                    )
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Sign out failed"
                    mutableState.value = state.value.copy(
                        errorMessage = _errorMessage.value
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load cloud backups from the selected provider
     */
    fun loadCloudBackups() {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = cloudBackupManager.listCloudBackups(provider)
                
                result.onSuccess { backups ->
                    _cloudBackups.value = backups
                    mutableState.value = state.value.copy(
                        cloudBackups = backups
                    )
                }.onFailure { error ->
                    _errorMessage.value = "Failed to load backups: ${error.message}"
                    mutableState.value = state.value.copy(
                        errorMessage = _errorMessage.value
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Upload a backup to cloud storage
     */
    fun uploadBackup(localFilePath: String, fileName: String) {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = cloudBackupManager.uploadToCloud(provider, localFilePath, fileName)
                
                when (result) {
                    is ireader.domain.models.BackupResult.Success -> {
                        val timestamp = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(result.timestamp))
                        _successMessage.value = "Backup uploaded successfully at $timestamp"
                        // Reload backups to show the new one
                        loadCloudBackups()
                        mutableState.value = state.value.copy(
                            successMessage = _successMessage.value
                        )
                    }
                    is ireader.domain.models.BackupResult.Error -> {
                        _errorMessage.value = result.message
                        mutableState.value = state.value.copy(
                            errorMessage = result.message
                        )
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Download a backup from cloud storage
     */
    fun downloadBackup(cloudFileName: String, localFilePath: String) {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = cloudBackupManager.downloadFromCloud(provider, cloudFileName, localFilePath)
                
                when (result) {
                    is ireader.domain.models.BackupResult.Success -> {
                        _successMessage.value = "Backup downloaded successfully to ${result.filePath}"
                        mutableState.value = state.value.copy(
                            successMessage = _successMessage.value
                        )
                    }
                    is ireader.domain.models.BackupResult.Error -> {
                        _errorMessage.value = result.message
                        mutableState.value = state.value.copy(
                            errorMessage = result.message
                        )
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a backup from cloud storage
     */
    fun deleteBackup(backup: CloudBackupFile) {
        val provider = _selectedProvider.value ?: return
        
        screenModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val storageProvider = cloudBackupManager.getProvider(provider)
                val result = storageProvider?.deleteBackup(backup.fileName)
                
                result?.onSuccess {
                    _successMessage.value = "Backup deleted successfully"
                    // Reload backups to reflect the deletion
                    loadCloudBackups()
                    mutableState.value = state.value.copy(
                        successMessage = _successMessage.value
                    )
                }?.onFailure { error ->
                    _errorMessage.value = "Failed to delete backup: ${error.message}"
                    mutableState.value = state.value.copy(
                        errorMessage = _errorMessage.value
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        mutableState.value = state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun getProviderName(provider: CloudProvider): String {
        return when (provider) {
            CloudProvider.GOOGLE_DRIVE -> "Google Drive"
            CloudProvider.DROPBOX -> "Dropbox"
            CloudProvider.LOCAL -> "Local"
        }
    }
}
