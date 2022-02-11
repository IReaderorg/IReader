package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.source.Source
import org.ireader.domain.utils.Resource
import org.ireader.infinity.core.domain.repository.RemoteRepository
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
                    uiText = UiText.StringResource(R.string.no_book_found_error)
                )
            )

        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.StringResource(R.string.noInternetError)
            )
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        }
    }
}