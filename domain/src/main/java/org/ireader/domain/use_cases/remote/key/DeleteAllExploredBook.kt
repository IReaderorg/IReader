package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.repository.RemoteKeyRepository

class DeleteAllExploredBook(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllExploredBook()
    }
}

