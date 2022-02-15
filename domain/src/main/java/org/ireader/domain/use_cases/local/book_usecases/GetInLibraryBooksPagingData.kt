package org.ireader.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.view_models.library.LibraryPagingSource

/**
 * Get Books that are in library and explore mode is false.
 */
class GetInLibraryBooksPagingData(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: FilterType,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                LibraryPagingSource(localBookRepository, sortType = sortType, isAsc = isAsc,
                    unreadFilter = unreadFilter == FilterType.Unread)
//                localBookRepository.getAllInLibraryPagingSource(sortType,
//                    isAsc,
//                    unreadFilter != FilterType.Disable)
            }
        ).flow
    }
}