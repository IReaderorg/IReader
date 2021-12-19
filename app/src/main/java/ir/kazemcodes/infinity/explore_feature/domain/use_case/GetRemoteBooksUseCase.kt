package ir.kazemcodes.infinity.explore_feature.domain.use_case

import ir.kazemcodes.infinity.api_feature.network.ParsedHttpSource
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.local_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.IOException

class GetRemoteBooksUseCase {


    @Throws(InvalidBookException::class)
    operator fun invoke(page: Int, source: ParsedHttpSource): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            val req = source.latestUpdatesRequest(page)
            val res = source.client.newCall(req).await()
            val books = source.latestUpdatesParse(res)

            Timber.d("Timber: GetRemoteBooksUseCase was Finished Successfully")
//
//            val elements = source.fetchElements(
//                url = source.baseUrl, headers = mutableMapOf(
//                    Pair<String, String>("Referer", source.baseUrl)
//                )
//            )
//
//            emit(Resource.Error<List<Book>>(message = "Cloudflare distributed the progress"))
//
//            val books = source.fetchBooks(elements)
            emit(Resource.Success<List<Book>>(books.Books))

        } catch (e: HttpException) {
            emit(
                Resource.Error<List<Book>>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        } catch (e: IOException) {
            emit(Resource.Error<List<Book>>(message = "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(
                Resource.Error<List<Book>>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        }
    }

}