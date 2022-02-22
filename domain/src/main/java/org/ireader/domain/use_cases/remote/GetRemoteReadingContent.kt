package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapterInfo
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.utils.Resource
import org.ireader.source.core.Source
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContent(private val remoteRepository: RemoteRepository) {
    operator fun invoke(
        chapter: Chapter,
        source: Source,
    ): Flow<Resource<List<String>>> = flow<Resource<List<String>>> {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.getContents(chapter.toChapterInfo())
            if (content.joinToString().isBlank()) {
                emit(Resource.Error<List<String>>(uiText = UiText.StringResource(R.string.cant_get_content)))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<List<String>>(content))

            }

        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: IOException) {
            emit(Resource.Error<List<String>>(uiText = UiText.StringResource(R.string.noInternetError)))
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        }
    }
}