package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteLatestBooksUseCase {
    operator fun invoke(page: Int, source: Source): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteLatestBooksUseCase page: $page was Called")
            val books = source.fetchLatestUpdates(page)

            if (books.isCloudflareEnabled) {
                emit(Resource.Error<List<Book>>("CloudFlare is Enable"))
            } else {
                emit(Resource.Success<List<Book>>(books.books.map { it.copy(source = source.name) }))
            }


            Timber.d("Timber: GetRemoteLatestBooksUseCase page: $page was Finished Successfully")
            if (!books.hasNextPage) {
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