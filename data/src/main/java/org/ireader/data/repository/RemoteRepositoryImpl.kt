package org.ireader.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiText
import org.ireader.data.R
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.BookPage
import org.ireader.domain.models.source.ChapterPage
import org.ireader.domain.models.source.Source
import org.ireader.domain.utils.Resource
import org.ireader.infinity.core.domain.repository.RemoteRepository
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


}


