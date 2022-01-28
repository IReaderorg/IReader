package ir.kazemcodes.infinity.core.domain.use_cases.remote

import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.asString
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
                emit(Resource.Error<ChapterPage>(uiText = UiText.DynamicString("Can't Get The Chapter Content.").asString()))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<ChapterPage>(content))

            }

        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.DynamicString(e.localizedMessage ?: Constants.UNKNOWN_ERROR).asString()
            )
        } catch (e: IOException) {
            emit(Resource.Error<ChapterPage>(uiText = UiText.noInternetError()))
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.DynamicString(e.localizedMessage ?: Constants.UNKNOWN_ERROR).asString()
            )
        }
    }
}