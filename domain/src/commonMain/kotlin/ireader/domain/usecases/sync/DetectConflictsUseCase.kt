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

        // Detect history conflicts
        conflicts.addAll(detectHistoryConflicts(localData.history, remoteData.history))

        // Detect chapter conflicts
        conflicts.addAll(detectChapterConflicts(localData.chapters, remoteData.chapters))

        // Detect book metadata conflicts
        conflicts.addAll(detectBookMetadataConflicts(localData.books, remoteData.books))

        return conflicts
    }

    private fun detectHistoryConflicts(
        local: List<HistorySyncData>,
        remote: List<HistorySyncData>
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()
        val localMap = local.associateBy { it.chapterGlobalId }
        val remoteMap = remote.associateBy { it.chapterGlobalId }

        for ((chapterId, localHistory) in localMap) {
            val remoteHistory = remoteMap[chapterId] ?: continue

            // Conflict if timestamps differ AND data differs
            if (localHistory.lastRead != remoteHistory.lastRead &&
                localHistory.readingProgress != remoteHistory.readingProgress
            ) {
                conflicts.add(
                    DataConflict(
                        conflictType = ConflictType.HISTORY,
                        localData = localHistory,
                        remoteData = remoteHistory,
                        conflictField = "readingProgress"
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectChapterConflicts(
        local: List<ChapterSyncData>,
        remote: List<ChapterSyncData>
    ): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()
        val localMap = local.associateBy { it.globalId }
        val remoteMap = remote.associateBy { it.globalId }

        for ((chapterId, localChapter) in localMap) {
            val remoteChapter = remoteMap[chapterId] ?: continue

            // Conflict if fetch times differ AND data differs
            if (localChapter.dateFetch != remoteChapter.dateFetch &&
                (localChapter.read != remoteChapter.read ||
                 localChapter.bookmark != remoteChapter.bookmark ||
                 localChapter.lastPageRead != remoteChapter.lastPageRead)
            ) {
                conflicts.add(
                    DataConflict(
                        conflictType = ConflictType.CHAPTER,
                        localData = localChapter,
                        remoteData = remoteChapter,
                        conflictField = "read"
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
        val localMap = local.associateBy { it.globalId }
        val remoteMap = remote.associateBy { it.globalId }

        for ((bookId, localBook) in localMap) {
            val remoteBook = remoteMap[bookId] ?: continue

            // Conflict if update times differ AND data differs
            if (localBook.updatedAt != remoteBook.updatedAt &&
                (localBook.title != remoteBook.title ||
                 localBook.author != remoteBook.author ||
                 localBook.favorite != remoteBook.favorite)
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
