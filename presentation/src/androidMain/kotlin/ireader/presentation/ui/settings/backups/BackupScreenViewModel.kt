package ireader.presentation.ui.settings.backups

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.BackUpBook
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.utils.extensions.convertLongToTime
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.SettingState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

import java.util.*



class BackupScreenViewModel(
    private val booksUseCasa: LocalGetBookUseCases,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    val restoreBackup: RestoreBackup,
    val createBackup: CreateBackup,
    val uiPreferences: UiPreferences,
    val automaticBackupUseCase: AutomaticBackup,
    val getSimpleStorage: GetSimpleStorage,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    val automaticBackup = uiPreferences.automaticBackupTime().asState()
    val maxAutomaticFiles = uiPreferences.maxAutomaticBackupFiles().asState()
    fun onLocalBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/gzip")
        val fn = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/gzip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(
                Intent.EXTRA_TITLE, fn
            )

        onStart(intent)
    }

    fun onRestoreBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/gzip")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        onStart(intent)
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
}
