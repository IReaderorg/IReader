package org.ireader.domain.view_models.settings

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.ireader.core.utils.convertLongToTime
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.feature_services.io.LibraryCovers
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.utils.launchIO
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val deleteUseCase: DeleteUseCase,
    private val libraryCovers: LibraryCovers,
    private val booksUseCasa: LocalGetBookUseCases,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,

    ) : BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state


    fun deleteAllDatabase() {
        viewModelScope.launchIO {
            deleteUseCase.deleteAllBook()
            deleteUseCase.deleteAllChapters()
            deleteUseCase.deleteAllRemoteKeys()
            deleteUseCase.deleteAllExploreBook()
        }

    }

    fun deleteAllChapters() {
        viewModelScope.launchIO {
            deleteUseCase.deleteAllChapters()
        }
    }

    fun deleteImageCache() {
        viewModelScope.launchIO {
            libraryCovers.deleteAll()
        }
    }


    fun onLocalBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("text/plain")
        val fn = "backup ${convertLongToTime(Calendar.getInstance().timeInMillis)}.txt"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(
                Intent.EXTRA_TITLE, fn
            )

        onStart(intent)

    }

    fun onRestoreBackupRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("text/plain")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
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
            list.add(BackUpBook(
                book = book,
                chapters.filter { it.bookId == book.id }
            ))
        }
        return Json.Default.encodeToJsonElement(list).toString()
    }

    fun insertBackup(list: List<BackUpBook>) {
        viewModelScope.launch {
            val books = list.map { it.book }
            val chapters = list.map { it.chapters }.flatten()
            insertUseCases.insertBooks(books)
            insertUseCases.insertChapters(chapters)
        }

    }

}

@Serializable
data class BackUpBook(
    val book: Book,
    val chapters: List<Chapter>,
)