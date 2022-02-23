package org.ireader.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.utils.Resource
import org.ireader.source.core.Source
import org.ireader.source.models.MangaInfo

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): MangaInfo


    fun getAllExploreBookByPaging(
        source: Source,
        exploreType: ExploreType,
        query: String? = null,
    ): PagingSource<Int, Book>


    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: Source,
    ): Flow<Resource<List<String>>>


    @OptIn(ExperimentalPagingApi::class)
    fun getRemoteBooksByRemoteMediator(
        source: Source,
        exploreType: ExploreType,
        query: String?,
    ): Flow<PagingData<Book>>

}