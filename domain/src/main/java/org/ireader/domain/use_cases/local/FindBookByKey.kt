package org.ireader.domain.use_cases.local

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Book
import org.ireader.common_data.repository.LocalBookRepository
import javax.inject.Inject

class FindBooksByKey @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return localBookRepository.findBooksByKey(key)
    }

}

class SubscribeBooksByKey @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(key: String, title: String): Flow<List<Book>> {
        return localBookRepository.subscribeBooksByKey(key, title)
    }

}

class FindBookByKey @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return localBookRepository.findBookByKey(key)
    }

}

