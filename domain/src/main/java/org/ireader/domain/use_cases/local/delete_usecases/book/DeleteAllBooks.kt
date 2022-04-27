package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.common_data.repository.LocalBookRepository
import javax.inject.Inject

/**
 * Delete All Book from database
 */
class DeleteAllBooks @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.deleteAllBooks()
    }
}
