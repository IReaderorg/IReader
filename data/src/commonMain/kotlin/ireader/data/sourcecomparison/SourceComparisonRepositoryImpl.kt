package ireader.data.sourcecomparison

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.models.entities.SourceComparison
import kotlinx.coroutines.flow.Flow

class SourceComparisonRepositoryImpl(
    private val handler: DatabaseHandler
) : SourceComparisonRepository {
    
    override suspend fun getSourceComparisonByBookId(bookId: Long): SourceComparison? {
        return handler.awaitOneOrNull {
            sourceComparisonQueries.getSourceComparisonByBookId(bookId, sourceComparisonMapper)
        }
    }
    
    override fun subscribeSourceComparisonByBookId(bookId: Long): Flow<SourceComparison?> {
        return handler.subscribeToOneOrNull {
            sourceComparisonQueries.getSourceComparisonByBookId(bookId, sourceComparisonMapper)
        }
    }
    
    override suspend fun insertSourceComparison(sourceComparison: SourceComparison) {
        handler.await {
            sourceComparisonQueries.insert(
                bookId = sourceComparison.bookId,
                currentSourceId = sourceComparison.currentSourceId,
                betterSourceId = sourceComparison.betterSourceId,
                chapterDifference = sourceComparison.chapterDifference.toLong(),
                cachedAt = sourceComparison.cachedAt,
                dismissedUntil = sourceComparison.dismissedUntil
            )
        }
    }
    
    override suspend fun updateSourceComparison(sourceComparison: SourceComparison) {
        handler.await {
            sourceComparisonQueries.update(
                bookId = sourceComparison.bookId,
                currentSourceId = sourceComparison.currentSourceId,
                betterSourceId = sourceComparison.betterSourceId,
                chapterDifference = sourceComparison.chapterDifference.toLong(),
                cachedAt = sourceComparison.cachedAt,
                dismissedUntil = sourceComparison.dismissedUntil
            )
        }
    }
    
    override suspend fun upsertSourceComparison(sourceComparison: SourceComparison) {
        handler.await {
            sourceComparisonQueries.upsert(
                bookId = sourceComparison.bookId,
                currentSourceId = sourceComparison.currentSourceId,
                betterSourceId = sourceComparison.betterSourceId,
                chapterDifference = sourceComparison.chapterDifference.toLong(),
                cachedAt = sourceComparison.cachedAt,
                dismissedUntil = sourceComparison.dismissedUntil
            )
        }
    }
    
    override suspend fun deleteSourceComparison(bookId: Long) {
        handler.await {
            sourceComparisonQueries.delete(bookId)
        }
    }
    
    override suspend fun deleteOldEntries(timestamp: Long) {
        handler.await {
            sourceComparisonQueries.deleteOldEntries(timestamp)
        }
    }
    
    override suspend fun updateDismissedUntil(bookId: Long, dismissedUntil: Long) {
        handler.await {
            sourceComparisonQueries.updateDismissedUntil(
                bookId = bookId,
                dismissedUntil = dismissedUntil
            )
        }
    }
}
