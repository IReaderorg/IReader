package ir.kazemcodes.infinity.library_feature.domain.use_case.book

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class GetLocalBooksUseCase @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())

                repository.localBookRepository.getBooks().map { it.map { book -> book.toBook() } }.collect { data ->

                emit(Resource.Success<List<Book>>(data = data))
            }

        }  catch (e: IOException) {
            emit(Resource.Error<List<Book>>(message = "Couldn't load from local database."))
        }catch (e : Exception) {
            emit(Resource.Error<List<Book>>(message = e.message.toString()))
        }
    }
}