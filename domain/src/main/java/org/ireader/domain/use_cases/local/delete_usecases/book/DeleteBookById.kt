package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.common_data.repository.LocalBookRepository
import javax.inject.Inject

class DeleteBookById @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(id: Long) {
        return localBookRepository.deleteBookById(id)
    }
}
