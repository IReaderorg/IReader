package org.ireader.domain.use_cases.local.book_usecases


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * get All books that inLibrary field is true
 * note: when there is no book with that id it return a error resource
 */
class SubscribeAllInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(): Flow<List<Book>> = flow {
        try {
            localBookRepository.subscribeAllInLibraryBooks(sortType = SortType.LastRead,
                isAsc = false,
                unreadFilter = false).first { books ->
                emit(books)
                true
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

class FindAllInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(): List<Book> {
        return localBookRepository.findAllInLibraryBooks(sortType = SortType.LastRead,
            isAsc = false,
            unreadFilter = false)
    }
}