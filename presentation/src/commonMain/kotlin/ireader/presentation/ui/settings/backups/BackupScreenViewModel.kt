package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.BackUpBook
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup
import ireader.domain.usecases.backup.v2.BackupException
import ireader.domain.usecases.backup.v2.BackupOrchestrator
import ireader.domain.usecases.backup.v2.BackupProgress
import ireader.domain.usecases.backup.v2.RestoreProgress
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
import ireader.presentation.ui.settings.reader.SettingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


/**
 * Progress state for LNReader import operations (for visual progress dialog)
 */
sealed class LNReaderImportProgress {
    object Idle : LNReaderImportProgress()
    data class Starting(val message: String = "Preparing import...") : LNReaderImportProgress()
    data class Parsing(val message: String = "Parsing backup data...") : LNReaderImportProgress()
    data class ImportingNovels(
        val current: Int,
        val total: Int,
        val novelName: String
    ) : LNReaderImportProgress()
    data class ImportingCategories(
        val current: Int,
        val total: Int
    ) : LNReaderImportProgress()
    data class Complete(
        val novelsImported: Int,
        val chaptersImported: Int,
        val categoriesImported: Int,
        val novelsSkipped: Int,
        val novelsFailed: Int,
        val message: String = "Import completed!"
    ) : LNReaderImportProgress()
    data class Error(val error: String) : LNReaderImportProgress()

    val isInProgress: Boolean
        get() = this !is Idle && this !is Complete && this !is Error

