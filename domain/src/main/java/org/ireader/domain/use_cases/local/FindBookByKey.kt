package org.ireader.domain.use_cases.local

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class FindBooksByKey @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return localBookRepository.findBooksByKey(key)
    }

}

class SubscribeBooksByKey @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String, title: String): Flow<List<Book>> {
        return localBookRepository.subscribeBooksByKey(key, title)
    }

}

class FindBookByKey @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return localBookRepository.findBookByKey(key)
    }

}

