package org.ireader.domain.use_cases.local.book_usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

/**
 * Get Books that are in library and explore mode is false.
 */
class SubscribeInLibraryBooksPagingData @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: FilterType,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                localBookRepository.getAllInLibraryPagingSource(sortType,
                    isAsc,
                    unreadFilter != FilterType.Disable)
            }
        ).flow
    }
}

class SubscribeInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        query: String,
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: FilterType,
    ): Flow<List<Book>> = flow {
        localBookRepository.subscribeAllInLibrary(
            sortByLastRead = sortType == SortType.LastRead,
            sortByAbs = sortType == SortType.Alphabetically,
            sortByDateAdded = sortType == SortType.DateAdded,
            sortByTotalChapter = sortType == SortType.TotalChapter,
            unread = FilterType.Unread == unreadFilter,
            isAsc = isAsc
        ).collect { books ->
            emit(books.filter { it.title.contains(query, true) })
        }
    }
}
