package ir.kazemcodes.infinity.core.domain.repository

import android.content.Context
import android.webkit.WebView
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.network.models.BookPage
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): BookPage


    fun getAllExploreBookByPaging(
        source: Source,
        exploreType: ExploreType,
        query:String?=null,
    ): PagingSource<Int, Book>


    fun getRemoteReadingContentUseCase(chapter: Chapter, source: Source): Flow<Resource<ChapterPage>>

    suspend fun downloadChapter(
        book: Book,
        source: Source,
        chapters: List<Chapter>,
        factory: (Context) -> WebView,
        totalRetries: Int = 3,
    ): Flow<Chapter>

}