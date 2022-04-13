package org.ireader.presentation.feature_settings.presentation.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.ireader.core.ChapterParse
import org.ireader.core.ChaptersParse
import org.ireader.core.DetailParse
import org.ireader.core.utils.*
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.FetchType
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toBook
import org.ireader.domain.models.entities.toChapter
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.MangaInfo
import tachiyomi.source.model.Text
import timber.log.Timber
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**This is fake Alert **/
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val fetcherUseCase: FetchUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: CatalogStore,
    private val webpageImpl: WebViewPageStateImpl,
    val webView: WebView,
) : BaseViewModel(), WebViewPageState by webpageImpl {



    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val url = URLDecoder.decode(savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name())

        val fetcher = savedStateHandle.get<Int>(NavigationArgs.fetchType.name)
        if (sourceId != null && chapterId != null && bookId != null) {
            val source = extensions.get(sourceId)?.source
            if (source != null && source is CatalogSource) {
                this.source = source
            }
        }
        if (fetcher != null) {
            this.fetcher = mapFetcher(fetcher)
        }
        updateUrl(url)
        updateWebUrl(url = url)
        val listing = source?.getListings()?.map { it.name }
        if (listing?.contains(DetailParse().name) == true || listing?.contains(ChaptersParse().name) == true || listing?.contains(
                ChapterParse().name) == true
        ) {
            getBooksByKey()
        }

    }

    private val _uiFLow = MutableSharedFlow<WebPageEvents>()
    val uiFLow = _uiFLow.asSharedFlow()

    private var getBooksJob: Job? = null
    fun getBooksByKey(
        url: String = this@WebViewPageModel.url,
        title: String = this@WebViewPageModel.bookTitle,
    ) {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            getBookUseCases.subscribeBooksByKey(
                url,
                title
            ).collect { list ->
                val l = (availableBooks + list).distinctBy { it.link }
                availableBooks.clear()
                availableBooks.addAll(l)

//                val books = availableBooks.filter { it.title != title }.distinct()
//                availableBooks.clear()
//                availableBooks.addAll(books)
            }
        }

    }

    fun onEvent(event: WebPageEvents) {
        when (event) {
            is WebPageEvents.OnConfirm -> {
                when (fetcher) {
                    is FetchType.DetailFetchType -> {
                        viewModelScope.launch {
                            getBookDetailAndChapter(pageSource = event.pagingSource,
                                source = source!!,
                                url = event.url,
                                insert = true
                            )
                        }
                    }
                    is FetchType.ChaptersFetchType -> {
                        viewModelScope.launch {
                            getChapters(
                                pageSource = event.pagingSource,
                                source = source!!,
                                insert = true
                            )
                        }
                    }
                    is FetchType.ContentFetchType -> {
                        viewModelScope.launch {
                            getContentFromWebView(
                                pageSource = event.pagingSource,
                                source = source!!,
                                url = event.url,
                            )
                        }
                    }
                    else -> {}
                }
                viewModelScope.launch {
                    getBookDetailAndChapter(
                        event.pagingSource,
                        source = source!!,
                        url = event.url,
                    )
                }

            }
            else -> {}
        }
    }


    fun getContentFromWebView(pageSource: String, url: String, source: CatalogSource) {
        viewModelScope.launch(Dispatchers.IO) {
            showSnackBar(UiText.StringResource(R.string.trying_to_fetch_chapters_content))
            val list = mutableListOf<Chapter>()
            list.addAll(getChapterUseCase.findChaptersByKey(url))
            val content =
                source.getPageList(ChapterInfo(key = Constants.PARSE_CONTENT, name = pageSource))
                    .filterIsInstance<Text>().map { it.text }
            if (content.isNotEmpty() && list.isNotEmpty()) {
                insertUseCases.insertChapters(list.map { it.copy(content = content) })
                showSnackBar(UiText.DynamicString("${list.first().title} of ${list.first().title} was updated"))
            } else {
                showSnackBar(UiText.DynamicString("Failed to to get the content"))
            }
        }

    }

    suspend fun getChapters(
        pageSource: String,
        source: CatalogSource,
        insert: Boolean = false,
    ) {
        try {
            val fetchedChapters =
                source.getChapterList(MangaInfo(key = Constants.PARSE_CHAPTERS, title = pageSource))
            getBooksByKey(url = url)
            fetcher = FetchType.ChaptersFetchType

            if (fetchedChapters.isNotEmpty()) {
                if (insert) {
                    if (selectedBooks.isNotEmpty()) {
                        val books =
                            getBookUseCases.findBookByIds(availableBooks.filter { it.id in selectedBooks }
                                .map { it.id })

                        books.forEach { book ->
                            val chapters = getChapterUseCase.findChaptersByBookId(book.id)
                            val u = removeSameItemsFromList(chapters,
                                fetchedChapters.map {
                                    it.toChapter(book.id)
                                        .copy(dateFetch = Clock.System.now().toEpochMilliseconds())
                                }) {
                                it.link
                            }
                            insertUseCases.insertChapters(u)
                        }
                        _eventFlow.showSnackBar(UiText.DynamicString("${fetchedChapters.size} was merged with selected books."))

                    } else {
                        _eventFlow.showSnackBar(UiText.DynamicString("There is no book selected"))
                    }
                } else {
                    _uiFLow.emit(WebPageEvents.ShowModalSheet)
                }
            } else {
                _eventFlow.showSnackBar(UiText.DynamicString("Failed to find any chapters"))
            }

        } catch (e: Exception) {
            Timber.e(e)
        }


    }

    suspend fun getBookDetailAndChapter(
        pageSource: String,
        url: String,
        source: CatalogSource,
        insert: Boolean = false,
    ) = try {
        val detail =
            source.getMangaDetails(MangaInfo(key = Constants.PARSE_DETAIL, title = pageSource))
        val chapters =
            source.getChapterList(MangaInfo(key = Constants.PARSE_CHAPTERS, title = pageSource))
        getBooksByKey(title = detail.title, url = url)

        fetcher = FetchType.DetailFetchType

        if (insert) {
            selectedBooks.forEach { selectedBookId ->
                if (selectedBookId == -1L) {
                    val bookId = insertUseCases.insertBook(detail.toBook(source.id).copy(
                        link = url,
                        favorite = true,
                        lastUpdated = Clock.System.now().toEpochMilliseconds()))
                    insertUseCases.insertChapters(chapters.map {
                        it.toChapter(bookId).copy(dateFetch = currentTimeToLong())
                    })
                    _eventFlow.showSnackBar(UiText.DynamicString("${detail.title} was Added to library."))
                } else {
                    val l = getChapterUseCase.findChaptersByBookId(selectedBookId)
                    val u = removeSameItemsFromList(l,
                        chapters.map {
                            it.toChapter(selectedBookId)
                                .copy(dateFetch = Clock.System.now().toEpochMilliseconds())
                        }) {
                        it.link
                    }
                    insertUseCases.updateChaptersUseCase(
                        selectedBookId, u
                    )
                    _eventFlow.showSnackBar(UiText.DynamicString("${detail.title} was Added to library with ${u.size} chapters."))
                }
            }
        } else {
            _uiFLow.emit(WebPageEvents.ShowModalSheet)
        }
    } catch (e: Exception) {
        Timber.e(e)
    }

