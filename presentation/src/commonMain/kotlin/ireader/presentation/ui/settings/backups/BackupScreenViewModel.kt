package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.BackUpBook
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.storage.StorageManager
import ireader.i18n.LocalizeHelper
import ireader.presentation.ui.settings.reader.SettingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.domain.utils.extensions.currentTimeToLong


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
            showSnackBar(ireader.i18n.UiText.DynamicString("Creating backup..."))
            
            // Create backup using existing use case
            val books = booksUseCasa.findAllInLibraryBooks()
            val backupData = createBackup.createBackupData(books)
            
            // Write to file
            when (val result = fileSystemService.writeFileBytes(uri, backupData)) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Backup created successfully"))
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to create backup: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        } catch (e: Exception) {
            showSnackBar(ireader.i18n.UiText.DynamicString("Backup error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    /**
     * Restore backup from specified location
     */
    private suspend fun restoreFromLocation(uri: ireader.domain.models.common.Uri) {
        try {
            showSnackBar(ireader.i18n.UiText.DynamicString("Restoring backup..."))
            
            // Read backup file
            when (val result = fileSystemService.readFileBytes(uri)) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    // Restore using existing use case
                    restoreBackup.restoreFromBytes(result.data)
                    showSnackBar(ireader.i18n.UiText.DynamicString("Backup restored successfully"))
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to restore: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        } catch (e: Exception) {
            showSnackBar(ireader.i18n.UiText.DynamicString("Restore error: ${e.message ?: "Unknown error"}"))
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
                title = "Select LNReader Backup"
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
