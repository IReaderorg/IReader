package org.ireader.domain.view_models.settings.webview

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.utils.*
import org.ireader.domain.FetchType
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.entities.*
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.MangaInfo
import tachiyomi.source.model.Text
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
    private val remoteKeyUseCase: RemoteKeyUseCase,
) : ViewModel() {

    var state by mutableStateOf<WebViewPageState>(WebViewPageState())
        private set

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
                state = state.copy(source = source)
            }
        }
        if (fetcher != null) {
            state = state.copy(fetcher = mapFetcher(fetcher))
        }
        updateUrl(url)
        updateWebUrl(url = url)

    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun onEvent(event: WebPageEvents) {
        when (event) {
            is WebPageEvents.OnConfirm -> {
                viewModelScope.launch {
                    getBookDetailAndChapter(event.pagingSource,
                        source = state.source!!,
                        url = event.url,
                        update = true,
                        goTo = true
                    )
                }

            }
            is WebPageEvents.OnUpdate -> {
                viewModelScope.launch {
                    getBookDetailAndChapter(event.pagingSource,
                        source = state.source!!,
                        url = event.url,
                        update = true,
                        goTo = false
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


    suspend fun getBookDetailAndChapter(
        pageSource: String,
        url: String,
        source: CatalogSource,
        update: Boolean = false,
        goTo: Boolean = false,
    ) {
        showSnackBar(UiText.StringResource(R.string.trying_to_fetch))
        val localBooks = mutableListOf<Book>()
        localBooks.addAll(getBookUseCases.findBooksByKey(url))
        val detail =
            source.getMangaDetails(MangaInfo(key = Constants.PARSE_DETAIL, title = pageSource))
        val chapter =
            source.getChapterList(MangaInfo(key = Constants.PARSE_CHAPTERS, title = pageSource))

        val newList = mutableListOf<Book>()
        val localChapterList = mutableListOf<Chapter>()
        localBooks.forEach {
            newList.add(updateBook(detail.toBook(source.id), it))
            val localChapters = getChapterUseCase.findChaptersByBookId(it.id)
            localChapterList.addAll(localChapters)
        }
        val uniqueList = mutableListOf<Chapter>()
        localBooks.forEach { lBook ->
            uniqueList.addAll(removeSameItemsFromList(oldList = localChapterList,
                newList = chapter.map { it.toChapter(lBook.id) },
                differentiateBy = {
                    it.title
                }))
        }
        if (!update) {
            _eventFlow.emit(WebPageEvents.ShowDialog("${detail.title} was fetched Successfully with ${chapter.size} chapters"))
        }
        if (update) {
            if (localBooks.isNotEmpty()) {
                val bookId = insertUseCases.insertBooks(newList)
                newList.forEach {
                    deleteUseCase.deleteChaptersByBookId(bookId = it.id)
                    insertUseCases.insertChapters(uniqueList)
                }
                showSnackBar(UiText.DynamicString("${localBooks.first().title} of ${localBooks.first().title} was updated"))
                if (goTo) {
                    viewModelScope.launch {
                        _eventFlow.emit(
                            WebPageEvents.GoTo(bookId = newList.first().id,
                                sourceId = source.id)
                        )
                    }
                }
            } else {
                val bookId = insertUseCases.insertBook(detail.toBook(source.id).copy(
                    lastUpdated = System.currentTimeMillis(),
                ))
                insertUseCases.insertChapters(uniqueList.map { it.copy(bookId = bookId) })
                showSnackBar(UiText.DynamicString("${detail.title} of ${detail.title} was updated"))
                if (goTo) {
                    viewModelScope.launch {
                        _eventFlow.emit(
                            WebPageEvents.GoTo(bookId = bookId,
                                sourceId = source.id)
                        )
                    }
                }
            }
        }
    }

    fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(R.string.error_unknown)
            )
        )
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
        state = state.copy(isLoading = loading)
    }

    fun updateUrl(url: String) {

        state = state.copy(url = url)

    }

    fun updateWebUrl(url: String) {

        state = state.copy(webUrl = url)

    }


}

data class WebViewPageState(
    val url: String = "",
    val webUrl: String = "",
    val fetcher: FetchType = FetchType.LatestFetchType,
    val source: CatalogSource? = null,
    val isLoading: Boolean = false,
    val bookId: Long? = null,
)