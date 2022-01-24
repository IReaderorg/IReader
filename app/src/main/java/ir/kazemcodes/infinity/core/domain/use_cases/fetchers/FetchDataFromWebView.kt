package ir.kazemcodes.infinity.core.domain.use_cases.fetchers

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

class FetchBookDetailAndChapterDetailFromWebView {

    operator fun invoke(
        pageSource:String,
        localChapters: List<Chapter>? = null,
        localBook : Book?=null,
        source: Source,
        insertUseCases: LocalInsertUseCases,
        deleteUseCase: DeleteUseCase,
    ): Flow<Resource<UiText.DynamicString>> = flow {
        emit(Resource.Loading<UiText.DynamicString>())
                try {
                    val bookFromPageSource = source.detailParse(Jsoup.parse(pageSource))
                    val chaptersFromPageSource = source.chaptersParse(Jsoup.parse(pageSource))
                    if (!chaptersFromPageSource.chapters.isNullOrEmpty()) {
                        emit(Resource.Error<UiText.DynamicString> ("trying"))
                        if (localChapters != null && chaptersFromPageSource.chapters.isNotEmpty() && localBook?.bookName?.isNotBlank() == true) {
                            val list = mutableListOf<Chapter>()
                            val sum: List<Chapter> = localChapters + chaptersFromPageSource.chapters

                            val uniqueList = sum.distinctBy {
                                it.title
                            }

                            list.addAll(uniqueList)
                            deleteUseCase.deleteChaptersByBookId(bookId = localBook.id)
                            insertUseCases.insertChapters(uniqueList.map { it.copy(
                                bookId = localBook.id,
                                bookName = localBook.bookName,
                                inLibrary = localBook.inLibrary,
                            ) })

                            emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${bookFromPageSource.book.bookName} was fetched with ${chaptersFromPageSource.chapters.size}   chapters")))

                        } else {
                            if (chaptersFromPageSource.chapters.isNotEmpty()) {
                                emit(Resource.Error<UiText.DynamicString>("Failed to to get the content"))

                            }
                            if (localChapters == null) {
                                emit(Resource.Error<UiText.DynamicString> ("try again in a few second"))

                            }
                        }

                    } else {
                        emit(Resource.Error("Failed to to get the content"))

                    }
                } catch (e: Exception) {
                    emit(Resource.Error("Failed to to get the content"))
                }
    }
}