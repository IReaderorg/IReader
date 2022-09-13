package ireader.ui.settings.backups

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import ireader.common.extensions.convertLongToTime
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.ui.settings.SettingState
import org.koin.android.annotation.KoinViewModel
import java.util.Calendar


@KoinViewModel
class BackupScreenViewModel(
    private val booksUseCasa: LocalGetBookUseCases,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    val restoreBackup: RestoreBackup,
    val createBackup: CreateBackup,
) : BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

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
        val list = mutableListOf<ireader.common.models.BackUpBook>()
        val books = booksUseCasa.findAllInLibraryBooks()

        val chapters = chapterUseCase.findAllInLibraryChapters()
        books.forEach { book ->
            list.add(
                ireader.common.models.BackUpBook(
                    book = book,
                    chapters.filter { it.bookId == book.id }
                )
            )
        }

        return Json.Default.encodeToJsonElement(list).toString()
    }

    suspend fun insertBackup(list: List<ireader.common.models.BackUpBook>) {
        val books = list.map { it.book }

        val chapters = list.map { it.chapters }.flatten()

        insertUseCases.insertBookAndChapters(books, chapters)
    }
}
