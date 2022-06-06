package org.ireader.domain.use_cases.local

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Book
import javax.inject.Inject

class FindBooksByKey @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return bookRepository.findBooksByKey(key)
    }
}

class SubscribeBooksByKey @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(key: String, title: String): Flow<List<Book>> {
        return bookRepository.subscribeBooksByKey(key, title)
    }
}

class FindBookByKey @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return bookRepository.findBookByKey(key)
    }
}
