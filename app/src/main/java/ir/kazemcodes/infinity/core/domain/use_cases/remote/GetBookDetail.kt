package ir.kazemcodes.infinity.core.domain.use_cases.remote

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetBookDetail(private val remoteRepository: RemoteRepository) {
    operator fun invoke(book: Book, source: Source): Flow<Resource<Book>> = flow {
        try {
            Timber.d("Timber: Remote Book Detail for ${book.bookName} Was called")
            val bookDetail = remoteRepository.getRemoteBookDetail(book = book, source = source)

            Timber.d("Timber: Remote Book Detail Was Fetched")
            emit(Resource.Success<Book>(bookDetail.book.copy(bookName = book.bookName,
                link = book.link,
                coverLink = book.coverLink,
                sourceId = source.sourceId)))
        } catch (e: HttpException) {
            emit(
                Resource.Error<Book>(
                    uiText = UiText.noBook()
                )
            )

        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.noInternetError()
            )
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.DynamicString(e.localizedMessage ?: Constants.UNKNOWN_ERROR).asString()
            )
        }
    }
}