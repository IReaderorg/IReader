package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteKeyRepository
import javax.inject.Inject

class DeleteAllExploredBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllExploredBook()
    }
}

class FindDeleteAllExploredBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): List<Book> {
        return remoteKeyRepository.findDeleteAllExploredBook()
    }
}

class DeleteAllSearchedBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllSearchedBook()
    }
}

