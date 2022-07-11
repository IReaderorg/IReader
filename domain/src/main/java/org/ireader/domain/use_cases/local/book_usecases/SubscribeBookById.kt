package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Book
import javax.inject.Inject

/**
 * return a book from id
 */
class SubscribeBookById @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return bookRepository.subscribeBookById(id = id)
    }
}

class FindBookById @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(id: Long): Book? {
        return bookRepository.findBookById(id = id)
    }
}
