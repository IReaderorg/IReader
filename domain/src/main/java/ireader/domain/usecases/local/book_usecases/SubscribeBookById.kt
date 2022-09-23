package ireader.domain.usecases.local.book_usecases

import ireader.common.models.entities.Book
import ireader.domain.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/**
 * return a book from id
 */
@Factory
class SubscribeBookById(private val bookRepository: BookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return bookRepository.subscribeBookById(id = id)
    }
}
@Factory
class FindBookById(private val bookRepository: BookRepository) {
    suspend operator fun invoke(id: Long?): Book? {
        if (id == null) return null
        return bookRepository.findBookById(id = id)
    }
}

@Factory
class FindDuplicateBook(private val bookRepository: BookRepository) {
    suspend operator fun invoke(title:String, source: Long): Book? {
       return bookRepository.findDuplicateBook(title,source)
    }
}
