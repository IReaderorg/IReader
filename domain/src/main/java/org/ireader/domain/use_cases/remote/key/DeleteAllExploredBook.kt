package org.ireader.domain.use_cases.remote.key

import org.ireader.common_data.repository.RemoteKeyRepository
import javax.inject.Inject

class DeleteAllExploredBook @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllExploredBook()
    }
}


class DeleteAllSearchedBook @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke() {
        remoteKeyRepository.deleteAllSearchedBook()
    }
}

