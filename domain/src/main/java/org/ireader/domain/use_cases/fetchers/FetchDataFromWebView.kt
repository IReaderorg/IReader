package org.ireader.domain.use_cases.fetchers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_extensions.UiText
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.utils.Resource
import org.ireader.core_api.source.HttpSource
import javax.inject.Inject


class FetchBookDetailAndChapterDetailFromWebView @Inject constructor() {

    operator fun invoke(
        pageSource: String,
        localChapters: List<Chapter>? = null,
        localBook: Book? = null,
        source: HttpSource,
        insertUseCases: LocalInsertUseCases,
        deleteUseCase: DeleteUseCase,
        url: String? = null,
    ): Flow<Resource<org.ireader.common_extensions.UiText.DynamicString>> = flow {
        //TODO fix this part later
//        try {
//            val bookFromPageSource = source.detailParse(Jsoup.parse(pageSource))
//            val chaptersFromPageSource = source.chaptersParse(Jsoup.parse(pageSource))
//            if (!chaptersFromPageSource.isNullOrEmpty()) {
//                emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.trying_to_fetch)))
//                if (localChapters != null && localBook?.title == bookFromPageSource.title && chaptersFromPageSource.isNotEmpty() && localBook.title.isNotBlank()) {
//                    val uniqueList = removeSameItemsFromList(oldList = localChapters,
//                        newList = chaptersFromPageSource.map { it.toChapter(localBook.id) },
//                        differentiateBy = {
//                            it.title
//                        })
//                    deleteUseCase.deleteChaptersByBookId(bookId = localBook.id)
//                    if (localBook.id != 0L) {
//                        insertUseCases.insertBook(
//                            updateBook(bookFromPageSource.toBook(source.id), localBook)
//                        )
//                    } else {
//                        insertUseCases.insertBook(
//                            updateBook(bookFromPageSource.toBook(source.id), localBook)
//                        )
//                    }
//
//                    insertUseCases.insertChapters(uniqueList.map {
//                        it.copy(
//                            bookId = localBook.id,
//                            inLibrary = localBook.favorite,
//                            dateFetch = System.currentTimeMillis(),
//                        )
//                    })
//                    emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${localBook.title.ifBlank { bookFromPageSource.title }} was fetched with ${chaptersFromPageSource.size}   chapters")))
//
//                } else if (!chaptersFromPageSource.isNullOrEmpty() && chaptersFromPageSource.isNotEmpty()) {
//                    if (chaptersFromPageSource.isNotEmpty() && bookFromPageSource.title.isNotBlank()) {
//                        var book = bookFromPageSource.toBook(source.id)
//                            .copy(
//                                favorite = true,
//                                dataAdded = System.currentTimeMillis(),
//                                lastUpdated = System.currentTimeMillis(),
//                            )
//                        if (url != null && url.isNotBlank()) {
//                            book = book.copy(link = url)
//                        }
//                        val insertedBookId =
//                            insertUseCases.insertBook(book)
//
//                        insertUseCases.insertChapters(chaptersFromPageSource.map {
//                            it.toChapter(insertedBookId).copy(
//                                bookId = insertedBookId,
//                                inLibrary = true,
//                                dateFetch = System.currentTimeMillis(),
//                            )
//                        })
//                        emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${bookFromPageSource.title} was fetched with ${chaptersFromPageSource.size}   chapters \n And Added to Library")))
//
//
//                    }
//                } else {
//                    if (chaptersFromPageSource.isNotEmpty()) {
//                        emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.failed_to_get_content)))
//
//                    }
//                    if (localChapters == null) {
//                        emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.trying_in_few_second)))
//
//                    }
//                }
//
//            } else {
//                emit(Resource.Error(UiText.StringResource(R.string.failed_to_get_content)))
//
//            }
//        } catch (e: Throwable) {
//            emit(Resource.Error(UiText.StringResource(R.string.failed_to_get_content)))
//        }
    }
}