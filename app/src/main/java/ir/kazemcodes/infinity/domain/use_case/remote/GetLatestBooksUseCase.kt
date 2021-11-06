package ir.kazemcodes.infinity.domain.use_case.remote

import ir.kazemcodes.infinity.common.Resource
import ir.kazemcodes.infinity.data.remote.source.model.Book
import ir.kazemcodes.infinity.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetLatestBooksUseCase @Inject constructor(
    private val repository: RemoteRepository
) {



    @Throws(InvalidBookException::class)
    operator fun invoke(page : Int) : Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            val books = repository.getLatestBooks(page)
            emit(Resource.Success<List<Book>>(books))
        } catch (e: HttpException) {
        emit(Resource.Error<List<Book>>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))

    } catch (e: IOException) {
        emit(Resource.Error<List<Book>>(message = "Couldn't Read Server, Check Your Internet Connection."))
    }
    }

}