    val progress: Float
        get() = when (this) {
            is Idle -> 0f
            is Starting -> 0.05f
            is Parsing -> 0.1f
            is ImportingNovels -> 0.15f + (current.toFloat() / total.coerceAtLeast(1)) * 0.75f
            is ImportingCategories -> 0.9f
            is Complete -> 1f
            is Error -> 0f
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
    private val importLNReaderBackup: ImportLNReaderBackup? = null,
    // V2 orchestrator — injected alongside old deps during migration
    private val orchestrator: BackupOrchestrator? = null,
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
     * Create backup using v2 orchestrator (checksummed, verified, single pipeline).
     * Falls back to legacy path if orchestrator is not injected.
     */
    private suspend fun createBackupToLocation(uri: ireader.domain.models.common.Uri) {
        val orch = orchestrator
        if (orch != null) {
            createBackupV2(orch, uri)
        } else {
            createBackupLegacy(uri)
        }
    }
    
    /**
     * Restore backup using v2 orchestrator (legacy-aware, partial-failure safe).
     * Falls back to legacy path if orchestrator is not injected.
     */
    private suspend fun restoreFromLocation(uri: ireader.domain.models.common.Uri) {
        val orch = orchestrator
        if (orch != null) {
            restoreBackupV2(orch, uri)
        } else {
            restoreBackupLegacy(uri)
        }
    }

    // ── V2 backup path ────────────────────────────────────────────────────

    private suspend fun createBackupV2(orch: BackupOrchestrator, uri: ireader.domain.models.common.Uri) {
        withContext(ireader.domain.utils.extensions.ioDispatcher) {
            orch.createBackup(uri) { progress ->
                _backupRestoreProgress.value = when (progress) {
                    is BackupProgress.Collecting ->
                        BackupRestoreProgress.BackupStarting()
                    is BackupProgress.Serializing ->
                        BackupRestoreProgress.BackupInProgress(
                            currentBook = progress.bookIndex,
                            totalBooks = progress.totalBooks,
                            bookName = progress.bookName,
                        )
                    is BackupProgress.Compressing ->
                        BackupRestoreProgress.BackupCompressing()
                    is BackupProgress.Writing ->
                        BackupRestoreProgress.BackupWriting()
                    is BackupProgress.Verifying ->
                        BackupRestoreProgress.BackupWriting() // reuse "writing" state for verify step
                    is BackupProgress.Complete ->
                        BackupRestoreProgress.BackupComplete()
                }
            }.fold(
                onSuccess = { summary ->
                    _backupRestoreProgress.value = BackupRestoreProgress.BackupComplete(
                        message = "Backup completed! ${summary.booksCount} books saved."
                    )
                },
                onFailure = { error ->
                    _backupRestoreProgress.value = BackupRestoreProgress.BackupError(
                        error = mapBackupError(error)
                    )
                },
            )
        }
    }

    private suspend fun restoreBackupV2(orch: BackupOrchestrator, uri: ireader.domain.models.common.Uri) {
        withContext(ireader.domain.utils.extensions.ioDispatcher) {
            orch.restoreBackup(uri) { progress ->
                _backupRestoreProgress.value = when (progress) {
                    is RestoreProgress.Reading ->
                        BackupRestoreProgress.RestoreStarting()
                    is RestoreProgress.Decompressing ->
                        BackupRestoreProgress.RestoreDecompressing()
                    is RestoreProgress.Validating ->
                        BackupRestoreProgress.RestoreParsing()
                    is RestoreProgress.Restoring ->
                        BackupRestoreProgress.RestoreInProgress(
                            currentBook = progress.bookIndex,
                            totalBooks = progress.totalBooks,
                            bookName = progress.bookName,
                        )
                    is RestoreProgress.Complete ->
                        BackupRestoreProgress.RestoreComplete(0, 0, "Restore completed!")
                }
            }.fold(
                onSuccess = { summary ->
                    val msg = buildString {
                        append("Restored ${summary.booksRestored} books, ${summary.chaptersRestored} chapters")
                        if (summary.errors.isNotEmpty()) {
                            append(" (${summary.errors.size} books had errors)")
                        }
                    }
                    _backupRestoreProgress.value = BackupRestoreProgress.RestoreComplete(
                        booksRestored = summary.booksRestored,
                        chaptersRestored = summary.chaptersRestored,
                        message = msg,
                    )
                },
                onFailure = { error ->
                    _backupRestoreProgress.value = BackupRestoreProgress.RestoreError(
                        error = mapBackupError(error)
                    )
                },
            )
        }
    }

    // ── Legacy backup path (kept for safety during migration) ─────────────

    private suspend fun createBackupLegacy(uri: ireader.domain.models.common.Uri) {
        try {
            _backupRestoreProgress.value = BackupRestoreProgress.BackupStarting()
            
            withContext(ireader.domain.utils.extensions.ioDispatcher) {
                val books = booksUseCasa.findAllInLibraryBooks()
                val totalBooks = books.size
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupInProgress(
                    currentBook = 0, totalBooks = totalBooks, bookName = "",
                    message = "Found $totalBooks books to backup..."
                )
                
                val backupData = createBackup.createBackupData(books)
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupCompressing()
                
                val compressedData = okio.Buffer().let { buffer ->
                    okio.GzipSink(buffer).buffer().use { gzipSink ->
                        gzipSink.write(backupData)
                    }
                    buffer.readByteArray()
                }
                
                _backupRestoreProgress.value = BackupRestoreProgress.BackupWriting()
                
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
    
    private suspend fun restoreBackupLegacy(uri: ireader.domain.models.common.Uri) {
        try {
            _backupRestoreProgress.value = BackupRestoreProgress.RestoreStarting()
            
            withContext(ireader.domain.utils.extensions.ioDispatcher) {
                when (val result = fileSystemService.readFileBytes(uri)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        _backupRestoreProgress.value = BackupRestoreProgress.RestoreDecompressing()
                        
                        val decompressedData = try {
                            okio.Buffer().apply { write(result.data) }
                                .let { okio.GzipSource(it) }
                                .let { okio.Buffer().apply { writeAll(it) } }
                                .readByteArray()
                        } catch (e: Exception) {
                            result.data
                        }
                        
                        _backupRestoreProgress.value = BackupRestoreProgress.RestoreParsing()
                        
                        val restoreResult = restoreBackup.restoreFromBytesWithProgress(
                            decompressedData
                        ) { current, total, bookName ->
                            _backupRestoreProgress.value = BackupRestoreProgress.RestoreInProgress(
                                currentBook = current, totalBooks = total,
                                bookName = bookName, message = "Restoring: $bookName"
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

    // ── Error mapping ─────────────────────────────────────────────────────

    private fun mapBackupError(error: Throwable): String {
        return when (error) {
            is BackupException.ReadFailed -> "Cannot read backup file"
            is BackupException.WriteFailed -> "Cannot write backup file"
            is BackupException.ChecksumMismatch -> "Backup file is corrupted (integrity check failed)"
            is BackupException.Corrupted -> error.message ?: "Backup file is corrupted"
            is BackupException.UnsupportedVersion -> "This backup was created with a newer app version"
            is BackupException.InsufficientSpace -> "Not enough storage space"
            is BackupException.VerificationFailed -> "Backup verification failed"
            is BackupException.PartialRestore -> "Some items failed to restore"
            else -> error.message ?: "Unknown error"
        }
    }

    // ==================== LNReader Import Progress Dialog ====================

    private val _lnReaderImportProgressDialog = MutableStateFlow<LNReaderImportProgress>(LNReaderImportProgress.Idle)
    val lnReaderImportProgressDialog: StateFlow<LNReaderImportProgress> = _lnReaderImportProgressDialog.asStateFlow()

    fun dismissLNReaderImportDialog() {
        _lnReaderImportProgressDialog.value = LNReaderImportProgress.Idle
    }

    // ==================== LNReader Backup Import ====================
    
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
            withContext(Dispatchers.IO) {
                importLNReaderBackup.invoke(uri, options).collect { progress ->
                    _lnReaderImportProgress.value = progress

                    // Map to dialog progress
                    _lnReaderImportProgressDialog.value = when (progress) {
                    is ImportLNReaderBackup.ImportProgress.Starting ->
                        LNReaderImportProgress.Starting()
                    is ImportLNReaderBackup.ImportProgress.Parsing ->
                        LNReaderImportProgress.Parsing(progress.message)
                    is ImportLNReaderBackup.ImportProgress.ImportingNovels ->
                        LNReaderImportProgress.ImportingNovels(
                            current = progress.current,
                            total = progress.total,
                            novelName = progress.novelName
                        )
                    is ImportLNReaderBackup.ImportProgress.ImportingCategories ->
                        LNReaderImportProgress.ImportingCategories(
                            current = progress.current,
                            total = progress.total
                        )
                    is ImportLNReaderBackup.ImportProgress.Complete -> {
                        _lnReaderImportResult.value = progress.result
                        val result = progress.result
                        LNReaderImportProgress.Complete(
                            novelsImported = result.novelsImported,
                            chaptersImported = result.chaptersImported,
                            categoriesImported = result.categoriesImported,
                            novelsSkipped = result.novelsSkipped,
                            novelsFailed = result.novelsFailed,
                            message = if (result.errors.isNotEmpty()) {
                                "Import completed with ${result.errors.size} errors. " +
                                "${result.novelsImported} novels, ${result.chaptersImported} chapters imported."
                            } else {
                                "Import complete: ${result.novelsImported} novels, " +
                                "${result.chaptersImported} chapters, " +
                                "${result.categoriesImported} categories imported"
                            }
                        )
                    }
                    is ImportLNReaderBackup.ImportProgress.Error -> {
                        LNReaderImportProgress.Error(getErrorMessage(progress.error))
                    }
                }
                
                when (progress) {
                    is ImportLNReaderBackup.ImportProgress.Starting -> {
                        showSnackBar(ireader.i18n.UiText.MStringResource(Res.string.lnreader_import_starting))
                    }
                    is ImportLNReaderBackup.ImportProgress.Complete -> {
                        val result = progress.result
                        val fullMessage = if (result.novelsSkipped > 0) {
                            "${_lnReaderImportProgressDialog.value.let { (it as? LNReaderImportProgress.Complete)?.message ?: "" }}\n${result.novelsSkipped} novels were skipped (already in library)"
                        } else {
                            (lnReaderImportProgressDialog.value as? LNReaderImportProgress.Complete)?.message ?: ""
                        }
                        showSnackBar(ireader.i18n.UiText.DynamicString(fullMessage))
                    }
                    is ImportLNReaderBackup.ImportProgress.Error -> {
                        val errorMessage = getErrorMessage(progress.error)
                        showSnackBar(ireader.i18n.UiText.DynamicString(errorMessage))
                    }
                    else -> { /* Progress updates handled by dialog */ }
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
