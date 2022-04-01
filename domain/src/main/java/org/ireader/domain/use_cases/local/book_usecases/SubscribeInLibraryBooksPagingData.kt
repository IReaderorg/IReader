package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class SubscribeInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        query: String,
        sortType: SortType,
        isAsc: Boolean,
        filter: List<FilterType>,
    ): Flow<List<Book>> = flow {
        localBookRepository.subscribeAllInLibrary(
            sortByLastRead = sortType == SortType.LastRead,
            sortByAbs = sortType == SortType.Alphabetically,
            sortByDateAdded = sortType == SortType.DateAdded,
            sortByTotalChapters = sortType == SortType.TotalChapters,
            unread = FilterType.Unread in filter,
            isAsc = isAsc,
            complete = FilterType.Completed in filter,
            dateAdded = sortType == SortType.DateAdded,
            dateFetched = sortType == SortType.DateFetched,
            downloaded = FilterType.Downloaded in filter,
            latestChapter = sortType == SortType.LatestChapter,
        ).collect { books ->
            emit(books.filter { it.title.contains(query, true) })
        }
    }
}
