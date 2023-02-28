package ireader.domain.usecases.local

import ireader.domain.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Book



class FindBooksByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return bookRepository.findBooksByKey(key)
    }
}

class SubscribeBooksByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String, title: String): Flow<List<Book>> {
        return bookRepository.subscribeBooksByKey(key, title)
    }
}

class FindBookByKey(private val bookRepository: BookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return bookRepository.findBookByKey(key)
    }
}
