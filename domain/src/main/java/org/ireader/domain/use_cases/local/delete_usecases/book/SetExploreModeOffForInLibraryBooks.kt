package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository

/**
 * Delete All books that have the parameter exploremode true and library true
 */
class SetExploreModeOffForInLibraryBooks(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.setExploreModeOffForInLibraryBooks()
    }
}