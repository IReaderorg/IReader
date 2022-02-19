package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.repository.RemoteKeyRepository

class DeleteAllRemoteKeys(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllRemoteKeys()
    }
}