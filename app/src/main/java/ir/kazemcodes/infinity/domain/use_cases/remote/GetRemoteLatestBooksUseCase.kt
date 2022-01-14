package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.data.network.models.BooksPage
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteLatestBooksUseCase {
    operator fun invoke(page: Int, source: Source,hasNextPage : Boolean): Flow<Resource<BooksPage>> = flow {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteLatestBooksUseCase page: $page was Called")
            if (hasNextPage) {
                val books = source.fetchLatest(page)

                if (books.isCloudflareEnabled) {
                    emit(Resource.Error<BooksPage>("CloudFlare is Enable"))
                }  else {
                    emit(Resource.Success<BooksPage>(books))
                }
            } else {
                emit(Resource.Error<BooksPage>("There is No Page"))
            }
            Timber.d("Timber: GetRemoteLatestBooksUseCase page: $page was Finished Successfully")

        } catch (e: HttpException) {
            emit(
                Resource.Error<BooksPage>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        } catch (e: IOException) {
            emit(Resource.Error<BooksPage>(message = "No Internet is Available."))
        } catch (e: Exception) {
            emit(
                Resource.Error<BooksPage>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        }
    }

}