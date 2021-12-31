package ir.kazemcodes.infinity.domain.use_cases.local.book

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class GetLocalBooksUseCase @Inject constructor(
    private val repository: Repository,
) {

    operator fun invoke(): Flow<Resource<List<Book>>> = flow {
        try {
            Timber.d("Timber: GetLocalBooksUseCase was Called")
            emit(Resource.Loading())

            repository.localBookRepository.getBooks().map { it.map { book -> book.toBook() } }
                .collect { data ->

                    emit(Resource.Success<List<Book>>(data = data))
                }
            Timber.d("Timber: GetLocalBooksUseCase was Finished Successfully")
        } catch (e: IOException) {
            emit(Resource.Error<List<Book>>(message = "Couldn't load from local database."))
        } catch (e: Exception) {
            emit(Resource.Error<List<Book>>(message = e.message.toString()))
        }
    }
}