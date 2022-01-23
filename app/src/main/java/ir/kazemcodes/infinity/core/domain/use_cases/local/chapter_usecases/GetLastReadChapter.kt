package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
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
                emit(Resource.Loading())
                localChapterRepository.getLastReadChapter(bookId).first { chapter ->
                    if (chapter != null) {
                        emit(Resource.Success(data = chapter))
                        true
                    } else {
                        emit(Resource.Error(Constants.NO_CHAPTER_ERROR))
                        true
                    }
                }
            } catch (e: Exception) {
                emit(Resource.Error<Chapter>(message = e.localizedMessage
                    ?: Constants.NO_CHAPTER_ERROR))
            }
        }
}


