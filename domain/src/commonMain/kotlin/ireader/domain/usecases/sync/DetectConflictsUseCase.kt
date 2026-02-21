package ireader.domain.usecases.sync

import ireader.domain.models.sync.*

/**
 * Use case for detecting conflicts between local and remote sync data.
 * A conflict occurs when the same data has been modified on both devices
 * since the last sync.
 */
class DetectConflictsUseCase {
    /**
     * Detect conflicts between local and remote data.
     *
     * @param localData Local sync data
     * @param remoteData Remote sync data
     * @return List of detected conflicts
     */
    operator fun invoke(
        localData: SyncData,
        remoteData: SyncData
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()

        // Detect reading progress conflicts
        conflicts.addAll(detectReadingProgressConflicts(localData.readingProgress, remoteData.readingProgress))

        // Detect bookmark conflicts
        conflicts.addAll(detectBookmarkConflicts(localData.bookmarks, remoteData.bookmarks))

        // Detect book metadata conflicts
        conflicts.addAll(detectBookMetadataConflicts(localData.books, remoteData.books))

        return conflicts
    }

    private fun detectReadingProgressConflicts(
        local: List<ReadingProgressData>,
        remote: List<ReadingProgressData>
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()
        val localMap = local.associateBy { it.bookId }
        val remoteMap = remote.associateBy { it.bookId }

        for ((bookId, localProgress) in localMap) {
            val remoteProgress = remoteMap[bookId] ?: continue

            // Conflict if timestamps differ AND data differs
            if (localProgress.lastReadAt != remoteProgress.lastReadAt &&
                (localProgress.chapterIndex != remoteProgress.chapterIndex ||
                 localProgress.offset != remoteProgress.offset ||
                 localProgress.progress != remoteProgress.progress)
            ) {
                conflicts.add(
                    DataConflict(
                        conflictType = ConflictType.READING_PROGRESS,
                        localData = localProgress,
                        remoteData = remoteProgress,
                        conflictField = "chapterIndex"
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectBookmarkConflicts(
        local: List<BookmarkData>,
        remote: List<BookmarkData>
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()
        val localMap = local.associateBy { it.bookmarkId }
        val remoteMap = remote.associateBy { it.bookmarkId }

        for ((bookmarkId, localBookmark) in localMap) {
            val remoteBookmark = remoteMap[bookmarkId] ?: continue

            // Conflict if created times differ AND data differs
            if (localBookmark.createdAt != remoteBookmark.createdAt &&
                (localBookmark.position != remoteBookmark.position ||
                 localBookmark.note != remoteBookmark.note)
            ) {
                conflicts.add(
                    DataConflict(
                        conflictType = ConflictType.BOOKMARK,
                        localData = localBookmark,
                        remoteData = remoteBookmark,
                        conflictField = "position"
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectBookMetadataConflicts(
        local: List<BookSyncData>,
        remote: List<BookSyncData>
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()
        val localMap = local.associateBy { it.bookId }
        val remoteMap = remote.associateBy { it.bookId }

        for ((bookId, localBook) in localMap) {
            val remoteBook = remoteMap[bookId] ?: continue

            // Conflict if update times differ AND data differs
            if (localBook.updatedAt != remoteBook.updatedAt &&
                (localBook.title != remoteBook.title ||
                 localBook.author != remoteBook.author ||
                 localBook.fileHash != remoteBook.fileHash)
            ) {
                conflicts.add(
                    DataConflict(
                        conflictType = ConflictType.BOOK_METADATA,
                        localData = localBook,
                        remoteData = remoteBook,
                        conflictField = "title"
                    )
                )
            }
        }

        return conflicts
    }
}
