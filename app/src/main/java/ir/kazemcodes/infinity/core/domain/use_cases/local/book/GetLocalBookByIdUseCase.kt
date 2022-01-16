package ir.kazemcodes.infinity.core.domain.use_cases.local.book

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalBookByIdUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(bookId: Int): Flow<Resource<Book?>> =
        flow {
            try {
                Timber.d("Timber: GetLocalBookByIdUseCase was Called")
                emit(Resource.Loading())
                repository.localBookRepository.getBookById(bookId = bookId).collect { book ->
                    if (book != null) {
                        emit(Resource.Success<Book?>(data = book.toBook()))
                    } else {
                        emit(Resource.Success<Book?>(data = null))
                    }
                }

                Timber.d("Timber: GetLocalBookByIdUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Book?>(message = e.message.toString()))
            }
        }


}