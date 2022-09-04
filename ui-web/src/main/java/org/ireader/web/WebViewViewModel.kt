package org.ireader.web

import android.webkit.WebView
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.accompanist.web.WebViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core_api.source.model.Command
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.ui_web.R
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insertUseCases: LocalInsertUseCases,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val extensions: CatalogStore,
    private val remoteUseCases: RemoteUseCases,
    private val savedStateHandle: SavedStateHandle,
    private val webpageImpl: WebViewPageStateImpl,
) : BaseViewModel(), WebViewPageState by webpageImpl {

    init {
        val url = URLDecoder.decode(
            savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name()
        )
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        enableChapterFetch = savedStateHandle.get<Boolean>("enableChapterFetch") == true
        enableChaptersFetch = savedStateHandle.get<Boolean>("enableChaptersFetch") == true
        enableBookFetch = savedStateHandle.get<Boolean>("enableBookFetch") == true
        bookId?.let {
            viewModelScope.launch {
                stateBook = getBookUseCases.findBookById(bookId)
            }
        }
        sourceId?.let {
            viewModelScope.launch {
                extensions.get(sourceId)?.let {
                    catalog = it
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

    fun toggleLoading(loading: Boolean) {
        isLoading = loading
    }

    private fun updateUrl(url: String) {
        this.url = url
    }

    fun updateWebUrl(url: String) {
        webUrl = url
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getContentFromWebView(
        chapter: Chapter,
        webView: WebView,
    ) {
        val catalog = catalog
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            remoteUseCases.getRemoteReadingContent(
                chapter,
                catalog,
                onError = {
                    showSnackBar(it)
                    showSnackBar(UiText.StringResource(R.string.failed))
                },
                onSuccess = { result ->
                    if (result.content.isNotEmpty()) {
                        webChapter = result
                        insertChapter(result)
                        showSnackBar(UiText.StringResource(R.string.success))
                    } else {
                        showSnackBar(UiText.StringResource(R.string.failed))
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
    ) {
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            val localChapters = getChapterUseCase.findChaptersByBookId(book.id)
            remoteUseCases.getRemoteChapters(
                book,
                catalog,
                onError = {
                    showSnackBar(it)
                },
                onSuccess = { result ->

                    webChapters = result
                    if (result.isNotEmpty()) {
                        showSnackBar(UiText.StringResource(R.string.success))
                        insertChapters(result.map { it.copy(bookId = book.id) })
                    } else {
                        showSnackBar(UiText.StringResource(R.string.failed))
                    }
                },
                commands = listOf(Command.Chapter.Fetch(url = url, pageSource)),
                oldChapters = localChapters
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDetails(
        webView: WebView,
        book: Book? = null,
    ) {
        viewModelScope.launch {
            val pageSource = webView.getHtml()
            val url = webView.originalUrl ?: ""
            remoteUseCases.getBookDetail(
                book ?: Book(key = "", title = "", sourceId = source?.id ?: 0),
                catalog,
                onError = {
                    showSnackBar(UiText.StringResource(R.string.failed))
                },
                onSuccess = { result ->
                    if (result.title.isNotBlank()) {
                        webBook = result
                        insertBook(result.copy(favorite = true))
                        showSnackBar(UiText.StringResource(R.string.success))
                    } else {
                        showSnackBar(UiText.StringResource(R.string.failed))
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

    var webView: WebView?
    var webViewState: WebViewState?

    var url: String
    var webUrl: String
    var isLoading: Boolean

    val source: org.ireader.core_api.source.CatalogSource?
    var catalog: CatalogLocal?

    var bookId: Long?
    var bookTitle: String

    var availableBooks: SnapshotStateList<Book>
    val selectedBooks: SnapshotStateList<Long>
    val isAvailable: Boolean
    val isSelected: Boolean

    var webChapter: Chapter?
    var webBook: Book?
    var webChapters: List<Chapter>

    var enableChapterFetch: Boolean
    var enableChaptersFetch: Boolean
    var enableBookFetch: Boolean
}

open class WebViewPageStateImpl @Inject constructor() : WebViewPageState {
    override var url: String by mutableStateOf("")
    override var webUrl: String by mutableStateOf("")
    override var webView: WebView? by mutableStateOf(null)
    override var webViewState: WebViewState? by mutableStateOf(null)

    override var isLoading: Boolean by mutableStateOf(false)

    override val source: org.ireader.core_api.source.CatalogSource? by derivedStateOf {
        val source = catalog?.source
        if (source is org.ireader.core_api.source.CatalogSource) source else null
    }
    override var catalog: CatalogLocal? by mutableStateOf(null)

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
    override var enableChapterFetch: Boolean by mutableStateOf(false)
    override var enableChaptersFetch: Boolean by mutableStateOf(false)
    override var enableBookFetch: Boolean by mutableStateOf(false)
}
