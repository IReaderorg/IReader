package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class SubscribeInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(
        sortType: SortType,
        desc: Boolean,
        filter: List<FilterType>,
    ): Flow<List<Book>> = flow {
        localBookRepository.subscribeAllInLibrary(
            sortByLastRead = sortType == SortType.LastRead,
            sortByAbs = sortType == SortType.Alphabetically,
            sortByDateAdded = sortType == SortType.DateAdded,
            sortByTotalChapters = sortType == SortType.TotalChapters,
            desc = desc,
            dateAdded = sortType == SortType.DateAdded,
            dateFetched = sortType == SortType.DateFetched,
            latestChapter = sortType == SortType.LatestChapter,
            lastChecked = sortType == SortType.LastChecked
        ).collect { books ->
            val filteredBooks = mutableListOf<Book>()

            withContext(Dispatchers.IO) {
                when {
                    filter.contains(FilterType.Unread) -> {
                        filteredBooks.addAll(localBookRepository.findUnreadBooks())
                    }
                    filter.contains(FilterType.Downloaded) -> {
                        filteredBooks.addAll(localBookRepository.findCompletedBooks())
                    }
                    filter.contains(FilterType.Completed) -> {
                        filteredBooks.addAll(localBookRepository.findDownloadedBooks())
                    }
                    else -> {}
                }
            }
            emit(books.filter { book ->
                if (filter.isNotEmpty()) {
                    book in filteredBooks
                } else {
                    true
                }
            })


//            if (filter.isNotEmpty()) {
//                emit(books.filter { it.title.contains(query, true) }.filter { it in filteredBooks })
//
//            } else {
//                emit(books.filter { it.title.contains(query, true) })
//            }

//                when(isAsc) {
//                    true -> {
//                        when (sortType) {
//                            SortType.Alphabetically -> this.sortedBy { it.title }
//                            SortType.DateAdded -> this.sortedBy { it.dataAdded }
//                            else -> {}
//                        }
//                    }
//                    else -> {}
//                }
        }
    }
}
