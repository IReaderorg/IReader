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

class GetRemoteBookDetailUseCase {

    @Throws(InvalidBookException::class)
    operator fun invoke(
        book: Book,
        source:ParsedHttpSource
    ): Flow<Resource<Book>> = flow {
            emit(Resource.Loading())
        try {
            val req = source.bookDetailsRequest(book)
            val res = source.client.newCall(req).await()
            val bookDetail = source.bookDetailsParse(res)
            Timber.d("Timber: Remote Book Detail Was Fetched")
      emit(Resource.Success<Book>(bookDetail.copy(bookName = book.bookName, link = book.link, coverLink = book.coverLink)))
        } catch (e: HttpException) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        } catch (e: IOException) {
            emit(Resource.Error<Book>(message = "Couldn't Read Server, Check Your Internet Connection."))
        }catch (e: Exception) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        }
    }

}