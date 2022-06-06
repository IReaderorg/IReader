package org.ireader.domain.use_cases.local.delete_usecases.book

import javax.inject.Inject

/**
 * Delete All Book from database
 */
class DeleteAllBooks @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke() {
        return bookRepository.deleteAllBooks()
    }
}
