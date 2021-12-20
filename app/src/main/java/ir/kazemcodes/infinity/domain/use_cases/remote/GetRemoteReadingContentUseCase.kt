package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.models.Chapter
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContentUseCase {


    operator fun invoke(chapter: Chapter, source: ParsedHttpSource) = flow<Resource<String>> {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            emit(Resource.Loading())
            val req = source.pageContentRequest(chapter)
            val res = source.client.newCall(req).await()
            val content = source.pageContentParse(res)
//            val elements = api.fetchElements(url = link, headers = mutableMapOf(
//                Pair<String, String>("Referer", link)
//            ))
//            val readingContent = api.fetchReadingContent(elements)
            Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
            emit(Resource.Success<String>(content))
        } catch (e: HttpException) {
            emit(Resource.Error<String>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))

        } catch (e: IOException) {
            emit(Resource.Error<String>(message = "Couldn't Read Server, Check Your Internet Connection."))
        }catch (e: Exception) {
            emit(Resource.Error<String>(message = e.localizedMessage?:"An Unexpected Error Occurred"))
        }
    }

}