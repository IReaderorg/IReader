package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.backup.BackupData
import ireader.domain.models.backup.BackupInfo
import ireader.domain.models.backup.ReadingProgress
import ireader.domain.services.backup.GoogleDriveBackupService
import ireader.domain.usecases.backup.GoogleDriveOAuthHandler
import ireader.domain.usecases.book.BookUseCases
import ireader.domain.usecases.category.CategoryUseCases
import ireader.domain.usecases.chapter.ChapterUseCases
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

class GoogleDriveViewModel(
    private val googleDriveService: GoogleDriveBackupService,
    private val bookUseCases: BookUseCases,
    private val chapterUseCases: ChapterUseCases,
    private val categoryUseCases: CategoryUseCases,
    private val insertUseCases: LocalInsertUseCases
) : StateViewModel<GoogleDriveViewModel.State>(State()) {
    
    data class State(
        val isConnected: Boolean = false,
        val accountEmail: String? = null,
        val backups: List<BackupInfo> = emptyList(),
        val isLoading: Boolean = false,
        val isCreatingBackup: Boolean = false,
        val isRestoringBackup: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val needsOAuthFlow: Boolean = false
    )
    
    private val _isConnected = mutableStateOf(false)
    val isConnected: androidx.compose.runtime.State<Boolean> get() = _isConnected
    
    private val _accountEmail = mutableStateOf<String?>(null)
    val accountEmail: androidx.compose.runtime.State<String?> get() = _accountEmail
    
    private val _backups = MutableStateFlow<List<BackupInfo>>(emptyList())
    val backups: StateFlow<List<BackupInfo>> = _backups.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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
    
    private val _needsOAuthFlow = mutableStateOf(false)
    val needsOAuthFlow: androidx.compose.runtime.State<Boolean> get() = _needsOAuthFlow
    
    init {
        checkConnectionStatus()
        checkPendingOAuthCallback()
    }

    private fun checkPendingOAuthCallback() {
        if (GoogleDriveOAuthHandler.hasPendingData()) {
            val error = GoogleDriveOAuthHandler.pendingError
            if (error != null) {
                _errorMessage.value = "Authentication failed: $error"
                GoogleDriveOAuthHandler.clear()
            }
        }
    }
    
    fun checkConnectionStatus() {
        scope.launch {
            _isLoading.value = true
            try {
                val isAuth = googleDriveService.isAuthenticated()
                _isConnected.value = isAuth
                if (isAuth) loadBackups()
                updateState { it.copy(isConnected = isAuth) }
            } catch (e: Exception) {
                _isConnected.value = false
                updateState { it.copy(isConnected = false) }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
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
                    _needsOAuthFlow.value = false
                    updateState { it.copy(isConnected = true, accountEmail = email, needsOAuthFlow = false) }
                    loadBackups()
                }.onFailure { error ->
                    val message = error.message ?: "Failed to connect"
                    if (message.contains("must be initiated from UI", ignoreCase = true)) {
                        _needsOAuthFlow.value = true
                        updateState { it.copy(needsOAuthFlow = true) }
                    } else {
                        _errorMessage.value = message
                        updateState { it.copy(errorMessage = message) }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun onOAuthSuccess(email: String) {
        _isConnected.value = true
        _accountEmail.value = email
        _successMessage.value = "Successfully connected to Google Drive"
        _needsOAuthFlow.value = false
        updateState { it.copy(isConnected = true, accountEmail = email, needsOAuthFlow = false) }
        GoogleDriveOAuthHandler.clear()
        loadBackups()
    }
    
    fun onOAuthError(error: String) {
        _errorMessage.value = error
        _needsOAuthFlow.value = false
        updateState { it.copy(errorMessage = error, needsOAuthFlow = false) }
        GoogleDriveOAuthHandler.clear()
    }
    
    fun clearOAuthFlowFlag() {
        _needsOAuthFlow.value = false
        updateState { it.copy(needsOAuthFlow = false) }
    }
    
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
                    updateState { it.copy(isConnected = false, accountEmail = null, backups = emptyList()) }
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to disconnect"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createBackup() {
        scope.launch {
            _isCreatingBackup.value = true
            _errorMessage.value = null
            try {
                val books = bookUseCases.getBooksInLibrary()
                val allChapters = mutableListOf<ireader.domain.models.entities.Chapter>()
                books.forEach { book ->
                    val chapters = chapterUseCases.getChaptersByBookId(book.id)
                    allChapters.addAll(chapters)
                }
                
                val readingProgress = allChapters
                    .filter { it.read || it.lastPageRead > 0 }
                    .map { chapter ->
                        ReadingProgress(
                            bookId = chapter.bookId,
                            chapterId = chapter.id,
                            lastPageRead = chapter.lastPageRead,
                            lastReadTime = currentTimeToLong()
                        )
                    }
                
                val bookmarks = allChapters
                    .filter { it.bookmark }
                    .map { chapter ->
                        ireader.domain.models.backup.Bookmark(
                            bookId = chapter.bookId,
                            chapterId = chapter.id,
                            page = chapter.lastPageRead,
                            timestamp = currentTimeToLong()
                        )
                    }
                
                val backupData = BackupData(
                    novels = books,
                    chapters = allChapters,
                    readingProgress = readingProgress,
                    bookmarks = bookmarks,
                    settings = mutableMapOf()
                )
                
                val result = googleDriveService.createBackup(backupData)
                result.onSuccess {
                    _successMessage.value = "Backup created successfully"
                    loadBackups()
                }.onFailure { error ->
                    _errorMessage.value = "Failed to create backup: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create backup: ${e.message}"
            } finally {
                _isCreatingBackup.value = false
            }
        }
    }

    fun restoreBackup(backupInfo: BackupInfo, confirmed: Boolean = false) {
        scope.launch {
            _isRestoringBackup.value = true
            _errorMessage.value = null
            try {
                val result = googleDriveService.downloadBackup(backupInfo.id)
                result.onSuccess { backupData ->
                    try {
                        val existingBooks = bookUseCases.getBooksInLibrary()
                        existingBooks.forEach { book -> bookUseCases.deleteBook(book.id) }
                        
                        val existingCategories = categoryUseCases.getCategories()
                        existingCategories.forEach { category ->
                            if (!category.isSystemCategory) {
                                categoryUseCases.deleteCategory(category.id, moveToDefaultCategory = false)
                            }
                        }
                        
                        backupData.novels.forEach { book -> bookUseCases.updateBook(book) }
                        if (backupData.chapters.isNotEmpty()) {
                            insertUseCases.insertChapters(backupData.chapters)
                        }
                        
                        backupData.readingProgress.forEach { progress ->
                            chapterUseCases.updateReadStatus(progress.chapterId, isRead = progress.lastPageRead > 0)
                        }
                        backupData.bookmarks.forEach { bookmark ->
                            chapterUseCases.updateBookmarkStatus(bookmark.chapterId, isBookmarked = true)
                        }
                        
                        _successMessage.value = "Backup restored successfully. Please restart the app."
                    } catch (e: Exception) {
                        _errorMessage.value = "Failed to restore data: ${e.message}"
                    }
                }.onFailure { error ->
                    _errorMessage.value = "Failed to download backup: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to restore backup: ${e.message}"
            } finally {
                _isRestoringBackup.value = false
            }
        }
    }
    
    fun deleteBackup(backupInfo: BackupInfo) {
        scope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = googleDriveService.deleteBackup(backupInfo.id)
                result.onSuccess {
                    _successMessage.value = "Backup deleted successfully"
                    loadBackups()
                }.onFailure { error ->
                    _errorMessage.value = "Failed to delete backup: ${error.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        updateState { it.copy(errorMessage = null, successMessage = null) }
    }
}
