package ir.kazemcodes.infinity.core.domain.use_cases.remote

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteBookDetailUseCase {


    operator fun invoke(
        book: Book,
        source: Source,
    ): Flow<Resource<Book>> = flow {
        emit(Resource.Loading())
        try {
            Timber.d("Timber: Remote Book Detail for ${book.bookName} Was called")
            val bookDetail = source.fetchBook(book)

            Timber.d("Timber: Remote Book Detail Was Fetched")
            emit(Resource.Success<Book>(bookDetail.book.copy(bookName = book.bookName,
                link = book.link,
                coverLink = book.coverLink,
                source = source.name)))
        } catch (e: HttpException) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        } catch (e: IOException) {
            emit(Resource.Error<Book>(message = "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        }
    }

}