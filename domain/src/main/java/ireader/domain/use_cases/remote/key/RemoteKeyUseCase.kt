package ireader.domain.use_cases.remote.key

data class RemoteKeyUseCase(
    val deleteAllExploredBook: DeleteAllExploredBook,
    val deleteAllRemoteKeys: DeleteAllRemoteKeys,
    val insertAllExploredBook: InsertAllExploredBook,
    val insertAllRemoteKeys: InsertAllRemoteKeys,
    val findAllPagedExploreBooks: FindAllPagedExploreBooks,
    val subScribeAllPagedExploreBooks: SubScribeAllPagedExploreBooks,
    val prepareExploreMode: PrepareExploreMode,
    val clearExploreMode: ClearExploreMode,
)
