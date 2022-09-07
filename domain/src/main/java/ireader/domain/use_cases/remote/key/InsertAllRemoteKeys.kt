package ireader.domain.use_cases.remote.key

import ireader.common.data.repository.RemoteKeyRepository
import ireader.common.models.entities.RemoteKeys
import org.koin.core.annotation.Factory

@Factory
class InsertAllRemoteKeys(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(keys: List<RemoteKeys>) {
        return ireader.common.extensions.withIOContext {
            remoteKeyRepository.insertAllRemoteKeys(keys)
        }
    }
}
