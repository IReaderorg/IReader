package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import javax.inject.Inject

class SubscribeInLibraryBooks @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    operator fun invoke(
        sortType: LibrarySort,
        desc: Boolean,
        filter: List<LibraryFilter>,
    ): Flow<List<BookItem>> = flow {
//        localBookRepository.
//        subscribeAllInLibrary(
//            sortByLastRead = sortType == SortType.LastRead,
//            sortByAbs = sortType == SortType.Alphabetically,
//            sortByDateAdded = sortType == SortType.DateAdded,
//            sortByTotalChapters = sortType == SortType.TotalChapters,
//            desc = desc,
//            dateAdded = sortType == SortType.DateAdded,
//            dateFetched = sortType == SortType.DateFetched,
//            latestChapter = sortType == SortType.LatestChapter,
//            lastChecked = sortType == SortType.LastChecked
//        ).collect { books ->
//            val filteredBooks = mutableListOf<BookItem>()
//
//            withContext(Dispatchers.IO) {
//                if (filter.contains(FilterType.Unread)) {
//                    filteredBooks.addAll(localBookRepository.findUnreadBooks())
//                }
//                if (filter.contains(FilterType.Downloaded)) {
//                    filteredBooks.addAll(localBookRepository.findCompletedBooks())
//                }
//                if (filter.contains(FilterType.Completed)) {
//                    filteredBooks.addAll(localBookRepository.findDownloadedBooks())
//                }
//            }
//            emit(
//                books.filter { book ->
//                    if (filter.isNotEmpty()) {
//                        book in filteredBooks
//                    } else {
//                        true
//                    }
//                }
//            )

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
