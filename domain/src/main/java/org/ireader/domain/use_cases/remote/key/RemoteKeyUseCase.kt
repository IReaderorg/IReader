package org.ireader.domain.use_cases.remote.key

import androidx.annotation.Keep
import javax.inject.Inject


data class RemoteKeyUseCase @Inject constructor(
    val deleteAllExploredBook: DeleteAllExploredBook,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val insertAllExploredBook: InsertAllExploredBook,
    val insertAllRemoteKeys: InsertAllRemoteKeys,
    val findAllPagedExploreBooks: FindAllPagedExploreBooks,
    val subScribeAllPagedExploreBooks: SubScribeAllPagedExploreBooks,
    val prepareExploreMode: PrepareExploreMode,
    val clearExploreMode: ClearExploreMode,
)
