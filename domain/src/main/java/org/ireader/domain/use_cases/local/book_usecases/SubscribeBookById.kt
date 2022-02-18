package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

/**
 * return a book from id
 */
class SubscribeBookById(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return localBookRepository.subscribeBookById(id = id)
    }
}

class FindBookById(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(id: Long): Book? {
        return localBookRepository.findBookById(id = id)
    }
}


