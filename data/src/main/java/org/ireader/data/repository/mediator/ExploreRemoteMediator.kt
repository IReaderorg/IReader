package org.ireader.data.repository.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import org.ireader.core.utils.UiText
import org.ireader.data.local.AppDatabase
import org.ireader.domain.R
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.toBook
import retrofit2.HttpException
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@ExperimentalPagingApi
class ExploreRemoteMediator(
    private val source: CatalogSource,
    private val database: AppDatabase,
    private val listing: Listing?,
    private val filters: List<Filter<*>>?,
    private val query: String? = null,
) : RemoteMediator<Int, Book>() {

    private val remoteKey = database.remoteKeysDao
    private val chapterDao = database.libraryChapterDao
    private val libraryDao = database.libraryBookDao
    private val Localbook = database.libraryBookDao


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Book>,
    ): RemoteMediator.MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {
                    chapterDao.deleteNotInLibraryChapters()
                    libraryDao.deleteNotInLibraryBooks()
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return RemoteMediator.MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return RemoteMediator.MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }
            val response = if (query != null) {
                if (query.isNotBlank()) {
                    source.getMangaList(filters = listOf(Filter.Title()
                        .apply { this.value = query }),
                        page = currentPage)
                } else {
                    throw Exception(UiText.StringResource(R.string.query_must_not_be_empty)
                        .toString())
                }
            } else if (filters != null) {
                source.getMangaList(filters = filters, currentPage)
            } else if (listing != null) {
                source.getMangaList(sort = listing, currentPage)
            } else {
                throw Exception(UiText.StringResource(R.string.no_filter_applied)
                    .toString())
            }

            val endOfPaginationReached = !response.hasNextPage


            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKey.deleteAllExploredBook()
                    remoteKey.deleteAllRemoteKeys()
                }
                val keys = response.mangas.map { book ->
                    RemoteKeys(
                        id = book.title,
                        prevPage = prevPage,
                        nextPage = nextPage,
                        sourceId = source.id
                    )
                }


                remoteKey.insertAllRemoteKeys(remoteKeys = keys)
                remoteKey.insertAllExploredBook(response.mangas.map {
                    it.toBook(source.id,
                        tableId = 1)
                })
            }
            RemoteMediator.MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: UnknownHostException) {
            return RemoteMediator.MediatorResult.Error(Exception("There is no internet available,please check your internet connection"))
        } catch (e: IOException) {
            return RemoteMediator.MediatorResult.Error(e)
        } catch (e: HttpException) {
            return RemoteMediator.MediatorResult.Error(e)
        } catch (e: SSLHandshakeException) {
            return RemoteMediator.MediatorResult.Error(e)
        } catch (e: Exception) {
            return RemoteMediator.MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Book>,
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.title?.let { bookName ->
                remoteKey.getRemoteKeys(id = bookName)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, Book>,
    ): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(id = book.title)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, Book>,
    ): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(id = book.title)
            }
    }

}