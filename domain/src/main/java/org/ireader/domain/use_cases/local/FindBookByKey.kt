package org.ireader.domain.use_cases.local

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

class FindBooksByKey(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): List<Book> {
        return localBookRepository.findBooksByKey(key)
    }

}

class FindBookByKey(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(key: String): Book? {
        return localBookRepository.findBookByKey(key)
    }

}

