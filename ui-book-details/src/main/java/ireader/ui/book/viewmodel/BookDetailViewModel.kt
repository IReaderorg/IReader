package ireader.ui.book.viewmodel

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import ireader.common.extensions.async.withIOContext
import ireader.common.extensions.withUIContext
import ireader.common.models.entities.Book
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.Chapter
import ireader.common.resources.UiText
import ireader.core.api.log.Log
import ireader.core.api.source.model.CommandList
import ireader.core.catalogs.interactor.GetLocalCatalog
import ireader.core.ui.preferences.ReaderPreferences
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.use_cases.epub.EpubCreator
import ireader.domain.use_cases.history.HistoryUseCase
import ireader.domain.use_cases.local.DeleteUseCase
import ireader.domain.use_cases.local.LocalGetChapterUseCase
import ireader.domain.use_cases.local.LocalInsertUseCases
import ireader.domain.use_cases.remote.RemoteUseCases
import ireader.domain.use_cases.services.ServiceUseCases
import ireader.ui.book.R
import ireader.ui.component.Controller
import java.util.Calendar
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BookDetailViewModel(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    val state: DetailStateImpl,
    val chapterState: ChapterStateImpl,
    private val serviceUseCases: ServiceUseCases,
    val deleteUseCase: DeleteUseCase,
    private val applicationScope: CoroutineScope,
    val createEpub: EpubCreator,
    val historyUseCase: HistoryUseCase,
    val readerPreferences: ReaderPreferences,
    val insertUseCases: LocalInsertUseCases,
    private val param: Param,
) : BaseViewModel(), DetailState by state, ChapterState by chapterState {
    data class Param(val bookId:Long? , val sourceId: Long?)

    companion object  {
        fun createParam(controller: Controller) : Param {
            return Param(controller.navBackStackEntry.arguments?.getLong("bookId"),controller.navBackStackEntry.arguments?.getLong("sourceId"))
        }
    }

    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null
    var initBooks = false
    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(ChapterSort.default)
    var layout by readerPreferences.showChapterNumberPreferences().asState()

    init {
        val bookId = param.bookId
        val sourceId = param.sourceId
        if (bookId != null && sourceId != null) {
            val catalogSource = getLocalCatalog.get(sourceId)
            this.catalogSource = catalogSource

            val source = catalogSource?.source
            if (source is ireader.core.api.source.CatalogSource) {
                this.modifiedCommands = source.getCommands()
            }
            toggleBookLoading(true)
            chapterIsLoading = true
            subscribeBook(bookId = bookId)
            subscribeChapters(bookId = bookId)
            getLastReadChapter(bookId)
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_book))
            }
        }
    }

    private fun subscribeBook(bookId: Long) {
        getBookUseCases.subscribeBookById(bookId)
            .onEach { snapshot ->
                state.book = snapshot
                toggleBookLoading(false)
                if (!initBooks) {
                    initBooks = true
                    if (snapshot != null && snapshot.lastUpdate < 1L && source != null) {
                        getRemoteBookDetail(snapshot, catalogSource)
                        getRemoteChapterDetail(snapshot, catalogSource)
                    } else {
                        toggleBookLoading(false)
                        chapterIsLoading = false
                    }
                }
            }.launchIn(scope)
    }

    private fun subscribeChapters(bookId: Long) {
        getChapterUseCase.subscribeChaptersByBookId(bookId).onEach { snapshot ->
            chapters = snapshot
        }.launchIn(viewModelScope)
    }

    private val reservedChars = "|\\?*<\":>+[]/'"
    private fun sanitizeFilename(name: String): String {
        var tempName = name
        for (c in reservedChars) {
            tempName = tempName.replace(c, ' ')
        }
        return tempName.replace("  ", " ")
    }

    fun onEpubCreateRequested(book: Book, onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/epub+zip")
        val fn = "${sanitizeFilename(book.title)}.epub"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/epub+zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(
                Intent.EXTRA_TITLE, fn
            )
        onStart(intent)
    }
    @Composable
    fun getChapters(bookId: Long?): State<List<Chapter>> {
        if (bookId == null) return remember {
            mutableStateOf(emptyList())
        }
        val scope = rememberCoroutineScope()
        val unfiltered = remember(bookId, sorting.value, filters.value) {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId = bookId,
                sort = sorting.value.parameter,
            ).shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
        }

        return remember(chapterState.query, bookId, sorting.value, filters.value) {
            val query = chapterState.query
            if (query.isNullOrBlank()) {
                unfiltered
            } else {
                unfiltered.map { chapters ->
                    chapters.filter { it.name.contains(query, true) }
                }
            }.map { it.filteredWith(filters.value) }.onEach {
                chapterState.chapters = it
            }
        }.collectAsState(emptyList())
    }

    private fun List<Chapter>.filteredWith(filters: List<ChaptersFilters>): List<Chapter> {
        if (filters.isEmpty()) return this
        val validFilters =
            filters.filter { it.value == ChaptersFilters.Value.Included || it.value == ChaptersFilters.Value.Excluded }
        var filteredList = this
        for (filter in validFilters) {
            val filterFn: (Chapter) -> Boolean = when (filter.type) {
                ChaptersFilters.Type.Unread -> {
                    {
                        !it.read
                    }
                }
                ChaptersFilters.Type.Bookmarked -> {
                    { book -> book.bookmark }
                }
                ChaptersFilters.Type.Downloaded -> {
                    {
                        it.content.joinToString("").isNotBlank()
                    }
                }
            }
            filteredList = when (filter.value) {
                ChaptersFilters.Value.Included -> filter(filterFn)
                ChaptersFilters.Value.Excluded -> filterNot(filterFn)
                ChaptersFilters.Value.Missing -> this
            }
        }

        return filteredList
    }

    fun getLastReadChapter(bookId: Long) {
        viewModelScope.launch {
            historyUseCase.subscribeHistoryByBookId(bookId).onEach {
                lastRead = it?.chapterId
            }.launchIn(viewModelScope)
        }
    }

    suspend fun getRemoteBookDetail(book: Book, source: CatalogLocal?) {
        toggleBookLoading(true)
        getBookDetailJob?.cancel()
        getBookDetailJob = viewModelScope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = source,
                onError = { message ->
                    withUIContext {
                        toggleBookLoading(false)
                        if (message != null) {
                            Log.error { message.toString() }
                        }
                    }
                },
                onSuccess = { resultBook ->
                    withUIContext {
                        toggleBookLoading(false)
                    }
                    localInsertUseCases.updateBook.update(resultBook)
                }

            )
        }
    }
    fun getLastChapterIndex(): Int {
        return when (val index = chapters.reversed().indexOfFirst { it.id == lastRead }) {
            -1 -> {
                throw Exception("chapter not found")
            }
            else -> {
                index
            }
        }
    }

    suspend fun getRemoteChapterDetail(
        book: Book?,
        source: CatalogLocal?,
        commands: CommandList = emptyList()
    ) {
        if (book == null) return
        chapterIsLoading = true
        getChapterDetailJob?.cancel()
        getChapterDetailJob = viewModelScope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = source,
                onError = { message ->
                    Log.error { message.toString() }
                    // showSnackBar(message)
                    withUIContext {
                        chapterIsLoading = false
                    }
                },
                onSuccess = { result ->
                    localInsertUseCases.insertChapters(result)
                    withUIContext {
                        chapterIsLoading = false
                    }
                },
                commands = commands,
                oldChapters = chapterState.chapters
            )
        }
    }

    fun toggleInLibrary(book: Book) {
        this.inLibraryLoading = true
        applicationScope.launch {
            if (!book.favorite) {
                withIOContext {
                    localInsertUseCases.updateBook.update(
                        book.copy(
                            favorite = true,
                            dateAdded = Calendar.getInstance().timeInMillis,
                        )
                    )
                }
            } else {
                withIOContext {
                    deleteUseCase.unFavoriteBook(listOf(book.id))
                }
            }
            this@BookDetailViewModel.inLibraryLoading = false
        }
    }
    fun insertChapters(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapters(chapters)
        }
    }

    fun deleteChapters(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
        }
    }

    fun downloadChapters() {
        book?.let { book ->
            serviceUseCases.startDownloadServicesUseCase(chapterIds = this.selection.toLongArray())
        }
    }

    fun startDownloadService(book: Book) {
        serviceUseCases.startDownloadServicesUseCase(bookIds = longArrayOf(book.id))
    }
    fun toggleFilter(type: ChaptersFilters.Type) {
        val newFilters = filters.value
            .map { filterState ->
                if (type == filterState.type) {
                    ChaptersFilters(
                        type,
                        when (filterState.value) {
                            ChaptersFilters.Value.Included -> ChaptersFilters.Value.Excluded
                            ChaptersFilters.Value.Excluded -> ChaptersFilters.Value.Missing
                            ChaptersFilters.Value.Missing -> ChaptersFilters.Value.Included
                        }
                    )
                } else {
                    filterState
                }
            }
        this.filters.value = newFilters
    }

    private fun toggleBookLoading(isLoading: Boolean) {
        this.detailIsLoading = isLoading
    }
    fun toggleSort(type: ChapterSort.Type) {
        val currentSort = sorting
        sorting.value = if (type == currentSort.value.type) {
            currentSort.value.copy(isAscending = !currentSort.value.isAscending)
        } else {
            currentSort.value.copy(type = type)
        }
    }
}
