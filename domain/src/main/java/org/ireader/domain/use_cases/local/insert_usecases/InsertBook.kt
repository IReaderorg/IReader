package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class InsertBook @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(book: Book): Long {
        return localBookRepository.insertBook(book)
    }
}