package ir.kazemcodes.infinity.core.presentation.components

import android.webkit.WebView
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
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class WebViewFetcher(
    private val coroutineScope: CoroutineScope,
    private val localChapterRepository: LocalChapterRepository,
    private val localBookRepository: LocalBookRepository,
    private val url: String,
    private val fetcher: FetchType,
    private val source: Source,
    private val webView: WebView,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchInfo(
        localBook: Book? = null, localChapters: List<Chapter>? = null,
    ): Flow<Resource<UiText.DynamicString>> = flow {
        emit(Resource.Loading())
        when (fetcher) {
            FetchType.Detail -> {
                try {
                    val book = source.detailParse(Jsoup.parse(webView.getHtml()))
                    val chapters = source.chaptersParse(Jsoup.parse(webView.getHtml()))
                    if (!chapters.chapters.isNullOrEmpty()) {
                        emit(Resource.Error("trying"))
                        if (localChapters != null && chapters.chapters.isNotEmpty() && localBook?.bookName?.isNotBlank() == true) {
                            val list = mutableListOf<Chapter>()
                            val sum: List<Chapter> = localChapters + chapters.chapters

                            val uniqueList = sum.distinctBy {
                                it.title
                            }

                            list.addAll(uniqueList)
                            coroutineScope.launch(Dispatchers.IO) {
                                deleteChapterDetails(book = localBook)
                                insertChaptersToLocal(book = localBook,uniqueList)
                            }
                            emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${book.book.bookName} was fetched with ${chapters.chapters.size}  chapters")))

                        } else {
                            if (chapters.chapters.isNotEmpty()) {
                                emit(Resource.Error("Failed to to get the content"))

                            }
                            if (localChapters == null) {
                                emit(Resource.Error("try again in a few second"))

                            }
                        }

                    } else {
                        emit(Resource.Error("Failed to to get the content"))

                    }
                } catch (e: Exception) {
                    emit(Resource.Error("Failed to to get the content"))
                }


            }
            else -> {}
        }
    }

    fun insertChaptersToLocal(book: Book, chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.insertChapters(
                chapters,
                book,
                source = source,
                inLibrary = book.inLibrary
            )
        }
    }


    fun insertBookDetailToLocal(book: Book) {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.insertBook(book)
        }
    }

    fun deleteChapterDetails(book: Book) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.deleteChapters(book.bookName, source.name)
        }
    }

}