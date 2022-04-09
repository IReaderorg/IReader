package org.ireader.presentation.feature_library.presentation.viewmodel


import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersDescUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase
import org.ireader.presentation.feature_services.LibraryUpdatesService
import org.ireader.presentation.feature_services.downloaderService.DownloadService
import org.ireader.presentation.feature_services.downloaderService.DownloadService.Companion.DOWNLOADER_BOOKS_IDS
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val libraryLayoutUseCase: LibraryLayoutTypeUseCase,
    private val sortersDescUseCase: SortersDescUseCase,
    private val sortersUseCase: SortersUseCase,
    private val historyUseCase: HistoryUseCase,
    private val libraryState: LibraryStateImpl,

    ) : BaseViewModel(), LibraryState by libraryState {


    init {
        readLayoutTypeAndFilterTypeAndSortType()
        getLibraryBooks()
    }

    suspend fun getHistories() {
        historyUseCase.findHistories()
    }

    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.OnLayoutTypeChange -> {
                onLayoutTypeChange(event.layoutType)
            }
            is LibraryEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
                getLibraryDataIfSearchModeIsOff()
            }
            is LibraryEvents.UpdateSearchInput -> {
                onQueryChange(event.query)
            }
            is LibraryEvents.SearchBook -> {
                getLibraryBooks()
            }
            is LibraryEvents.EnableFilter -> {
                when (event.filterType) {
                    is FilterType.Unread -> {
                        addFilters(event.filterType)
                    }
                    else -> {

                    }
                }
            }
        }

    }

    fun addBooksToSelection(book: Book) {
        selection.add(book.id)
    }


    private var getBooksJob: Job? = null
    fun getLibraryBooks() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            localGetBookUseCases.SubscribeInLibraryBooks(
                sortType,
                desc = desc,
                filters
            ).collectLatest {
                books = it.filter { it.title.contains(searchQuery, true) }
            }
        }
    }


    private fun onQueryChange(query: String) {
        searchQuery = query
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        this.inSearchMode = inSearchMode
    }


    private fun onLayoutTypeChange(layoutType: DisplayMode) {
        libraryLayoutUseCase.save(layoutType.layoutIndex)
        this.layout = layoutType.layout
    }

    lateinit var downloadWork: OneTimeWorkRequest
    fun downloadChapters(context: Context) {
        downloadWork =
            OneTimeWorkRequestBuilder<DownloadService>().apply {
                setInputData(
                    Data.Builder().apply {

                        putLongArray(DOWNLOADER_BOOKS_IDS, selection.toLongArray())
                    }.build()
                )
                addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                "Group_Download"),
            ExistingWorkPolicy.REPLACE,
            downloadWork
        )
        selection.clear()


    }

    fun markAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            selection.forEach { bookId ->
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                insertUseCases.insertChapters(chapters.map { it.copy(read = true) })

            }
            selection.clear()
        }

    }

    fun markAsNotRead() {
        viewModelScope.launch(Dispatchers.IO) {
            selection.forEach { bookId ->
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                insertUseCases.insertChapters(chapters.map { it.copy(read = false) })
            }
            selection.clear()
        }

    }

    fun deleteBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteBookAndChapterByBookIds(selection)
            selection.clear()
        }


    }

    private fun readLayoutTypeAndFilterTypeAndSortType() {
        val sortType = sortersUseCase.read()
        val layoutType = libraryLayoutUseCase.read().layout
        val sortBy = sortersDescUseCase.read()
        this.layout = layoutType
        this.sortType = sortType
        this.desc = sortBy

    }

    fun changeSortIndex(sortType: SortType) {
        this.sortType = sortType
        if (sortType == sortType) {
            this.desc = !desc
            val sortBy = sortersDescUseCase.save(this.desc)
        }
        saveSortType(sortType)
        getLibraryBooks()
    }

    private fun saveSortType(sortType: SortType) {
        sortersUseCase.save(sortType.index)
    }

    fun addFilters(filterType: FilterType) {
        this.filters.add(filterType)
        getLibraryBooks()
    }

    fun removeFilters(filterType: FilterType) {
        this.filters.remove(filterType)
        getLibraryBooks()
    }

    private fun getLibraryDataIfSearchModeIsOff() {
        if (inSearchMode) return
        this.searchedBook = emptyList()
        this.searchQuery = ""
        getLibraryBooks()
    }

    lateinit var work: OneTimeWorkRequest
    fun refreshUpdate(context: Context) {
        work =
            OneTimeWorkRequestBuilder<LibraryUpdatesService>().apply {
                addTag(LibraryUpdatesService.LibraryUpdateTag)
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LibraryUpdatesService.LibraryUpdateTag,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}