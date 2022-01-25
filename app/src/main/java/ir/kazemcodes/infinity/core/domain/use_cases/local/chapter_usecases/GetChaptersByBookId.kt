package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class GetChaptersByBookId(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Int,
        isAsc: Boolean? = null
    ): Flow<Resource<List<Chapter>>> = flow {
        try {
            emit(Resource.Loading())
            localChapterRepository.getChaptersByBookId(bookId = bookId,isAsc?:true).first { chapters ->
                if (chapters != null) {
                    emit(Resource.Success<List<Chapter>>(data = chapters))
                    true

                } else {
                    emit(Resource.Error<List<Chapter>>(message = Constants.NO_CHAPTERS_ERROR))
                    true
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error<List<Chapter>>(message = e.localizedMessage ?: Constants.NO_CHAPTER_ERROR))
        }
    }
}


class GetLocalChaptersByPaging(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Int,
        isAsc : Boolean,
    ): Flow<PagingData<Chapter>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_BIG_PAGE_SIZE,
                maxSize = Constants.MAX_BIG_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localChapterRepository.getLocalChaptersByPaging(bookId = bookId, isAsc = isAsc)
            }
        ).flow
    }
}