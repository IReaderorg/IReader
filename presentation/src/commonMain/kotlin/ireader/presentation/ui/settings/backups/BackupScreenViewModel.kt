package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.BackUpBook
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.lnreader_error_corrupted_backup
import ireader.i18n.resources.lnreader_error_database_failed
import ireader.i18n.resources.lnreader_error_empty_backup
import ireader.i18n.resources.lnreader_error_file_not_found
import ireader.i18n.resources.lnreader_error_invalid_backup
import ireader.i18n.resources.lnreader_error_out_of_memory
import ireader.i18n.resources.lnreader_error_parse_failed
import ireader.i18n.resources.lnreader_error_permission_denied
import ireader.i18n.resources.lnreader_error_read_failed
import ireader.i18n.resources.lnreader_import_not_available
import ireader.i18n.resources.lnreader_import_starting
import ireader.i18n.resources.save_backup
import ireader.i18n.resources.select_backup_file
import ireader.i18n.resources.select_lnreader_backup
import ireader.presentation.ui.settings.reader.SettingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okio.buffer
import okio.use

/**
 * Progress state for backup/restore operations
 */
sealed class BackupRestoreProgress {
    object Idle : BackupRestoreProgress()
    
    // Backup states
    data class BackupStarting(val message: String = "Preparing backup...") : BackupRestoreProgress()
    data class BackupInProgress(
        val currentBook: Int,
        val totalBooks: Int,
        val bookName: String,
        val message: String = "Backing up library..."
    ) : BackupRestoreProgress()
    data class BackupCompressing(val message: String = "Compressing backup...") : BackupRestoreProgress()
    data class BackupWriting(val message: String = "Saving backup file...") : BackupRestoreProgress()
    data class BackupComplete(val message: String = "Backup completed!") : BackupRestoreProgress()
    data class BackupError(val error: String) : BackupRestoreProgress()
    
    // Restore states
    data class RestoreStarting(val message: String = "Reading backup file...") : BackupRestoreProgress()
    data class RestoreDecompressing(val message: String = "Decompressing backup...") : BackupRestoreProgress()
    data class RestoreParsing(val message: String = "Parsing backup data...") : BackupRestoreProgress()
    data class RestoreInProgress(
        val currentBook: Int,
        val totalBooks: Int,
        val bookName: String,
        val message: String = "Restoring library..."
    ) : BackupRestoreProgress()
    data class RestoreComplete(
        val booksRestored: Int,
        val chaptersRestored: Int,
        val message: String = "Restore completed!"
    ) : BackupRestoreProgress()
    data class RestoreError(val error: String) : BackupRestoreProgress()
    
    val isInProgress: Boolean
        get() = this !is Idle && this !is BackupComplete && this !is BackupError && 
                this !is RestoreComplete && this !is RestoreError
    
    val progress: Float
        get() = when (this) {
            is Idle -> 0f
            is BackupStarting -> 0.05f
            is BackupInProgress -> 0.1f + (currentBook.toFloat() / totalBooks.coerceAtLeast(1)) * 0.7f
            is BackupCompressing -> 0.85f
            is BackupWriting -> 0.95f
            is BackupComplete -> 1f
            is BackupError -> 0f
            is RestoreStarting -> 0.05f
            is RestoreDecompressing -> 0.1f
            is RestoreParsing -> 0.15f
            is RestoreInProgress -> 0.2f + (currentBook.toFloat() / totalBooks.coerceAtLeast(1)) * 0.75f
            is RestoreComplete -> 1f
            is RestoreError -> 0f
        }
}


