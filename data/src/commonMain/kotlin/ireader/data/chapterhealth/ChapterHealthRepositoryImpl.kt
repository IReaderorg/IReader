package ireader.data.chapterhealth

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.models.entities.ChapterHealth
import kotlinx.coroutines.flow.Flow

class ChapterHealthRepositoryImpl(
    private val handler: DatabaseHandler
) : ChapterHealthRepository {
    
    override suspend fun getChapterHealthById(chapterId: Long): ChapterHealth? {
        return handler.awaitOneOrNull {
            chapterHealthQueries.getChapterHealthById(chapterId, chapterHealthMapper)
        }
    }
    
    override fun subscribeChapterHealthById(chapterId: Long): Flow<ChapterHealth?> {
        return handler.subscribeToOneOrNull {
            chapterHealthQueries.getChapterHealthById(chapterId, chapterHealthMapper)
        }
    }
    
    override suspend fun insertChapterHealth(chapterHealth: ChapterHealth) {
        handler.await {
            chapterHealthQueries.insert(
                chapterId = chapterHealth.chapterId,
                isBroken = chapterHealth.isBroken,
                breakReason = chapterHealth.breakReason?.name,
                checkedAt = chapterHealth.checkedAt,
                repairAttemptedAt = chapterHealth.repairAttemptedAt,
                repairSuccessful = chapterHealth.repairSuccessful,
                replacementSourceId = chapterHealth.replacementSourceId
            )
        }
    }
    
    override suspend fun updateChapterHealth(chapterHealth: ChapterHealth) {
        handler.await {
            chapterHealthQueries.update(
                chapterId = chapterHealth.chapterId,
                isBroken = chapterHealth.isBroken,
                breakReason = chapterHealth.breakReason?.name,
                checkedAt = chapterHealth.checkedAt,
                repairAttemptedAt = chapterHealth.repairAttemptedAt,
                repairSuccessful = chapterHealth.repairSuccessful,
                replacementSourceId = chapterHealth.replacementSourceId
            )
        }
    }
    
    override suspend fun upsertChapterHealth(chapterHealth: ChapterHealth) {
        handler.await {
            chapterHealthQueries.upsert(
                chapterId = chapterHealth.chapterId,
                isBroken = chapterHealth.isBroken,
                breakReason = chapterHealth.breakReason?.name,
                checkedAt = chapterHealth.checkedAt,
                repairAttemptedAt = chapterHealth.repairAttemptedAt,
                repairSuccessful = chapterHealth.repairSuccessful,
                replacementSourceId = chapterHealth.replacementSourceId
            )
        }
    }
    
    override suspend fun deleteChapterHealth(chapterId: Long) {
        handler.await {
            chapterHealthQueries.delete(chapterId)
        }
    }
    
    override suspend fun deleteOldEntries(timestamp: Long) {
        handler.await {
            chapterHealthQueries.deleteOldEntries(timestamp)
        }
    }
}
