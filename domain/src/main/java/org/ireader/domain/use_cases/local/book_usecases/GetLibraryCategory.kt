package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.ireader.common_data.repository.LibraryRepository
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.LibraryBook
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import org.ireader.core_api.source.model.MangaInfo
import javax.inject.Inject

class GetLibraryCategory @Inject internal constructor(
    private val libraryRepository: LibraryRepository
) {

    suspend fun await(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList()
    ): List<LibraryBook> {
        return when (categoryId) {
            Category.ALL_ID -> libraryRepository.findAll(sort)
            Category.UNCATEGORIZED_ID -> libraryRepository.findUncategorized(sort)
            else -> libraryRepository.findForCategory(categoryId, sort)
        }.filteredWith(filters)
    }

    fun subscribe(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList()
    ): Flow<List<LibraryBook>> {
        return when (categoryId) {
            Category.ALL_ID -> libraryRepository.subscribeAll(sort)
            Category.UNCATEGORIZED_ID -> libraryRepository.subscribeUncategorized(sort)
            else -> libraryRepository.subscribeToCategory(categoryId, sort)
        }.map { it.filteredWith(filters) }
    }

    private suspend fun List<LibraryBook>.filteredWith(filters: List<LibraryFilter>): List<LibraryBook> {
        if (filters.isEmpty()) return this
        var downloadedBooksId = emptyList<Long>()
        val validFilters =
            filters.filter { it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded }
        if (validFilters.map { it.type }.contains(LibraryFilter.Type.Downloaded)) {
            withContext(Dispatchers.IO) {
                downloadedBooksId = libraryRepository.findDownloadedBooks().map { it.id }
            }
        }
        var filteredList = this
        for (filter in validFilters) {
            val filterFn: (LibraryBook) -> Boolean = when (filter.type) {
                LibraryFilter.Type.Unread -> {
                    {
                        it.unread > 0
                    }
                }
                LibraryFilter.Type.Completed -> {
                    { book -> book.status == MangaInfo.COMPLETED }
                }
                LibraryFilter.Type.Downloaded -> {
                    {
                        it.id in downloadedBooksId
                    }
                }
            }
            filteredList = when (filter.value) {
                LibraryFilter.Value.Included -> filter(filterFn)
                LibraryFilter.Value.Excluded -> filterNot(filterFn)
                LibraryFilter.Value.Missing -> this
            }
        }

        return filteredList
    }
}
