package org.ireader.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.utils.Resource
import org.ireader.source.core.HttpSource
import org.ireader.source.models.MangaInfo

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: HttpSource,
    ): MangaInfo


    fun getAllExploreBookByPaging(
        source: HttpSource,
        exploreType: ExploreType,
        query: String? = null,
    ): PagingSource<Int, Book>


    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: HttpSource,
    ): Flow<Resource<List<String>>>


    @OptIn(ExperimentalPagingApi::class)
    fun getRemoteBooksByRemoteMediator(
        source: HttpSource,
        exploreType: ExploreType,
        query: String?,
    ): Flow<PagingData<Book>>

}