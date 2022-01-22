package ir.kazemcodes.infinity.core.presentation.components

import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import uy.kohesive.injekt.injectLazy

class WebViewFetcher(
    private val coroutineScope: CoroutineScope,
    private val localChapterRepository: LocalChapterRepository,
    private val localBookRepository: LocalBookRepository,
    private val url: String,
    private val fetcher: FetchType,
    private val source: Source
) {
    val webView by injectLazy<WebView>()

    private val _state = mutableStateOf<WebViewExtState>(WebViewExtState(webView = webView,
        url = url,
       fetcher = fetcher))
    val state: State<WebViewExtState> = _state


    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchInfo() :Flow<Resource<UiText.DynamicString>> = flow{
        emit(Resource.Loading())
            when (fetcher) {
                FetchType.Detail -> {
                    try {
                        val book = source.detailParse(Jsoup.parse(webView.getHtml()))
                        val chapters = source.chaptersParse(Jsoup.parse(webView.getHtml()))
                        if (!chapters.chapters.isNullOrEmpty()) {
                            _state.value = state.value.copy(book = book.book)
                            emit(Resource.Error("fetched something"))
                            getLocalBook(book.book)
                            getLocalChaptersByBookName(book.book.bookName)
                            emit(Resource.Error("looking for it in database"))
                            if (state.value.chapters != null && chapters.chapters.isNotEmpty()) {
                                val list = mutableListOf<Chapter>()
                                val sum = state.value.chapters!! + chapters.chapters

                                val uniqueList = sum.distinctBy {
                                    it.title
                                }.sortedBy { "s(\\d+)".toRegex().matchEntire(it.title)?.groups?.get(1)?.value?.toInt() }

                                list.addAll(uniqueList)
                                coroutineScope.launch(Dispatchers.IO) {
                                    deleteChapterDetails()
                                    insertChaptersToLocal(uniqueList)
                                }
                                emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${book.book.bookName} was fetched with ${chapters.chapters.size}  chapters")))

                            } else {
                                emit(Resource.Error("failed to get content try again in a few second"))
                            }

                        } else {
                            emit(Resource.Error("Failed to to get the content"))

                        }
                    }catch (e:Exception) {
                        emit(Resource.Error("Failed to to get the content"))
                    }


                }
                else -> {}
        }
    }
    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.insertChapters(
                chapters,
                state.value.book,
                source = source,
                inLibrary = state.value.book.inLibrary
            )
        }
    }

    private fun getLocalBook(book: Book) {
        localBookRepository.getLocalBookByName(state.value.book.bookName, sourceName = source.name).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                        )
                        insertBookDetailToLocal(result.data.copy(category = book.category,
                            status = book.status,
                            description = book.description,
                            author = book.author,
                            rating = book.rating))
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalChaptersByBookName(bookName: String) {
        localChapterRepository.getChapterByName(bookName, source.name)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data,
                            )
                        }
                    }
                    is Resource.Error -> {

                    }
                    is Resource.Loading -> {

                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun getLocalBookByName(bookName: String) {
        localBookRepository.getLocalBookByName(bookName,source.name).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                        )
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalChapterByName(bookName: String, chapterTitle: String) {
        localChapterRepository.getChapterByChapter(chapterTitle = chapterTitle,
            bookName = bookName,
            source = source.name).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _state.value = state.value.copy(
                            chapter = result.data,
                        )
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    fun insertBookDetailToLocal(book: Book) {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.insertBook(book)
        }
    }

    fun deleteChapterDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.deleteChapters(state.value.book.bookName, source.name)
        }
    }
}
data class WebViewExtState(
    val webView: WebView,
    val url: String,
    val book: Book = Book.create(),
    val books: List<Book> = emptyList<Book>(),
    val chapters: List<Chapter>? = null,
    val chapter: Chapter? = null,
    val fetcher: FetchType,
)