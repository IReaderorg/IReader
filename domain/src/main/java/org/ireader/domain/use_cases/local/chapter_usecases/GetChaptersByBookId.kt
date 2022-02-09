package org.ireader.infinity.core.domain.use_cases.local.chapter_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.utils.Resource

/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class GetChaptersByBookId(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Int,
        isAsc: Boolean? = null,
    ): Flow<Resource<List<Chapter>>> = flow {
        try {
            localChapterRepository.getChaptersByBookId(bookId = bookId, isAsc ?: true)
                .first { chapters ->
                    if (chapters != null) {
                        emit(Resource.Success<List<Chapter>>(data = chapters))
                        true

                    } else {
                        emit(Resource.Error<List<Chapter>>(uiText = UiText.StringResource(R.string.cant_get_content)))
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


class GetLocalChaptersByPaging(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        bookId: Int,
        isAsc: Boolean,
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