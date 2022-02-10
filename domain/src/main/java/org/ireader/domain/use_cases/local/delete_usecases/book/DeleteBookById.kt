package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository

class DeleteBookById(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(id: Int) {
        return localBookRepository.deleteBookById(id)
    }
}