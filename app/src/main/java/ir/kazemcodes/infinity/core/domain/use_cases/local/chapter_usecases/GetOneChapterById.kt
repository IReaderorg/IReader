package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class GetOneChapterById(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Int,
    ): Flow<Resource<Chapter>> = flow {
        try {
            emit(Resource.Loading())
            localChapterRepository.getOneChapterById(chapterId = chapterId).first { chapters ->
                if (chapters != null) {
                    emit(Resource.Success<Chapter>(data = chapters))
                    true
                } else {
                    emit(Resource.Error<Chapter>(message = Constants.NO_CHAPTER_ERROR))
                    true
                }

            }
        } catch (e: Exception) {
            emit(Resource.Error<Chapter>(message = e.localizedMessage
                ?: Constants.NO_CHAPTER_ERROR))
        }
    }
}