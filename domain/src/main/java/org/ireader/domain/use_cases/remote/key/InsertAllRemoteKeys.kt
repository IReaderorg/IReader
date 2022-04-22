package org.ireader.domain.use_cases.remote.key

import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.repository.RemoteKeyRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertAllRemoteKeys @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(keys: List<RemoteKeys>) {
        return withIOContext {
            remoteKeyRepository.insertAllRemoteKeys(keys)
        }
    }
}