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
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.removeSameItemsFromList
import org.ireader.domain.R
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.*
import org.ireader.domain.source.Extensions
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import org.ireader.source.core.Source
import org.ireader.source.sources.en.source_tower_deprecated.FetchType
import org.jsoup.Jsoup
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
    private val extensions: Extensions,
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
            state = state.copy(source = extensions.findSourceById(sourceId))
        }
        if (fetcher != null) {
            state = state.copy(fetcher = mapFetcher(fetcher))
        }
        updateUrl(url)
        updateWebUrl(url = url)

    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun getContentFromWebView(pageSource: String, url: String, source: Source) {
        viewModelScope.launch(Dispatchers.IO) {
            showSnackBar(UiText.StringResource(R.string.trying_to_fetch_chapters_content))
            val list = mutableListOf<Chapter>()
            list.addAll(getChapterUseCase.findChaptersByKey(url))
            val content = source.pageContentParse(Jsoup.parse(pageSource))
            if (content.isNotEmpty() && list.isNotEmpty()) {
                insertUseCases.insertChapters(list.map { it.copy(content = content) })
                showSnackBar(UiText.DynamicString("${list.first().title} of ${list.first().title} was updated"))
            } else {
                showSnackBar(UiText.DynamicString("Failed to to get the content"))
            }
        }

    }

    fun getExploredBook(pageSource: String, url: String, source: Source) {
        viewModelScope.launch {
            showSnackBar(UiText.StringResource(R.string.trying_to_fetch))
            val exploreBooks = source.latestParse(Jsoup.parse(pageSource))
            remoteKeyUseCase.deleteAllExploredBook()
            remoteKeyUseCase.deleteAllRemoteKeys()
            val keys = exploreBooks.books.map { book ->
                RemoteKeys(
                    id = book.title,
                    prevPage = 1,
                    nextPage = 2,
                    sourceId = source.id
                )
            }
            remoteKeyUseCase.insertAllRemoteKeys(keys = keys)
            remoteKeyUseCase.insertAllExploredBook(exploreBooks.books.map { it.toBook(source.id) })
            showSnackBar(UiText.DynamicString("return to explore screen to view books"))
        }
    }

    fun getBookDetailAndChapter(pageSource: String, url: String, source: Source) {
        viewModelScope.launch {
            showSnackBar(UiText.StringResource(R.string.trying_to_fetch))
            val localBooks = mutableListOf<Book>()
            localBooks.addAll(getBookUseCases.findBooksByKey(url))
            val detail = source.detailParse(Jsoup.parse(pageSource))
            val chapter = source.chaptersParse(Jsoup.parse(pageSource))
            if (detail.title.isNotEmpty() && chapter.isNotEmpty() && localBooks.isNotEmpty()) {
                val newList = mutableListOf<Book>()
                val localChapterList = mutableListOf<Chapter>()
                localBooks.forEach {
                    newList.add(updateBook(detail.toBook(source.id), it))
                    getChapterUseCase.subscribeChaptersByBookId(it.id).collect { localChapters ->
                        localChapterList.addAll(localChapters)
                    }
                }
                val uniqueList = mutableListOf<Chapter>()
                localBooks.forEach { lBook ->
                    uniqueList.addAll(removeSameItemsFromList(oldList = localChapterList,
                        newList = chapter.map { it.toChapter(lBook.id) },
                        differentiateBy = {
                            it.title
                        }))
                }
                if (localBooks.isNotEmpty()) {
                    insertUseCases.insertBooks(newList)
                    newList.forEach {
                        deleteUseCase.deleteChaptersByBookId(bookId = it.id)
                        insertUseCases.insertChapters(uniqueList)
                    }
                    showSnackBar(UiText.DynamicString("${localBooks.first().title} of ${localBooks.first().title} was updated"))
                } else {
                    val bookId = insertUseCases.insertBook(detail.toBook(source.id).copy(
                        favorite = true,
                        lastUpdated = System.currentTimeMillis(),
                        dataAdded = System.currentTimeMillis()))
                    insertUseCases.insertChapters(uniqueList.map { it.copy(bookId = bookId) })
                    showSnackBar(UiText.DynamicString("${detail.title} of ${detail.title} was added to library"))
                }
            } else {

                showSnackBar(UiText.DynamicString("Failed to to get the content"))
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
    val source: Source? = null,
    val isLoading: Boolean = false,
)