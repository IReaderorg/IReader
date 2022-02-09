package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.book

import org.ireader.infinity.core.domain.repository.LocalBookRepository

/**
 * Delete All Books That are paged in Explore Screen
 */
class DeleteAllExploreBook(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.deleteAllExploreBook()
    }
}