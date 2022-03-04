package org.ireader.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.utils.Resource
import tachiyomi.source.CatalogSource
import tachiyomi.source.Source
import tachiyomi.source.model.Listing
import tachiyomi.source.model.MangaInfo

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): MangaInfo


    fun getAllExploreBookByPaging(
        source: CatalogSource,
        listing: Listing,
        query: String? = null,
    ): PagingSource<Int, Book>


    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: CatalogSource,
    ): Flow<Resource<List<String>>>


    @OptIn(ExperimentalPagingApi::class)
    fun getRemoteBooksByRemoteMediator(
        source: CatalogSource,
        listing: Listing,
        query: String?,
    ): Flow<PagingData<Book>>

}