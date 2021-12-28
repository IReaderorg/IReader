package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.data.network.models.ChapterPage
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContentUseCase {
    operator fun invoke(chapter: Chapter, source: Source) = flow<Resource<ChapterPage>> {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.fetchContent(chapter)
            Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
            emit(Resource.Success<ChapterPage>(content))
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