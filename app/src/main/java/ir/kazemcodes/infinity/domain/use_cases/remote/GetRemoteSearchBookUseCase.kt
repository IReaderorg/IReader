package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.data.network.models.BooksPage
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteSearchBookUseCase {

    @Throws(InvalidBookException::class)
    operator fun invoke(page: Int, query: String, source: Source): Flow<Resource<BooksPage>> =
        flow {
            try {
                emit(Resource.Loading())
                Timber.d("Timber: GetRemoteSearchBookUseCase page: $page was Finished Called")
                val books = source.fetchSearchBook(page, query)


                Timber.d("Timber: GetRemoteSearchBookUseCase page: $page was Finished Successfully")
                emit(Resource.Success<BooksPage>(books))
            } catch (e: HttpException) {
                emit(
                    Resource.Error<BooksPage>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<BooksPage>(message = "Couldn't Read Server, Check Your Internet Connection."))
            } catch (e: Exception) {
                emit(
                    Resource.Error<BooksPage>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )

            }
        }
}