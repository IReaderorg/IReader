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
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class GetOneChapterById(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Long,
    ): Flow<Resource<Chapter>> = flow {
        try {
            localChapterRepository.getOneChapterById(chapterId = chapterId).first { chapters ->
                if (chapters != null) {
                    emit(Resource.Success<Chapter>(data = chapters))
                    true
                } else {
                    emit(Resource.Error<Chapter>(uiText = UiText.StringResource(R.string.no_chapter_found_error)))
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