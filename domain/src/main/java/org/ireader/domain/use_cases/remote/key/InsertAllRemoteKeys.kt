package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.repository.RemoteKeyRepository

class InsertAllRemoteKeys(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(keys: List<RemoteKeys>) {
        remoteKeyRepository.insertAllRemoteKeys(keys)
    }
}