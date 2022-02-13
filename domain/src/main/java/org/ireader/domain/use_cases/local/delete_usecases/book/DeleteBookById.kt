package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository

class DeleteBookById(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(id: Long) {
        return localBookRepository.deleteBookById(id)
    }
}