package ireader.presentation.ui.book.viewmodel


import androidx.compose.runtime.*
import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.utils.extensions.withIOContext
import ireader.domain.utils.extensions.withUIContext
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.core.PlatformHelper
import ireader.presentation.ui.home.explore.viewmodel.BooksState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import java.util.*


class BookDetailViewModel(
    private val localInsertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    val state: DetailStateImpl,
    val chapterState: ChapterStateImpl,
    private val serviceUseCases: ServiceUseCases,
    val deleteUseCase: ireader.domain.usecases.local.DeleteUseCase,
    private val applicationScope: CoroutineScope,
    val createEpub: EpubCreator,
    val historyUseCase: HistoryUseCase,
    val readerPreferences: ReaderPreferences,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val param: Param,
    val booksState: BooksState,
    val platformHelper: PlatformHelper
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DetailState by state, ChapterState by chapterState {
    data class Param(val bookId: Long?)


    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null
    var initBooks = false
    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(loadSortingPreference())
    var layout by readerPreferences.showChapterNumberPreferences().asState()

    private fun loadSortingPreference(): ChapterSort {
        val sortTypeName = readerPreferences.chapterSortType().get()
        val isAscending = readerPreferences.chapterSortAscending().get()
        return try {
            val type = ChapterSort.Type.valueOf(sortTypeName)
            ChapterSort(type, isAscending)
        } catch (e: Exception) {
            ChapterSort.default
        }
    }

    private fun saveSortingPreference(sort: ChapterSort) {
        readerPreferences.chapterSortType().set(sort.type.name)
        readerPreferences.chapterSortAscending().set(sort.isAscending)
    }

    var launcher : () -> Any = {}
    init {
        booksState.book = null
        val bookId = param.bookId
        val sourceId = runBlocking {
            bookId?.let { getBookUseCases.findBookById(it)?.sourceId }
        }

        if (bookId != null && sourceId != null) {
            val catalogSource = getLocalCatalog.get(sourceId)
            this.catalogSource = catalogSource

            val source = catalogSource?.source
            if (source is ireader.core.source.CatalogSource) {
                this.modifiedCommands = source.getCommands()
            }
            toggleBookLoading(true)
            chapterIsLoading = true
            scope.launch {
                subscribeBook(bookId = bookId)
                subscribeChapters(bookId = bookId)
                getLastReadChapter(bookId)
                initBook(bookId)
            }

        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(MR.strings.something_is_wrong_with_this_book))
            }
        }
    }


    private suspend fun initBook(bookId: Long) {
        var book = getBookUseCases.findBookById(bookId)

        if (book != null && book.lastUpdate < 1L && source != null) {
            book = book?.copy(
                id = bookId,
                initialized = true,
                lastUpdate = Clock.System.now().toEpochMilliseconds(),

                )
            booksState.replaceBook(book)
            localInsertUseCases.updateBook.update(
                book
            )
            getRemoteBookDetail(book, catalogSource)
            getRemoteChapterDetail(book, catalogSource)
        } else {
            toggleBookLoading(false)
            chapterIsLoading = false
        }

    }


    private fun subscribeBook(bookId: Long) {
        getBookUseCases.subscribeBookById(bookId).onEach { snapshot ->
            booksState.replaceBook(snapshot)
        }.launchIn(scope)
    }

    private fun subscribeChapters(bookId: Long) {
        getChapterUseCase.subscribeChaptersByBookId(bookId).onEach { snapshot ->
            chapters = snapshot
        }.launchIn(scope)
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
        historyUseCase.subscribeHistoryByBookId(bookId).onEach {
            lastRead = it?.chapterId
        }.launchIn(scope)
    }

    suspend fun getRemoteBookDetail(book: Book?, source: CatalogLocal?) {
        if (book == null) return
        toggleBookLoading(true)
        getBookDetailJob?.cancel()
        getBookDetailJob = scope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = source,
                onError = { message ->
                    withUIContext {
                        toggleBookLoading(false)
                        showSnackBar(message)
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
        getChapterDetailJob = scope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = source,
                onError = { message ->
                    Log.error { message.toString() }
                    showSnackBar(message)
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


    fun deleteChapters(chapters: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
        }
    }

    fun downloadChapters() {
        booksState.book?.let { book ->
            serviceUseCases.startDownloadServicesUseCase.start(chapterIds = this.selection.toLongArray())
        }
    }

    fun startDownloadService(book: Book) {
        serviceUseCases.startDownloadServicesUseCase.start(bookIds = longArrayOf(book.id))
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
        val newSort = if (type == currentSort.value.type) {
            currentSort.value.copy(isAscending = !currentSort.value.isAscending)
        } else {
            currentSort.value.copy(type = type)
        }
        sorting.value = newSort
        saveSortingPreference(newSort)
    }
}
