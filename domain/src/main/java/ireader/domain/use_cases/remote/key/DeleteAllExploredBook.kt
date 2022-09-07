package ireader.domain.use_cases.remote.key

import ireader.common.data.repository.RemoteKeyRepository

class DeleteAllExploredBook(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllExploredBook()
    }
}

class DeleteAllSearchedBook(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllSearchedBook()
    }
}
