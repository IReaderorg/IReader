package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository

/**
 * Delete All Books That are paged in Explore Screen
 */
class DeleteAllExploreBook(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.deleteAllExploreBook()
    }
}
