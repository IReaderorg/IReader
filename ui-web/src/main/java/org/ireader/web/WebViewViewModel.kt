package org.ireader.web

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Command
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**This is fake Alert **/
@SuppressLint("StaticFieldLeak")

@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insertUseCases: LocalInsertUseCases,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val extensions: CatalogStore,
    private val remoteUseCases: RemoteUseCases,
    private val savedStateHandle: SavedStateHandle,
    private val webpageImpl: WebViewPageStateImpl,
    private val constantCookiesStorage: ConstantCookiesStorage,
) : BaseViewModel(), WebViewPageState by webpageImpl {

    init {
        val url = URLDecoder.decode(
            savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name()
        )
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        bookId?.let {
            viewModelScope.launch {
                stateBook = getBookUseCases.findBookById(bookId)
            }
        }
        sourceId?.let {
            viewModelScope.launch {
                extensions.get(sourceId)?.source.let {
                    if (it is CatalogSource) {
                        source = it
                    }
                }
            }
        }
        chapterId?.let {
            viewModelScope.launch {
                stateChapter = getChapterUseCase.findChapterById(chapterId, null)
            }
        }
        updateUrl(url)
        updateWebUrl(url = url)
    }

    private val _uiFLow = MutableSharedFlow<WebPageEvents>()
    val uiFLow = _uiFLow.asSharedFlow()

    fun toggleLoading(loading: Boolean) {
        isLoading = loading
    }

    fun updateUrl(url: String) {
        this.url = url
    }

    fun updateCookies(url: String) {
//        val cf = CookieManager.getInstance()?.getCookie("cf_clearance")
//        Log.error { "CF = $cf" }
//        viewModelScope.launch {
//            cf?.let {
//                constantCookiesStorage.addCookie(Url(url), Cookie("cf_clearance", cf))
//            }
//        }
    }

    fun updateWebUrl(url: String) {
        webUrl = url
    }

    fun onEvent(event: WebPageEvents) {
        when (event) {
            is WebPageEvents.OnConfirm -> {
            }
            else -> {}
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getContentFromWebView(
        chapter: Chapter,
        webView: WebView,
        source: CatalogSource
    ) {
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            remoteUseCases.getRemoteReadingContent(
                chapter,
                source,
                onError = {
                    showSnackBar(it)
                    showSnackBar(UiText.DynamicString("Failed"))
                },
                onSuccess = {
                    if (it.content.isNotEmpty()) {
                        webChapter = it
                        insertChapter(it)
                        showSnackBar(UiText.DynamicString("Success"))
                    } else {
                        showSnackBar(UiText.DynamicString("Failed"))
                    }

                },
                commands = listOf(Command.Content.Fetch(url = url, pageSource))
            )

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChapters(
        book: Book,
        webView: WebView,
        source: CatalogSource,
    ) {
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            remoteUseCases.getRemoteChapters(
                book,
                source,
                onError = {
                    showSnackBar(it)
                },
                onSuccess = {
                    webChapters = it
                    if (it.isNotEmpty()) {
                        showSnackBar(UiText.DynamicString("Success"))
                        insertChapters(it.map { it.copy(bookId = book.id) })
                    } else {
                        showSnackBar(UiText.DynamicString("Failed"))
                    }
                },
                commands = listOf(Command.Chapter.Fetch(url = url, pageSource))
            )

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDetails(
        webView: WebView,
        source: CatalogSource,
        book: Book? = null,
    ) {
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.originalUrl ?: ""
            remoteUseCases.getBookDetail(
                book ?: Book(link = "", title = "", sourceId = source.id),
                source,
                onError = {
                    showSnackBar(UiText.DynamicString("Failed"))
                },
                onSuccess = {
                    if (!book?.title.isNullOrBlank()) {
                        webBook = it
                        insertBook(it.copy(favorite = true))
                        showSnackBar(UiText.DynamicString("Success"))
                    } else {
                        showSnackBar(UiText.DynamicString("Failed"))
                    }

                },
                commands = listOf(Command.Detail.Fetch(url = url, pageSource))
            )

        }
    }

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

    fun insertChapters(chapter: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertChapters(chapter)
        }
    }
}

interface WebViewPageState {
    var stateChapter: Chapter?
    var stateBook: Book?

    var url: String
    var webUrl: String
    var isLoading: Boolean

    var source: CatalogSource?

    var bookId: Long?
    var bookTitle: String

    var availableBooks: SnapshotStateList<Book>
    val selectedBooks: SnapshotStateList<Long>
    val isAvailable: Boolean
    val isSelected: Boolean

    var webChapter: Chapter?
    var webBook: Book?
    var webChapters: List<Chapter>
}

open class WebViewPageStateImpl @Inject constructor() : WebViewPageState {
    override var url: String by mutableStateOf("")
    override var webUrl: String by mutableStateOf("")

    override var isLoading: Boolean by mutableStateOf(false)

    override var source: CatalogSource? by mutableStateOf(null)

    override var bookId: Long? by mutableStateOf(null)
    override var bookTitle: String by mutableStateOf("")
    override var stateChapter: Chapter? by mutableStateOf(null)
    override var stateBook: Book? by mutableStateOf(null)

    override var availableBooks: SnapshotStateList<Book> = mutableStateListOf()
    override var selectedBooks: SnapshotStateList<Long> = mutableStateListOf()
    override val isAvailable: Boolean by derivedStateOf { availableBooks.isNotEmpty() }
    override val isSelected: Boolean by derivedStateOf { selectedBooks.isNotEmpty() }

    override var webChapter: Chapter? by mutableStateOf(null)
    override var webBook: Book? by mutableStateOf(null)
    override var webChapters: List<Chapter> by mutableStateOf(emptyList())
}
