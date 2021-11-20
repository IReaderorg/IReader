package ir.kazemcodes.infinity.explore_feature.domain.use_case

import android.util.Log
import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.base_feature.util.Constants.TAG
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetBookDetailUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    operator fun invoke(
        book: Book,
        url: String,
        headers: Map<String, String>
    ): Flow<Resource<Book>> = flow {
        try {

            emit(Resource.Loading())

            val data = repository.local.getBookByName(book.name)

            if (data != null) {
                emit(Resource.Success<Book>(data = data.toBook()))
                Log.d(TAG, "GetBookDetailUseCase Local: BookDetail was loaded Successfully ")
            } else {
                val elements = repository.remote.getElements(url = url, headers = headers)
                val bookDetail = repository.remote.getBookDetail(book, elements = elements)

                repository.local.insertBook(bookDetail.toBookEntity())
                emit(Resource.Success<Book>(bookDetail))
                Log.d(TAG, "GetBookDetailUseCase Remote: BookDetail was loaded Successfully ")
            }
        } catch (e: HttpException) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        } catch (e: IOException) {
            emit(Resource.Error<Book>(message = "Couldn't Read Server, Check Your Internet Connection."))
        }
    }

}