package org.ireader.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.utils.Resource
import tachiyomi.source.CatalogSource
import tachiyomi.source.Source
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import tachiyomi.source.model.MangaInfo

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): MangaInfo


    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: CatalogSource,
    ): Flow<Resource<List<String>>>


    @OptIn(ExperimentalPagingApi::class)
    fun getRemoteBooksByRemoteMediator(
        source: CatalogSource,
        listing: Listing?,
        filters: List<Filter<*>>?,
        query: String?,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE,
        maxSize: Int = Constants.MAX_PAGE_SIZE,
    ): Flow<PagingData<Book>>

}