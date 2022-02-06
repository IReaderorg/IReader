package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases

import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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


