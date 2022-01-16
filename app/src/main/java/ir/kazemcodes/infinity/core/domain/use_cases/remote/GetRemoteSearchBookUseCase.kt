package ir.kazemcodes.infinity.core.domain.use_cases.remote

import com.google.gson.JsonParseException
import ir.kazemcodes.infinity.core.data.network.models.BooksPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteSearchBookUseCase {

    operator fun invoke(page: Int, query: String, source: Source): Flow<Resource<BooksPage>> =
        flow {
            try {
                emit(Resource.Loading())
                Timber.d("Timber: GetRemoteSearchBookUseCase page: $page was Finished Called")
                val books = source.fetchSearch(page, query)


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
            } catch (e: JsonParseException) {
                emit(Resource.Error<BooksPage>(message = "something is wrong with json parser."))
            } catch (e: Exception) {
                emit(
                    Resource.Error<BooksPage>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )

            }
        }
}
