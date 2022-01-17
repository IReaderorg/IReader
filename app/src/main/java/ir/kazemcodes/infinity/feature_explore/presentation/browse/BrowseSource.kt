package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import retrofit2.HttpException
import java.io.IOException

class BrowseSource(private val source: Source,private val exploreType: ExploreType) : PagingSource<Int, Book>() {

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {

        val page = params.key ?: 1

        return try {
            source.fetchLatest(page)
            val response = when (exploreType) {
                is ExploreType.Latest ->{
                    source.fetchLatest(page)
                }
                is ExploreType.Popular -> {
                    source.fetchPopular(page)
                }
            }
            LoadResult.Page(
                data = response.books,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.books.isEmpty()) null else page + 1
            )

        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}