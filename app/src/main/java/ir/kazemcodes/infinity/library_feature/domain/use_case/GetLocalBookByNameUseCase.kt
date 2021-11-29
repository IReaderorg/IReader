package ir.kazemcodes.infinity.library_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalBookByNameUseCase @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(book: Book): Flow<Resource<Book?>> =
        flow {
            try {
                emit(Resource.Loading())
                repository.localBookRepository.getBookByName(bookName = book.bookName).collect { bookEntity->
                    if (bookEntity != null) {
                        emit(Resource.Success<Book?>(data = bookEntity.toBook()))
                    } else {
                        emit(Resource.Success<Book?>(data = Book.create()))
                    }
                }

            } catch (e: Exception) {
                emit(Resource.Error<Book?>(message = e.message.toString()))
            }
        }


}