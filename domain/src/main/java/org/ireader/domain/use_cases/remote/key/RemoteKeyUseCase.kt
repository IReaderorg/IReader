package org.ireader.domain.use_cases.remote.key

data class RemoteKeyUseCase(
    val deleteAllExploredBook: DeleteAllExploredBook,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val insertAllExploredBook: InsertAllExploredBook,
    val insertAllRemoteKeys: InsertAllRemoteKeys,
)
