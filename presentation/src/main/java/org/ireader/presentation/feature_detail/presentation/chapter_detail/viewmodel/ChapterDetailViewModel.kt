package org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.presentation.feature_services.downloaderService.DownloadService
import org.ireader.presentation.feature_services.downloaderService.DownloadService.Companion.DOWNLOADER_Chapters_IDS
import javax.inject.Inject


@HiltViewModel
class ChapterDetailViewModel @Inject constructor(
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val state: ChapterDetailStateImpl,
) : ViewModel(), ChapterDetailState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        if (bookId != null && sourceId != null) {
            viewModelScope.launch {
                getLocalBookById(bookId)

            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
            }
        }
    }

    fun onEvent(event: ChapterDetailEvent) {
        when (event) {
            is ChapterDetailEvent.ToggleOrder -> {
                this.chapters = this.chapters.reversed()
                toggleAsc()
                book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
            }

        }
    }

    fun getLastReadChapter(book: Book) {
        viewModelScope.launch {
            lastRead = getChapterUseCase.findLastReadChapter(book.id)?.id
        }
    }

    fun getLastChapterIndex(): Int {
        return when (val index = chapters.indexOfFirst { it.id == lastRead }) {
            -1 -> {
                throw Exception("chapter not found")
            }
            else -> {
                index
            }
        }
    }

    fun autoSortChapterInDB() {
        val list = state.chapters.sortedWith(object : Comparator<Chapter> {
            override fun compare(o1: Chapter, o2: Chapter): Int {
                return extractInt(o1) - extractInt(o2)
            }

            fun extractInt(s: Chapter): Int {
                val num = s.title.replace("\\D".toRegex(), "")
                // return 0 if no digits found
                return if (num.isEmpty()) 0 else Integer.parseInt(num)
            }
        }
        )
        viewModelScope.launch(Dispatchers.IO)
        {
            deleteUseCase.deleteChapters(list)
            insertUseCases.insertChapters(list)
        }
    }

    fun reverseChapterInDB() {
        toggleAsc()
        book?.let { getLocalChaptersByPaging(isAsc = isAsc) }
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
            insertUseCases.insertChapters(chapters.reversed())
        }
    }

    fun toggleAsc() {
        this.isAsc = !this.isAsc
    }

    private suspend fun getLocalBookById(id: Long) {
        viewModelScope.launch {
            val book = getBookUseCases.findBookById(id = id)
            if (book != null) {
                this@ChapterDetailViewModel.book = book
                getLocalChaptersByPaging(isAsc = isAsc)
                getLastReadChapter(book)

            }
        }

    }

    private
    var getChapterJob: Job? = null
    fun getLocalChaptersByPaging(isAsc: Boolean = true) {
        val book = state.book
        getChapterJob?.cancel()
        getChapterJob = viewModelScope.launch {
            if (book != null) {
//                getChapterUseCase.getLocalChaptersByPaging(
//                    bookId = book.id,
//                    isAsc = isAsc,
//                    query = query
//                )
//                    .cachedIn(viewModelScope)
//                    .collect { snapshot ->
//                        _chapters.value = snapshot
//                    }
                getChapterUseCase.subscribeChaptersByBookId(
                    bookId = book.id,
                    isAsc = isAsc,
                    query = query
                ).collect { chapters ->
                    this@ChapterDetailViewModel.chapters = chapters.distinctBy { it.id }
                }

            }

        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
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

    lateinit
    var work: OneTimeWorkRequest
    fun downloadChapters(context: Context) {

        book?.let { book ->
            work =
                OneTimeWorkRequestBuilder<DownloadService>().apply {
                    setInputData(
                        Data.Builder().apply {
                            putLongArray(DOWNLOADER_Chapters_IDS,
                                this@ChapterDetailViewModel.selection.toLongArray())
                            putLongArray(DownloadService.DOWNLOADER_BOOKS_IDS,
                                longArrayOf(book.id))
                        }.build()
                    )
                    addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
                }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                    book.id + book.sourceId),
                ExistingWorkPolicy.REPLACE,
                work
            )
        }

    }

    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(R.string.error_unknown)
            )
        )
    }
}

