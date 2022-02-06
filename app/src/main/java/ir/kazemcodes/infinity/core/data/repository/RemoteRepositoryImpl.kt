package ir.kazemcodes.infinity.core.data.repository

import android.content.Context
import android.webkit.WebView
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeysDao
import ir.kazemcodes.infinity.core.data.network.models.BookPage
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class RemoteRepositoryImpl(
    private val remoteKeysDao: RemoteKeysDao,
) : RemoteRepository {



    override suspend fun getRemoteBookDetail(book: Book, source: Source): BookPage {
        return source.fetchBook(book)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getAllExploreBookByPaging(
        source: Source,
        exploreType: ExploreType,
        query: String?,
    ): PagingSource<Int, Book> {
            return remoteKeysDao.getAllExploreBookByPaging()
    }


    override fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: Source,
    ): Flow<Resource<ChapterPage>> = flow<Resource<ChapterPage>> {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.fetchContent(chapter)

            if (content.content.joinToString()
                    .isBlank() || content.content.contains(Constants.CLOUDFLARE_LOG)
            ) {
                emit(Resource.Error<ChapterPage>(uiText = UiText.StringResource(R.string.cant_get_content)))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<ChapterPage>(content))

            }

        } catch (e: HttpException) {
            emit(Resource.Error<ChapterPage>(uiText = UiText.ExceptionString(e)))
        } catch (e: IOException) {
            emit(Resource.Error<ChapterPage>(uiText = UiText.StringResource(R.string.noInternetError)))
        } catch (e: Exception) {
            emit(Resource.Error<ChapterPage>(uiText = UiText.ExceptionString(e)))
        }
    }

    override suspend fun downloadChapter(
        book: Book,
        source: Source,
        chapters: List<Chapter>,
        factory: (Context) -> WebView,
        totalRetries: Int,
    ): Flow<Chapter> {
        TODO("Not yet implemented")
    }

}


