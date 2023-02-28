package ireader.domain.usecases.local.book_usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.core.source.model.MangaInfo



class GetLibraryCategory  internal constructor(
    private val libraryRepository: LibraryRepository
) {

    suspend fun await(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList()
    ): List<LibraryBook> {
        return libraryRepository.findAll(sort).filter { books ->
            books.category.toLong() == categoryId
        }.filteredWith(filters)
    }

    fun subscribe(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList()
    ): Flow<List<LibraryBook>> {
      return  libraryRepository.subscribe(sort).map { it.filter { books ->
            books.category.toLong() == categoryId
        } }.map { it.filteredWith(filters) }
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
                        it.unreadCount > 0
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
