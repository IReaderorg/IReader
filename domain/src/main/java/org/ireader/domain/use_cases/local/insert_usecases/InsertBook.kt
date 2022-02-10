package org.ireader.infinity.core.domain.use_cases.local.insert_usecases

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository

class InsertBook(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(book: Book) {
        localBookRepository.insertBook(book)
    }
}