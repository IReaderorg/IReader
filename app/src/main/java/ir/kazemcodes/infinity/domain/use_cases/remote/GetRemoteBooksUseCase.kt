package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.domain.local_feature.domain.util.InvalidBookException
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteBooksUseCase {


    @Throws(InvalidBookException::class)
    operator fun invoke(page: Int, source: Source): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteBooksUseCase page: $page was Finished Called")
            val books = source.fetchLatestUpdates(page)

            Timber.d("Timber: GetRemoteBooksUseCase page: $page was Finished Successfully")
            if (books.hasNextPage) {
                emit(Resource.Success<List<Book>>(books.Books))
            } else{
                Resource.Error<List<Book>>(
                    message = "There is No More Books"
                )
            }

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