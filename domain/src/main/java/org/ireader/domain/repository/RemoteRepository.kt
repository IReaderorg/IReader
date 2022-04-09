package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.utils.Resource
import tachiyomi.source.CatalogSource
import tachiyomi.source.Source
import tachiyomi.source.model.MangaInfo

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): MangaInfo



    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: CatalogSource,
    ): Flow<Resource<List<String>>>



}