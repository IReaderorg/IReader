package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.book

import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository

/**
 * Delete All books that have the parameter exploremode true and library true
 */
class SetExploreModeOffForInLibraryBooks(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.setExploreModeOffForInLibraryBooks()
    }
}