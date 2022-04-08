package org.ireader.domain.use_cases.remote.key

import javax.inject.Inject

data class RemoteKeyUseCase @Inject constructor(
    val deleteAllExploredBook: DeleteAllExploredBook,
    val findDeleteAllExploredBook: FindDeleteAllExploredBook,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val insertAllExploredBook: InsertAllExploredBook,
    val insertAllRemoteKeys: InsertAllRemoteKeys,
    val findAllPagedExploreBooks: FindAllPagedExploreBooks,
    val subScribeAllPagedExploreBooks: SubScribeAllPagedExploreBooks,
)
