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
import timber.log.Timber
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
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    Timber.d("1")
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return RemoteMediator.MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    Timber.d("2")
                    prevPage

                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return RemoteMediator.MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    Timber.d("3")
                    nextPage
                }
            }
            Timber.d("EXPLORE SCREEN :The request was made.")
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
            } else {
                source.getMangaList(sort = listing, currentPage)
            }

            Timber.d("EXPLORE SCREEN :The request was made: current page is $currentPage and it resulted to ${response.mangas.size}")
            Timber.d("4")
            Timber.d("5")

            val endOfPaginationReached = !response.hasNextPage
            Timber.d("6")
            Timber.d("7")

            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1
            Timber.d("8")
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    Timber.d("9")
                    remoteKey.deleteAllExploredBook()
                    Timber.d("10")
                    remoteKey.deleteAllRemoteKeys()
                    Timber.d("11")
                }
                val keys = response.mangas.map { book ->
                    Timber.d("12")
                    RemoteKeys(
                        title = book.title,
                        prevPage = prevPage,
                        nextPage = nextPage,
                        sourceId = source.id
                    )
                }
                Timber.d("13")
                remoteKey.insertAllRemoteKeys(remoteKeys = keys)
                Timber.d("14")
                remoteKey.insertAllExploredBook(response.mangas.map {
                    it.toBook(source.id,
                        tableId = 1)
                })
                Timber.d("15")
            }
            Timber.d("16")
            RemoteMediator.MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: UnknownHostException) {
            return RemoteMediator.MediatorResult.Error(Exception("There is no internet available,please check your internet connection"))
        } catch (e: IOException) {
            return RemoteMediator.MediatorResult.Error(e)
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
                remoteKey.getRemoteKeys(title = bookName)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, Book>,
    ): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(title = book.title)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, Book>,
    ): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { book ->
                remoteKey.getRemoteKeys(title = book.title)
            }
    }

}