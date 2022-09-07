package ireader.domain.use_cases.remote.key

import ireader.common.data.repository.RemoteKeyRepository

class DeleteAllRemoteKeys(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllRemoteKeys()
    }
}
