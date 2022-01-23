package ir.kazemcodes.infinity.core.domain.use_cases.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreRemoteMediator
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.select.Selector
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContent(private val remoteRepository: RemoteRepository) {
    operator fun invoke( chapter: Chapter,
                         source: Source,
    ): Flow<Resource<ChapterPage>> = flow<Resource<ChapterPage>> {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.fetchContent(chapter)

            if (content.content.joinToString()
                    .isBlank() || content.content.contains(Constants.CLOUDFLARE_LOG)
            ) {
                emit(Resource.Error<ChapterPage>(message = "Can't Get The Chapter Content."))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<ChapterPage>(content))

            }

        } catch (e: HttpException) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "An Unexpected Error Occurred."))
        } catch (e: IOException) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "An Unexpected Error Occurred"))
        }
    }
}