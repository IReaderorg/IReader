package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteKeyRepository
import javax.inject.Inject

class InsertAllExploredBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return remoteKeyRepository.insertAllExploredBook(books)
    }
}