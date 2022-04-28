package org.ireader.domain.use_cases.remote.key

import org.ireader.common_models.entities.RemoteKeys
import org.ireader.common_extensions.withIOContext
import javax.inject.Inject

class InsertAllRemoteKeys @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke(keys: List<RemoteKeys>) {
        return org.ireader.common_extensions.withIOContext {
            remoteKeyRepository.insertAllRemoteKeys(keys)
        }
    }
}