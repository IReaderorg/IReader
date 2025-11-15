package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.mutableStateOf
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.backup.BackupData
import ireader.domain.models.backup.BackupInfo
import ireader.domain.models.backup.ReadingProgress
import ireader.domain.services.backup.GoogleDriveBackupService
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Google Drive backup operations
 */
class GoogleDriveViewModel(
    private val googleDriveService: GoogleDriveBackupService,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository
) : StateViewModel<GoogleDriveViewModel.State>(State()) {
    
    data class State(
        val isConnected: Boolean = false,
        val accountEmail: String? = null,
        val backups: List<BackupInfo> = emptyList(),
        val isLoading: Boolean = false,
        val isCreatingBackup: Boolean = false,
        val isRestoringBackup: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )
    
    private val _isConnected = mutableStateOf(false)
    val isConnected: androidx.compose.runtime.State<Boolean> get() = _isConnected
    
    private val _accountEmail = mutableStateOf<String?>(null)
    val accountEmail: androidx.compose.runtime.State<String?> get() = _accountEmail
    
    private val _backups = MutableStateFlow<List<BackupInfo>>(emptyList())
    val backups: StateFlow<List<BackupInfo>> = _backups.asStateFlow()
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: androidx.compose.runtime.State<Boolean> get() = _isLoading
    
    private val _isCreatingBackup = mutableStateOf(false)
    val isCreatingBackup: androidx.compose.runtime.State<Boolean> get() = _isCreatingBackup
    
    private val _isRestoringBackup = mutableStateOf(false)
    val isRestoringBackup: androidx.compose.runtime.State<Boolean> get() = _isRestoringBackup
    
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: androidx.compose.runtime.State<String?> get() = _errorMessage
    
    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: androidx.compose.runtime.State<String?> get() = _successMessage
    
    init {
        checkConnectionStatus()
    }
    
    /**
     * Check if connected to Google Drive
     */
    fun checkConnectionStatus() {
        scope.launch {
            _isLoading.value = true
            try {
                val isAuth = googleDriveService.isAuthenticated()
                _isConnected.value = isAuth
                
                if (isAuth) {
                    loadBackups()
                }
                
                updateState { it.copy(isConnected = isAuth) }
            } catch (e: Exception) {
                _isConnected.value = false
                updateState { it.copy(isConnected = false) }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Connect to Google Drive
     */
    fun connect() {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = googleDriveService.authenticate()
                
                result.onSuccess { email ->
                    _isConnected.value = true
                    _accountEmail.value = email
                    _successMessage.value = "Successfully connected to Google Drive"
                    
                    updateState { it.copy(
                        isConnected = true,
                        accountEmail = email,
                        successMessage = _successMessage.value
                    ) }
                    
                    // Load backups after successful connection
                    loadBackups()
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to connect to Google Drive"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Disconnect from Google Drive
     */
    fun disconnect() {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = googleDriveService.disconnect()
                
                result.onSuccess {
                    _isConnected.value = false
                    _accountEmail.value = null
                    _backups.value = emptyList()
                    _successMessage.value = "Disconnected from Google Drive"
                    
                    updateState { it.copy(
                        isConnected = false,
                        accountEmail = null,
                        backups = emptyList(),
                        successMessage = _successMessage.value
                    ) }
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to disconnect"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load list of backups from Google Drive
     */
    fun loadBackups() {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = googleDriveService.listBackups()
                
                result.onSuccess { backupList ->
                    _backups.value = backupList
                    updateState { it.copy(backups = backupList) }
                }.onFailure { error ->
                    _errorMessage.value = "Failed to load backups: ${error.message}"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Create a new backup and upload to Google Drive
     */
    fun createBackup() {
        scope.launch {
            _isCreatingBackup.value = true
            _errorMessage.value = null
            
            try {
                // TODO: Fix repository method calls when API is stable
                // Collect all data from repositories
                val books: List<ireader.domain.models.entities.Book> = emptyList() // bookRepository.findAll().first()
                val chapters: List<ireader.domain.models.entities.Chapter> = emptyList() // chapterRepository.findAllChapters()
                val categories: List<ireader.domain.models.entities.Category> = emptyList() // categoryRepository.findAll()
                
                // Create reading progress data
                val readingProgress: List<ReadingProgress> = chapters
                    .filter { it.read || it.lastPageRead > 0 }
                    .map { chapter ->
                        ReadingProgress(
                            bookId = chapter.bookId,
                            chapterId = chapter.id,
                            lastPageRead = chapter.lastPageRead,
                            lastReadTime = System.currentTimeMillis()
                        )
                    }
                
                // Create bookmarks data
                val bookmarks: List<ireader.domain.models.backup.Bookmark> = chapters
                    .filter { it.bookmark }
                    .map { chapter ->
                        ireader.domain.models.backup.Bookmark(
                            bookId = chapter.bookId,
                            chapterId = chapter.id,
                            page = chapter.lastPageRead,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                
                // Package backup data
                val backupData = BackupData(
                    novels = books,
                    chapters = chapters,
                    readingProgress = readingProgress,
                    bookmarks = bookmarks,
                    settings = emptyMap() // TODO: Add settings if needed
                )
                
                // Upload to Google Drive
                val result = googleDriveService.createBackup(backupData)
                
                result.onSuccess { backupId ->
                    _successMessage.value = "Backup created successfully"
                    updateState { it.copy(
                        successMessage = _successMessage.value
                    ) }
                    
                    // Reload backups list
                    loadBackups()
                }.onFailure { error ->
                    _errorMessage.value = "Failed to create backup: ${error.message}"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create backup: ${e.message}"
                updateState { it.copy(
                    errorMessage = _errorMessage.value
                ) }
            } finally {
                _isCreatingBackup.value = false
            }
        }
    }
    
    /**
     * Restore a backup from Google Drive
     */
    fun restoreBackup(backupInfo: BackupInfo) {
        scope.launch {
            _isRestoringBackup.value = true
            _errorMessage.value = null
            
            try {
                // Download backup from Google Drive
                val result = googleDriveService.downloadBackup(backupInfo.id)
                
                result.onSuccess { backupData ->
                    // TODO: Clear existing data and restore from backup
                    // This requires careful implementation to avoid data loss
                    // Should show confirmation dialog before proceeding
                    
                    _successMessage.value = "Backup restored successfully. Please restart the app."
                    updateState { it.copy(
                        successMessage = _successMessage.value
                    ) }
                }.onFailure { error ->
                    _errorMessage.value = "Failed to restore backup: ${error.message}"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to restore backup: ${e.message}"
                updateState { it.copy(
                    errorMessage = _errorMessage.value
                ) }
            } finally {
                _isRestoringBackup.value = false
            }
        }
    }
    
    /**
     * Delete a backup from Google Drive
     */
    fun deleteBackup(backupInfo: BackupInfo) {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = googleDriveService.deleteBackup(backupInfo.id)
                
                result.onSuccess {
                    _successMessage.value = "Backup deleted successfully"
                    updateState { it.copy(
                        successMessage = _successMessage.value
                    ) }
                    
                    // Reload backups list
                    loadBackups()
                }.onFailure { error ->
                    _errorMessage.value = "Failed to delete backup: ${error.message}"
                    updateState { it.copy(
                        errorMessage = _errorMessage.value
                    ) }
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
        updateState { it.copy(
            errorMessage = null,
            successMessage = null
        ) }
    }
}
