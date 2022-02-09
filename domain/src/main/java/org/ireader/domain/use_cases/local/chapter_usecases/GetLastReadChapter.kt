package org.ireader.domain.use_cases.local.chapter_usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.utils.Resource

/**
 * Get latest read chapter
 *  * note: if nothing is found it return a resource of error
 */
class GetLastReadChapter(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Int,
    ): Flow<Resource<Chapter>> =
        flow {
            try {
                localChapterRepository.getLastReadChapter(bookId).first { chapter ->
                    if (chapter != null) {
                        emit(Resource.Success(data = chapter))
                        true
                    } else {
                        emit(Resource.Error(uiText = UiText.StringResource(R.string.cant_get_content)))
                        true
                    }
                }
            } catch (e: Exception) {
                Resource.Error<Resource<List<Book>>>(
                    uiText = UiText.ExceptionString(e)
                )
            }
        }
}


