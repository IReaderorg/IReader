package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.utils.Resource
import ir.kazemcodes.infinity.data.network.models.Source
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContentUseCase {


    operator fun invoke(chapter: Chapter, source: Source) = flow<Resource<String>> {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            emit(Resource.Loading())
            val content = source.fetchContent(chapter)
            Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
            emit(Resource.Success<String>(content.content))
        } catch (e: HttpException) {
            emit(Resource.Error<String>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))

        } catch (e: IOException) {
            emit(Resource.Error<String>(message = "Couldn't Read Server, Check Your Internet Connection."))
        }catch (e: Exception) {
            emit(Resource.Error<String>(message = e.localizedMessage?:"An Unexpected Error Occurred"))
        }
    }

}