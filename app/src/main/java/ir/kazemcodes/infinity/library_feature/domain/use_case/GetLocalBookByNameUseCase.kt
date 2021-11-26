package ir.kazemcodes.infinity.library_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalBookByNameUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke(bookName: String): Flow<Resource<Book?>> =
        flow {
            try {
                emit(Resource.Loading())
                val book = repository.localBookRepository.getBookByName(bookName = bookName)
                if (book !=null) {
                emit(Resource.Success<Book?>(data = book.toBook()))
                } else {
                    emit(Resource.Success<Book?>(data = null))
                }

            } catch (e: Exception) {
                emit(Resource.Error<Book?>(message = e.message.toString()))
            }
        }


}