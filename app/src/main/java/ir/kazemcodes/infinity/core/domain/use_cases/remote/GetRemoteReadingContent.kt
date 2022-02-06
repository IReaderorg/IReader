package ir.kazemcodes.infinity.core.domain.use_cases.remote

import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContent(private val remoteRepository: RemoteRepository) {
    operator fun invoke( chapter: Chapter,
                         source: Source,
    ): Flow<Resource<ChapterPage>> = flow<Resource<ChapterPage>> {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.fetchContent(chapter)

            if (content.content.joinToString().isBlank() || content.content.contains(Constants.CLOUDFLARE_LOG)
            ) {
                emit(Resource.Error<ChapterPage>(uiText = UiText.StringResource(R.string.cant_get_content)))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<ChapterPage>(content))

            }

        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText =  UiText.ExceptionString(e)
            )
        } catch (e: IOException) {
            emit(Resource.Error<ChapterPage>(uiText =UiText.StringResource(R.string.noInternetError)))
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText =  UiText.ExceptionString(e)
            )
        }
    }
}