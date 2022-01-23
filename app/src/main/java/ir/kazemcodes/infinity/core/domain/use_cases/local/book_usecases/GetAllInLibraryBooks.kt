package ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases


import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

/**
 * get All books that inLibrary field is true
 * note: when there is no book with that id it return a error resource
 */
class GetAllInLibraryBooks(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            localBookRepository.getAllInLibraryBooks().first { books ->
                if (books != null) {
                    emit(Resource.Success(books))
                    true
                } else {
                    Resource.Error<Resource<List<Book>>>(
                        message = Constants.NO_BOOKS_ERROR
                    )
                    true
                }
            }
        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                message = e.localizedMessage ?: ""
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                message = e.localizedMessage ?: ""
            )
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                message = e.localizedMessage ?: ""
            )
        }


    }
}