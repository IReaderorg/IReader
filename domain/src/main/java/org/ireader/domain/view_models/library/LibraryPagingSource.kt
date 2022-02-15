package org.ireader.domain.view_models.library

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

class LibraryPagingSource(
    private val localBookRepository: LocalBookRepository,
    private val sortType: SortType,
    private val isAsc: Boolean,
    private val unreadFilter: Boolean,
) : PagingSource<Int, Book>() {
    override suspend fun load(
        params: LoadParams<Int>,
    ): PagingSource.LoadResult<Int, Book> {
        return try {


            val books = localBookRepository.getAllInLibraryBooks(
                isAsc = isAsc,
                unreadFilter = unreadFilter,
                sortType = sortType
            ).first()
            LoadResult.Page(
                data = books,
                prevKey = null,
                nextKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}