package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

/**
 * Delete All Books That are paged in Explore Screen
 */
class DeleteAllExploreBook @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke() {
        return localBookRepository.deleteAllExploreBook()
    }
}
