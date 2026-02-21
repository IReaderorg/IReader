package ireader.domain.usecases.sync

import ireader.domain.models.sync.*

/**
 * Use case for resolving data conflicts between local and remote data.
 * Applies the specified conflict resolution strategy to determine which
 * version of the data to keep.
 */
class ResolveConflictsUseCase {
    /**
     * Resolve conflicts using the specified strategy.
     *
     * @param conflicts List of conflicts to resolve
     * @param strategy Strategy to use for resolution
     * @return Result containing list of resolved data or error
     */
    operator fun invoke(
        conflicts: List<DataConflict>,
        strategy: ConflictResolutionStrategy
    ): Result<List<Any>> {
        if (conflicts.isEmpty()) {
            return Result.success(emptyList())
        }

        if (strategy == ConflictResolutionStrategy.MANUAL) {
            return Result.failure(
                SyncError.ConflictResolutionFailed("Manual resolution required").toException()
            )
        }

        val resolved = conflicts.map { conflict ->
            when (strategy) {
                ConflictResolutionStrategy.LATEST_TIMESTAMP -> resolveByLatestTimestamp(conflict)
                ConflictResolutionStrategy.LOCAL_WINS -> conflict.localData
                ConflictResolutionStrategy.REMOTE_WINS -> conflict.remoteData
                ConflictResolutionStrategy.MERGE -> mergeConflict(conflict)
                ConflictResolutionStrategy.MANUAL -> error("Manual strategy should be handled above")
            }
        }

        return Result.success(resolved)
    }

    private fun resolveByLatestTimestamp(conflict: DataConflict): Any {
        return when (conflict.conflictType) {
            ConflictType.READING_PROGRESS -> {
                val local = conflict.localData as ReadingProgressData
                val remote = conflict.remoteData as ReadingProgressData
                if (local.lastReadAt >= remote.lastReadAt) local else remote
            }
            ConflictType.BOOKMARK -> {
                val local = conflict.localData as BookmarkData
                val remote = conflict.remoteData as BookmarkData
                if (local.createdAt >= remote.createdAt) local else remote
            }
            ConflictType.BOOK_METADATA -> {
                val local = conflict.localData as BookSyncData
                val remote = conflict.remoteData as BookSyncData
                if (local.updatedAt >= remote.updatedAt) local else remote
            }
        }
    }

    private fun mergeConflict(conflict: DataConflict): Any {
        return when (conflict.conflictType) {
            ConflictType.READING_PROGRESS -> {
                val local = conflict.localData as ReadingProgressData
                val remote = conflict.remoteData as ReadingProgressData
                // Merge by choosing furthest progress
                if (local.chapterIndex > remote.chapterIndex) {
                    local
                } else if (remote.chapterIndex > local.chapterIndex) {
                    remote
                } else {
                    // Same chapter, choose higher offset
                    if (local.offset >= remote.offset) local else remote
                }
            }
            ConflictType.BOOKMARK -> {
                // Bookmarks can't be meaningfully merged, fall back to latest timestamp
                resolveByLatestTimestamp(conflict)
            }
            ConflictType.BOOK_METADATA -> {
                // Book metadata can't be meaningfully merged, fall back to latest timestamp
                resolveByLatestTimestamp(conflict)
            }
        }
    }

    private fun SyncError.toException(): Exception {
        return when (this) {
            is SyncError.ConflictResolutionFailed -> Exception(message)
            else -> Exception("Conflict resolution failed")
        }
    }
}
