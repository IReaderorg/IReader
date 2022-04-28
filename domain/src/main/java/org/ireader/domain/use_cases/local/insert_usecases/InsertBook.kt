package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_extensions.withIOContext
import javax.inject.Inject

class InsertBook @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(book: Book): Long {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localBookRepository.insertBook(book)
        }
    }
}