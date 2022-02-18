package org.ireader.domain.use_cases.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.models.entities.Book
import javax.inject.Inject

class LibraryMediator @Inject constructor(
    private val dao: LibraryBookDao,
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val currentPage = params.key ?: 1
        return try {
            val response = dao.findAllInLibraryBooks()
            val endOfPaginationReached = true
            if (response.isNotEmpty()) {
                LoadResult.Page(
                    data = response,
                    prevKey = if (currentPage == 1) null else currentPage - 1,
                    nextKey = if (endOfPaginationReached) null else currentPage + 1
                )
            } else {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition
    }
}