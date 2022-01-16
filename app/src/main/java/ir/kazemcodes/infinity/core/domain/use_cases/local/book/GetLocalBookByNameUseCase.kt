package ir.kazemcodes.infinity.core.domain.use_cases.local.book

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalBookByNameUseCase @Inject constructor(
    private val repository: Repository,
) {

    operator fun invoke(book: Book): Flow<Resource<Book?>> =
        flow {
            try {
                Timber.d("Timber: GetLocalBookByNameUseCase was Called")
                emit(Resource.Loading())
                repository.localBookRepository.getBookByName(bookName = book.bookName)
                    .collect { bookEntity ->
                        if (bookEntity != null) {
                            emit(Resource.Success<Book?>(data = bookEntity.toBook()))
                        } else {
                            emit(Resource.Success<Book?>(data = Book.create()))
                        }
                    }
                Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")

            } catch (e: Exception) {
                emit(Resource.Error<Book?>(message = e.message.toString()))
            }
        }


}