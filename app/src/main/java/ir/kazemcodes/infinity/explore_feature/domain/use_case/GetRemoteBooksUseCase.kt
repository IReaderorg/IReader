package ir.kazemcodes.infinity.explore_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetRemoteBooksUseCase @Inject constructor(
    private val repository: Repository
) {



    @Throws(InvalidBookException::class)
    operator fun invoke(url : String) : Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            val elements = repository.remote.getElements(url = url, headers = mutableMapOf(
                Pair<String, String>("Referer", "https://readwebnovels.net/")
            ))
            val books = repository.remote.getBooks(elements)
            emit(Resource.Success<List<Book>>(books))
        } catch (e: HttpException) {
        emit(Resource.Error<List<Book>>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))
    } catch (e: IOException) {
        emit(Resource.Error<List<Book>>(message = "Couldn't Read Server, Check Your Internet Connection."))
    }catch (e: Exception) {
            emit(Resource.Error<List<Book>>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))

        }
    }

}