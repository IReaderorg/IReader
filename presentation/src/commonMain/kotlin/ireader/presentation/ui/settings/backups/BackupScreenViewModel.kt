package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.BackUpBook
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.storage.StorageManager
import ireader.presentation.ui.settings.reader.SettingState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement



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
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    val automaticBackup = uiPreferences.automaticBackupTime().asState()
    val maxAutomaticFiles = uiPreferences.maxAutomaticBackupFiles().asState()
    
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
//        val fn = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
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
            val timestamp = System.currentTimeMillis()
            val fileName = "IReader_backup_$timestamp"
            
            when (val result = fileSystemService.saveFile(
                defaultFileName = fileName,
                fileExtension = "gz",
                title = "Save Backup"
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
                title = "Select Backup File"
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
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to create backup: ${result.message}"))
                }
                else -> {}
            }
        } catch (e: Exception) {
            showSnackBar(ireader.i18n.UiText.DynamicString("Backup error: ${e.message}"))
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
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to restore: ${result.message}"))
                }
                else -> {}
            }
        } catch (e: Exception) {
            showSnackBar(ireader.i18n.UiText.DynamicString("Restore error: ${e.message}"))
        }
    }
}
