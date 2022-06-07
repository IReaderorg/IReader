package org.ireader.settings.setting.backups

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.ireader.common_extensions.convertLongToTime
import org.ireader.common_models.BackUpBook
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.backup.CreateBackup
import org.ireader.domain.use_cases.backup.RestoreBackup
import org.ireader.domain.use_cases.epub.epup_parser.epubparser.EpubParser
import org.ireader.domain.use_cases.epub.importer.ImportEpub
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.image_loader.LibraryCovers
import org.ireader.settings.setting.SettingState
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BackupScreenViewModel @Inject constructor(
    private val deleteUseCase: DeleteUseCase,
    private val libraryCovers: LibraryCovers,
    private val booksUseCasa: LocalGetBookUseCases,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val prefUseCases: ReaderPrefUseCases,
    val restoreBackup: RestoreBackup,
    val createBackup: CreateBackup,
    val importEpub: ImportEpub,
    val epubParser: EpubParser
) : BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    fun onLocalBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/gzip")
        val fn = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.proto"
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
    fun onEpubBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/*")
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


