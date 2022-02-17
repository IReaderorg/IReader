package org.ireader.domain.use_cases.fetchers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.core.utils.removeSameItemsFromList
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.fromBookInfo
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.updateBook
import org.ireader.domain.models.source.Source
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.utils.Resource
import org.jsoup.Jsoup

class FetchBookDetailAndChapterDetailFromWebView {

    operator fun invoke(
        pageSource: String,
        localChapters: List<Chapter>? = null,
        localBook: Book? = null,
        source: Source,
        insertUseCases: LocalInsertUseCases,
        deleteUseCase: DeleteUseCase,
    ): Flow<Resource<UiText.DynamicString>> = flow {
        try {
            val bookFromPageSource = source.detailParse(Jsoup.parse(pageSource))
            val chaptersFromPageSource = source.chaptersParse(Jsoup.parse(pageSource))
            if (!chaptersFromPageSource.isNullOrEmpty()) {
                emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.trying_to_fetch)))
                if (localChapters != null && chaptersFromPageSource.isNotEmpty() && localBook?.title?.isNotBlank() == true) {
                    val uniqueList = removeSameItemsFromList(oldList = localChapters,
                        newList = chaptersFromPageSource,
                        differentiateBy = {
                            it.title
                        })
                    deleteUseCase.deleteChaptersByBookId(bookId = localBook.id)
                    insertUseCases.insertBook(
                        updateBook(bookFromPageSource.fromBookInfo(source.sourceId), localBook)
                    )
                    insertUseCases.insertChapters(uniqueList.map {
                        it.copy(
                            bookId = localBook.id,
                            inLibrary = localBook.favorite,
                            dateFetch = System.currentTimeMillis(),
                        )
                    })
                    emit(Resource.Success<UiText.DynamicString>(UiText.DynamicString("${localBook.title} was fetched with ${chaptersFromPageSource.size}   chapters")))

                } else {
                    if (chaptersFromPageSource.isNotEmpty()) {
                        emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.failed_to_get_content)))

                    }
                    if (localChapters == null) {
                        emit(Resource.Error<UiText.DynamicString>(UiText.StringResource(R.string.trying_in_few_second)))

                    }
                }

            } else {
                emit(Resource.Error(UiText.StringResource(R.string.failed_to_get_content)))

            }
        } catch (e: Exception) {
            emit(Resource.Error(UiText.StringResource(R.string.failed_to_get_content)))
        }
    }
}