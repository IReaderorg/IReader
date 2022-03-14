package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.toBook
import org.ireader.domain.models.entities.updateBook
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.utils.Resource
import retrofit2.HttpException
import tachiyomi.source.Source
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

class GetBookDetail @Inject constructor(private val remoteRepository: RemoteRepository) {
    operator fun invoke(book: Book, source: Source): Flow<Resource<Book>> = flow {
        try {
            val now = Calendar.getInstance().timeInMillis
            Timber.d("Timber: Remote Book Detail for ${book.title} Was called")
            val bookDetail = remoteRepository.getRemoteBookDetail(book = book, source = source)
                .toBook(source.id)

            Timber.d("Timber: Remote Book Detail Was Fetched")

            emit(Resource.Success<Book>(updateBook(bookDetail, book)))
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