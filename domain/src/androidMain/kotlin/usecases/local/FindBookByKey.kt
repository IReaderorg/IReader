package ireader.domain.usecases.local

import ireader.domain.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Book
import org.koin.core.annotation.Factory

@Factory
class FindBooksByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return bookRepository.findBooksByKey(key)
    }
}
@Factory
class SubscribeBooksByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String, title: String): Flow<List<Book>> {
        return bookRepository.subscribeBooksByKey(key, title)
    }
}
@Factory
class FindBookByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return bookRepository.findBookByKey(key)
    }
}