class BackupScreenViewModel(
    private val booksUseCasa: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val chapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    val restoreBackup: RestoreBackup,
    val createBackup: CreateBackup,
    val uiPreferences: UiPreferences,
    val getSimpleStorage: GetSimpleStorage,
    val storageManager: StorageManager,
    private val scheduleAutomaticBackup: ireader.domain.usecases.backup.ScheduleAutomaticBackup? = null,
    // Platform services - Clean architecture
    private val fileSystemService: ireader.domain.services.platform.FileSystemService,
    private val localizeHelper: LocalizeHelper,
    // LNReader backup import
    private val importLNReaderBackup: ImportLNReaderBackup? = null
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    val automaticBackup = uiPreferences.automaticBackupTime().asState()
    val maxAutomaticFiles = uiPreferences.maxAutomaticBackupFiles().asState()
    
    // Backup/Restore progress state
    private val _backupRestoreProgress = MutableStateFlow<BackupRestoreProgress>(BackupRestoreProgress.Idle)
    val backupRestoreProgress: StateFlow<BackupRestoreProgress> = _backupRestoreProgress.asStateFlow()
    
    // LNReader import state
    private val _lnReaderImportProgress = MutableStateFlow<ImportLNReaderBackup.ImportProgress?>(null)
    val lnReaderImportProgress: StateFlow<ImportLNReaderBackup.ImportProgress?> = _lnReaderImportProgress.asStateFlow()
    
    private val _lnReaderImportResult = MutableStateFlow<ImportLNReaderBackup.ImportResult?>(null)
    val lnReaderImportResult: StateFlow<ImportLNReaderBackup.ImportResult?> = _lnReaderImportResult.asStateFlow()
    
    init {
        // Schedule automatic backups if enabled
        if (automaticBackup.value != ireader.domain.models.prefs.PreferenceValues.AutomaticBackup.Off) {
            scheduleAutomaticBackup?.schedule(automaticBackup.value)
        }
    }
    
    /**
     * Dismiss the progress dialog
     */
    fun dismissProgress() {
        _backupRestoreProgress.value = BackupRestoreProgress.Idle
    }
    
    /**
     * Update automatic backup frequency and reschedule
     */
    fun updateAutomaticBackupFrequency(frequency: ireader.domain.models.prefs.PreferenceValues.AutomaticBackup) {
        automaticBackup.value = frequency
        
        // Check storage permissions before scheduling
        if (!storageManager.hasStoragePermission()) {
            storageManager.initializeDirectories()
        }
        
        if (frequency == ireader.domain.models.prefs.PreferenceValues.AutomaticBackup.Off) {
            scheduleAutomaticBackup?.cancel()
        } else {
            scheduleAutomaticBackup?.schedule(frequency)
        }
    }
//    fun onLocalBackupRequested(onStart: (Intent) -> Unit) {
//        val mimeTypes = arrayOf("application/gzip")
//        val fn = "IReader_${convertLongToTime(currentTimeToLong())}.gz"
//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
//            .addCategory(Intent.CATEGORY_OPENABLE)
//            .setType("application/gzip")
//            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//            .putExtra(
//                Intent.EXTRA_TITLE, fn
//            )
//
//        onStart(intent)
//    }
//
//    fun onRestoreBackupRequested(onStart: (Intent) -> Unit) {
//        val mimeTypes = arrayOf("application/gzip")
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            .addCategory(Intent.CATEGORY_OPENABLE)
//            .setType("application/*")
//            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//        onStart(intent)
//    }

    suspend fun getAllBooks(): String {
        val list = mutableListOf<BackUpBook>()
        val books = booksUseCasa.findAllInLibraryBooks()

        val chapters = chapterUseCase.findAllInLibraryChapters()
        books.forEach { book ->
            list.add(
                BackUpBook(
                    book = book,
                    chapters.filter { it.bookId == book.id }
                )
            )
        }

        return Json.Default.encodeToJsonElement(list).toString()
    }

    suspend fun insertBackup(list: List<BackUpBook>) {
        val books = list.map { it.book }

        val chapters = list.map { it.chapters }.flatten()

        insertUseCases.insertBookAndChapters(books, chapters)
    }
    
    // ==================== Platform Service Methods ====================
    
    /**
     * Pick backup file location for creating backup using platform service
     */
    fun pickBackupLocation() {
        scope.launch {
            val timestamp = currentTimeToLong()
            val fileName = "IReader_backup_$timestamp"
            
            when (val result = fileSystemService.saveFile(
                defaultFileName = fileName,
                fileExtension = "gz",
                title = localizeHelper.localize(Res.string.save_backup)
            )) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    // Create backup to selected location
                    createBackupToLocation(result.data)
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Backup cancelled"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Pick backup file for restoration using platform service
     */
    fun pickRestoreFile() {
        scope.launch {
            when (val result = fileSystemService.pickFile(
                fileTypes = listOf("gz", "json"),
                title = localizeHelper.localize(Res.string.select_backup_file)
            )) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    // Restore from selected file
                    restoreFromLocation(result.data)
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Restore cancelled"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Create backup to specified URI (called from UI after file picker)
     */
    fun createBackupToUri(uri: ireader.domain.models.common.Uri) {
        scope.launch {
            createBackupToLocation(uri)
        }
    }
    
    /**
     * Restore backup from specified URI (called from UI after file picker)
     */
    fun restoreBackupFromUri(uri: ireader.domain.models.common.Uri) {
        scope.launch {
            restoreFromLocation(uri)
        }
    }
    
    /**
     * Create backup to specified location
     */
    private suspend fun createBackupToLocation(uri: ireader.domain.models.common.Uri) {
        try {
            _backupRestoreProgress.value = BackupRestoreProgress.BackupStarting()
            
            // Run on IO dispatcher to avoid blocking main thread
            kotlinx.coroutines.withContext(ireader.domain.utils.extensions.ioDispatcher) {
                // Get all books
                val books = booksUseCasa.findAllInLibraryBooks()
                val totalBooks = books.size
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupInProgress(
                    currentBook = 0,
                    totalBooks = totalBooks,
                    bookName = "",
                    message = "Found $totalBooks books to backup..."
                )
                
                // Create backup data with progress updates
                val backupData = createBackup.createBackupData(books)
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupCompressing()
                
                // Compress with gzip
                val compressedData = okio.Buffer().let { buffer ->
                    okio.GzipSink(buffer).buffer().use { gzipSink ->
                        gzipSink.write(backupData)
                    }
                    buffer.readByteArray()
                }
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupWriting()
                
                // Write to file
                when (val result = fileSystemService.writeFileBytes(uri, compressedData)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        _backupRestoreProgress.value = BackupRestoreProgress.BackupComplete(
                            message = "Backup completed! $totalBooks books saved."
                        )
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        _backupRestoreProgress.value = BackupRestoreProgress.BackupError(
                            error = result.message ?: "Unknown error"
                        )
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            _backupRestoreProgress.value = BackupRestoreProgress.BackupError(
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * Restore backup from specified location
     */
    private suspend fun restoreFromLocation(uri: ireader.domain.models.common.Uri) {
        try {
            _backupRestoreProgress.value = BackupRestoreProgress.RestoreStarting()
            
            // Run on IO dispatcher to avoid blocking main thread
            kotlinx.coroutines.withContext(ireader.domain.utils.extensions.ioDispatcher) {
                // Read backup file
                when (val result = fileSystemService.readFileBytes(uri)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        _backupRestoreProgress.value = BackupRestoreProgress.RestoreDecompressing()
                        
                        // Decompress gzip if needed (backup files are gzip compressed)
                        // Use Okio for KMP compatibility
                        val decompressedData = try {
                            okio.Buffer().apply { write(result.data) }
                                .let { okio.GzipSource(it) }
                                .let { okio.Buffer().apply { writeAll(it) } }
                                .readByteArray()
                        } catch (e: Exception) {
                            // Not gzip compressed, use raw data
                            result.data
                        }
                        
                        _backupRestoreProgress.value = BackupRestoreProgress.RestoreParsing()
                        
                        // Restore using existing use case with progress callback
                        val restoreResult = restoreBackup.restoreFromBytesWithProgress(
                            decompressedData
                        ) { current, total, bookName ->
                            _backupRestoreProgress.value = BackupRestoreProgress.RestoreInProgress(
                                currentBook = current,
                                totalBooks = total,
                                bookName = bookName,
                                message = "Restoring: $bookName"
                            )
                        }
                        
                        when (restoreResult) {
                            is RestoreBackup.Result.Success -> {
                                _backupRestoreProgress.value = BackupRestoreProgress.RestoreComplete(
                                    booksRestored = restoreResult.booksRestored,
                                    chaptersRestored = restoreResult.chaptersRestored,
                                    message = "Restored ${restoreResult.booksRestored} books, ${restoreResult.chaptersRestored} chapters"
                                )
                            }
                            is RestoreBackup.Result.Error -> {
                                _backupRestoreProgress.value = BackupRestoreProgress.RestoreError(
                                    error = restoreResult.error.message ?: "Unknown error"
                                )
                            }
                        }
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        _backupRestoreProgress.value = BackupRestoreProgress.RestoreError(
                            error = result.message ?: "Failed to read backup file"
                        )
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            _backupRestoreProgress.value = BackupRestoreProgress.RestoreError(
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    // ==================== LNReader Backup Import ====================
    
    /**
     * Pick LNReader backup file for import
     */
    fun pickLNReaderBackupFile() {
        scope.launch {
            when (val result = fileSystemService.pickFile(
                fileTypes = listOf("zip"),
                title = localizeHelper.localize(Res.string.select_lnreader_backup)
            )) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    importLNReaderBackupFromUri(result.data)
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Import cancelled"))
                }
                else -> {}
            }
        }
    }
    
    /**
     * Import LNReader backup from URI
     */
    fun importLNReaderBackupFromUri(
        uri: ireader.domain.models.common.Uri,
        options: ImportLNReaderBackup.ImportOptions = ImportLNReaderBackup.ImportOptions()
    ) {
        if (importLNReaderBackup == null) {
            showSnackBar(ireader.i18n.UiText.MStringResource(Res.string.lnreader_import_not_available))
            return
        }
        
        scope.launch {
            importLNReaderBackup.invoke(uri, options).collect { progress ->
                _lnReaderImportProgress.value = progress
                
                when (progress) {
                    is ImportLNReaderBackup.ImportProgress.Starting -> {
                        showSnackBar(ireader.i18n.UiText.MStringResource(Res.string.lnreader_import_starting))
                    }
                    is ImportLNReaderBackup.ImportProgress.Parsing -> {
                        // Progress update handled by UI
                    }
                    is ImportLNReaderBackup.ImportProgress.ImportingNovels -> {
                        // Progress update handled by UI
                    }
                    is ImportLNReaderBackup.ImportProgress.ImportingCategories -> {
                        // Progress update handled by UI
                    }
                    is ImportLNReaderBackup.ImportProgress.Complete -> {
                        _lnReaderImportResult.value = progress.result
                        val result = progress.result
                        
                        // Show appropriate message based on errors
                        val message = if (result.errors.isNotEmpty()) {
                            "Import completed with ${result.errors.size} errors. " +
                            "${result.novelsImported} novels, ${result.chaptersImported} chapters imported."
                        } else {
                            "Import complete: ${result.novelsImported} novels, " +
                            "${result.chaptersImported} chapters, " +
                            "${result.categoriesImported} categories imported"
                        }
                        
                        // Add warning about skipped novels
                        val fullMessage = if (result.novelsSkipped > 0) {
                            "$message\n${result.novelsSkipped} novels were skipped (already in library)"
                        } else {
                            message
                        }
                        
                        showSnackBar(ireader.i18n.UiText.DynamicString(fullMessage))
                    }
                    is ImportLNReaderBackup.ImportProgress.Error -> {
                        val errorMessage = getErrorMessage(progress.error)
                        showSnackBar(ireader.i18n.UiText.DynamicString(errorMessage))
                    }
                }
            }
        }
    }
    
    /**
     * Get user-friendly error message for LNReader import errors
     */
    private fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.InvalidBackupException ->
                localizeHelper.localize(Res.string.lnreader_error_invalid_backup)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.CorruptedBackupException ->
                localizeHelper.localize(Res.string.lnreader_error_corrupted_backup)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.EmptyBackupException ->
                localizeHelper.localize(Res.string.lnreader_error_empty_backup)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.ReadFailedException ->
                error.message ?: localizeHelper.localize(Res.string.lnreader_error_read_failed)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.ParseFailedException ->
                error.message ?: localizeHelper.localize(Res.string.lnreader_error_parse_failed)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.DatabaseException ->
                error.message ?: localizeHelper.localize(Res.string.lnreader_error_database_failed)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.PermissionDeniedException ->
                localizeHelper.localize(Res.string.lnreader_error_permission_denied)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.FileNotFoundException ->
                localizeHelper.localize(Res.string.lnreader_error_file_not_found)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.OutOfMemoryException ->
                localizeHelper.localize(Res.string.lnreader_error_out_of_memory)
            is ireader.domain.usecases.backup.lnreader.LNReaderImportException.UnknownException ->
                error.message ?: "An unexpected error occurred"
            else ->
                error.message ?: "Import failed: Unknown error"
        }
    }
    
    /**
     * Clear LNReader import state
     */
    fun clearLNReaderImportState() {
        _lnReaderImportProgress.value = null
        _lnReaderImportResult.value = null
    }
}
