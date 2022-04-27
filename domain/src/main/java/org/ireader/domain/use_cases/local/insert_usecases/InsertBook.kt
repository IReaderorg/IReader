package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_data.repository.LocalBookRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertBook @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(book: Book): Long {
        return withIOContext {
            return@withIOContext localBookRepository.insertBook(book)
        }
    }
}