package ir.kazemcodes.infinity.explore_feature.domain.use_case

import ir.kazemcodes.infinity.api_feature.network.ParsedHttpSource
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.IOException

class GetRemoteReadingContentUseCase {


    operator fun invoke(chapter: Chapter,source:ParsedHttpSource) = flow<Resource<String>> {
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