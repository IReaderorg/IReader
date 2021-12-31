package ir.kazemcodes.infinity.domain.use_cases.local.book

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
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