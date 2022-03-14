package org.ireader.domain.use_cases.local

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class FindBooksByKey @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return localBookRepository.findBooksByKey(key)
    }

}

class FindBookByKey @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return localBookRepository.findBookByKey(key)
    }

}

