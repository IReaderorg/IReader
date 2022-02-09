package org.ireader.domain.use_cases.local.book_usecases


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.utils.Resource
import org.ireader.infinity.core.domain.repository.LocalBookRepository
import retrofit2.HttpException
import java.io.IOException

/**
 * get All books that inLibrary field is true
 * note: when there is no book with that id it return a error resource
 */
class GetAllInLibraryBooks(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(): Flow<Resource<List<Book>>> = flow {
        try {
            localBookRepository.getAllInLibraryBooks().first { books ->
                if (books != null) {
                    emit(Resource.Success(books))
                    true
                } else {
                    Resource.Error<Resource<List<Book>>>(
                        uiText = UiText.StringResource(R.string.no_book_found_error),
                    )
                    true
                }
            }
        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        }


    }
}