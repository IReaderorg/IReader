package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeys
import ir.kazemcodes.infinity.core.data.network.models.Source
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class ExploreRemoteMediator(
    private val source: Source,
    private val database: BookDatabase,
    private val exploreType: ExploreType,
    private val query: String? = null,
) : RemoteMediator<Int, ExploreBook>() {

    private val bookDao = database.libraryBookDao
    private val remoteKey = database.remoteKeysDao

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ExploreBook>,
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }

            val response = when (exploreType) {
                is ExploreType.Latest -> {
                    source.fetchLatest(currentPage)
                }
                is ExploreType.Popular -> {
                    source.fetchPopular(currentPage)
                }
                is ExploreType.Search -> {
                    source.fetchSearch(currentPage, query = query ?: "")
                }
            }


            val endOfPaginationReached = response.books.isEmpty()



            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    bookDao.deleteAllExploredBook()
                    remoteKey.deleteAllRemoteKeys()
                }
                val keys = response.books.map { book ->
                    RemoteKeys(
                        id = book.id,
                        prevPage = prevPage,
                        nextPage = nextPage
                    )
                }
                remoteKey.addAllRemoteKeys(remoteKeys = keys)
                bookDao.insertAllExploredBook(response.books.map {
                    it.toExploreBook().copy(isExploreMode = true)
                })
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ExploreBook>,
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                remoteKey.getRemoteKeys(id = id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, ExploreBook>,
    ): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(id = book.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, ExploreBook>,
    ): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(id = book.id)
            }
    }

}