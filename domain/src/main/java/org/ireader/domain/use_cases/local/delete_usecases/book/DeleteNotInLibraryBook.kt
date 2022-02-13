package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository

/**
 * Delete All Books that are in library
 */
class DeleteNotInLibraryBook(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.deleteNotInLibraryChapters()
    }
}