//
//
//
//
//
//
//
//
//
//
//        try {
//            showSnackBar(UiText.StringResource(R.string.trying_to_fetch))
//            val localBooks = mutableListOf<Book>()
//            localBooks.addAll(getBookUseCases.findBooksByKey(url))
//            val detail =
//                source.getMangaDetails(MangaInfo(key = Constants.PARSE_DETAIL, title = pageSource))
//            val chapter =
//                source.getChapterList(MangaInfo(key = Constants.PARSE_CHAPTERS, title = pageSource))
//
//            val newList = mutableListOf<Book>()
//            val localChapterList = mutableListOf<Chapter>()
//            if (localBooks.isNotEmpty()) {
//                localBooks.forEach {
//                    newList.add(updateBook(detail.toBook(source.id), it))
//                    val localChapters = getChapterUseCase.findChaptersByBookId(it.id)
//                    localChapterList.addAll(localChapters)
//                }
//            }
//            val uniqueList = mutableListOf<Chapter>()
//            localBooks.forEach { lBook ->
//                uniqueList.addAll(removeSameItemsFromList(oldList = localChapterList,
//                    newList = chapter.map { it.toChapter(lBook.id) },
//                    differentiateBy = {
//                        it.link
//                    }))
//            }
//            if (!update) {
//                _eventFlow.emit(WebPageEvents.showModalSheet("${detail.title} was fetched Successfully with ${chapter.size} chapters"))
//            }
//            if (update) {
//                if (localBooks.isNotEmpty()) {
//                    val bookId = insertUseCases.insertBooks(newList)
//                    newList.forEach {
//                        deleteUseCase.deleteChaptersByBookId(bookId = it.id)
//                        insertUseCases.insertChapters(uniqueList)
//                    }
//                    showSnackBar(UiText.DynamicString("${localBooks.first().title} of ${localBooks.first().title} was updated"))
//                    if (goTo) {
//                        viewModelScope.launch {
//                            _eventFlow.emit(
//                                WebPageEvents.GoTo(bookId = newList.first().id,
//                                    sourceId = source.id)
//                            )
//                        }
//                    }
//                } else {
//                    val book = detail.toBook(source.id).copy(
//                        lastUpdated = Calendar.getInstance().timeInMillis,
//                    )
//                    val bookId = insertUseCases.insertBook(book.copy(link = url))
//                    insertUseCases.insertChapters(uniqueList.map { it.copy(bookId = bookId) })
//                    showSnackBar(UiText.DynamicString("${detail.title} of ${detail.title} was updated"))
//                    if (goTo) {
//                        viewModelScope.launch {
//                            _eventFlow.emit(
//                                WebPageEvents.GoTo(bookId = bookId,
//                                    sourceId = source.id)
//                            )
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Timber.e(e)
//        }

    fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertChapter(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapter(chapter)
        }
    }

    fun toggleLoading(loading: Boolean) {
        isLoading = loading
    }

    fun updateUrl(url: String) {
        this.url = url
    }

    fun updateWebUrl(url: String) {
        webUrl = url

    }


}

interface WebViewPageState {
    var url: String
    var webUrl: String
    var fetcher: FetchType
    var source: CatalogSource?
    var isLoading: Boolean
    var bookId: Long?
    var bookTitle: String

    var availableBooks: SnapshotStateList<Book>
    val selectedBooks: SnapshotStateList<Long>
    val isAvailable: Boolean
    val isSelected: Boolean
}

open class WebViewPageStateImpl @Inject constructor() : WebViewPageState {
    override var url: String by mutableStateOf("")
    override var webUrl: String by mutableStateOf("")
    override var fetcher: FetchType by mutableStateOf(FetchType.LatestFetchType)
    override var source: CatalogSource? by mutableStateOf(null)
    override var isLoading: Boolean by mutableStateOf(false)
    override var bookId: Long? by mutableStateOf(null)
    override var bookTitle: String by mutableStateOf("")

    override var availableBooks: SnapshotStateList<Book> = mutableStateListOf()
    override var selectedBooks: SnapshotStateList<Long> = mutableStateListOf()
    override val isAvailable: Boolean by derivedStateOf { availableBooks.isNotEmpty() }
    override val isSelected: Boolean by derivedStateOf { selectedBooks.isNotEmpty() }
}

