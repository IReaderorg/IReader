package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.repository.RemoteKeyRepository
import javax.inject.Inject

class DeleteAllRemoteKeys @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllRemoteKeys()
    }
}