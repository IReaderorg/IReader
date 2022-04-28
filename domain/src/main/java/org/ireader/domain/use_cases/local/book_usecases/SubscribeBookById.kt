package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.LocalBookRepository
import org.ireader.common_models.entities.Book
import javax.inject.Inject

/**
 * return a book from id
 */
class SubscribeBookById @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return localBookRepository.subscribeBookById(id = id)
    }
}

class FindBookById @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(id: Long): Book? {
        return localBookRepository.findBookById(id = id)
    }
}

class FindBookByIds @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(id: List<Long>): List<Book> {
        return localBookRepository.findBookByIds(id = id)
    }
}
